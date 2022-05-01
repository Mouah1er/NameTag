package fr.twah2em.nametag

import fr.twah2em.mcreflection.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.lang.reflect.Constructor
import java.util.*

open class Team(val name: String, prefix: String, suffix: String) {
    private val packetPlayOutScoreboardTeamClass = NMSClass("PacketPlayOutScoreboardTeam")!!
    private val packetPlayOutScoreboardTeamConstructor: Constructor<*> =
        constructor(packetPlayOutScoreboardTeamClass, emptyArray())!!

    private val fieldsRequireString: Boolean =
        field(packetPlayOutScoreboardTeamClass, "b")!!.type.isAssignableFrom(String::class.java)
    private val fieldRequireInt: Boolean =
        field(packetPlayOutScoreboardTeamClass, "h")!!.type.isAssignableFrom(Int::class.javaPrimitiveType!!)

    private val receivers = mutableSetOf<UUID>()
    private val allies = mutableSetOf<UUID>()

    open var prefix: String = prefix
        set(value) {
            field = value
            receivers
                .mapNotNull { Bukkit.getPlayer(it) }
                .forEach { sendPacket(it, createTeamPacket(2)) }
        }

    open var suffix: String = suffix
        set(value) {
            field = value
            receivers
                .mapNotNull { Bukkit.getPlayer(it) }
                .forEach { sendPacket(it, createTeamPacket(2)) }
        }

    protected open fun createTeamPacket(mode: Int): Any {
        val packet = invokeConstructor(packetPlayOutScoreboardTeamConstructor)

        field(field(packetPlayOutScoreboardTeamClass, "a")!!, packet, name)

        if (fieldsRequireString) {
            field(field(packetPlayOutScoreboardTeamClass, "b")!!, packet, "")
            field(field(packetPlayOutScoreboardTeamClass, "c")!!, packet, prefix)
            field(field(packetPlayOutScoreboardTeamClass, "d")!!, packet, suffix)
        } else {
            field(field(packetPlayOutScoreboardTeamClass, "b")!!, packet, createChatMessage(""))
            field(field(packetPlayOutScoreboardTeamClass, "c")!!, packet, createChatMessage(prefix))
            field(field(packetPlayOutScoreboardTeamClass, "d")!!, packet, createChatMessage(suffix))
        }

        field(field(packetPlayOutScoreboardTeamClass, "e")!!, packet, "always")
        field(field(packetPlayOutScoreboardTeamClass, "f")!!, packet, 0)

        if (fieldRequireInt) {
            field(field(packetPlayOutScoreboardTeamClass, "h")!!, packet, mode)
        } else {
            field(
                field(packetPlayOutScoreboardTeamClass, "h")!!,
                packet,
                allies
                    .mapNotNull { Bukkit.getOfflinePlayer(it) }
                    .map { it.name })
        }

        field(field(packetPlayOutScoreboardTeamClass, "i")!!, packet, 0)

        return packet
    }

    protected open fun addPlayer(playerName: String): Any {
        val packet = invokeConstructor(packetPlayOutScoreboardTeamConstructor)

        field(field(packetPlayOutScoreboardTeamClass, "a")!!, packet, name)

        if (fieldRequireInt) {
            field(field(packetPlayOutScoreboardTeamClass, "h")!!, packet, 3)
            field(field(packetPlayOutScoreboardTeamClass, "g")!!, packet, listOf(playerName))
        } else {
            field(field(packetPlayOutScoreboardTeamClass, "h")!!, packet,
                allies
                    .mapNotNull { Bukkit.getOfflinePlayer(it) }
                    .map { it.name })
        }

        return packet
    }

    protected open fun removePlayer(playerName: String): Any {
        val packet = invokeConstructor(packetPlayOutScoreboardTeamConstructor)

        field(field(packetPlayOutScoreboardTeamClass, "a")!!, packet, name)

        if (fieldRequireInt) {
            field(field(packetPlayOutScoreboardTeamClass, "h")!!, packet, 4)
            field(field(packetPlayOutScoreboardTeamClass, "g")!!, packet, listOf(playerName))
        } else {
            field(field(packetPlayOutScoreboardTeamClass, "h")!!, packet,
                allies
                    .mapNotNull { Bukkit.getOfflinePlayer(it) }
                    .map { it.name })
        }

        return packet
    }

    open fun addPlayer(player: OfflinePlayer) {
        allies.add(player.uniqueId)

        if (player.isOnline) {
            receivers
                .mapNotNull { Bukkit.getOfflinePlayer(it) }
                .forEach {
                    sendPacket(
                        it.player,
                        this.addPlayer(player.name)
                    )
                }
        }
    }

    open fun removePlayer(player: OfflinePlayer) {
        allies.remove(player.uniqueId)

        if (fieldsRequireString) {
            if (player.isOnline) {
                receivers
                    .mapNotNull { Bukkit.getOfflinePlayer(it) }
                    .forEach {
                        sendPacket(
                            it.player,
                            this.removePlayer(player.name)
                        )
                    }
            }
        }
    }

    open fun addReceiver(player: OfflinePlayer) {
        receivers.add(player.uniqueId)

        if (player.isOnline) {
            sendPacket(
                player.player,
                createTeamPacket(0)
            )

            allies
                .mapNotNull { Bukkit.getOfflinePlayer(it) }
                .forEach {
                    sendPacket(
                        player.player,
                        addPlayer(it)
                    )
                }
        }
    }

    open fun removeReceiver(player: OfflinePlayer) {
        receivers.remove(player.uniqueId)

        if (player.isOnline) {
            sendPacket(
                player.player,
                createTeamPacket(1)
            )
        }
    }

    open fun destroy() {
        receivers.forEach {
            removeReceiver(Bukkit.getOfflinePlayer(it))
        }
    }

    open fun isReceiver(player: OfflinePlayer): Boolean {
        return receivers.stream().anyMatch { it.equals(player.uniqueId) }
    }

    open fun isAlly(player: OfflinePlayer): Boolean {
        return allies.stream().anyMatch { it.equals(player.uniqueId) }
    }

    private fun createChatMessage(message: String): Any {
        val chatMessageClass = NMSClass("ChatMessage")
        val chatMessageConstructor = chatMessageClass!!.getConstructor(String::class.java)

        return chatMessageConstructor.newInstance(message)
    }
}