package fr.twah2em.nametag

import fr.twah2em.mcreflection.constructor
import fr.twah2em.mcreflection.invokeConstructor
import org.bukkit.OfflinePlayer
import java.lang.reflect.Constructor

open class TeamManager<T : Team>(private val teamClass: Class<T>) {
    private val teams = mutableSetOf<T>()

    open fun createTeam(name: String): T {
        return createTeam(name, "")
    }

    open fun createTeam(name: String, prefix: String): T {
        return createTeam(name, prefix, "")
    }

    open fun createTeam(name: String, prefix: String, suffix: String): T {
        if (teamByName(name) != null) {
            throw IllegalArgumentException("Team with name $name already exists")
        }

        val team = invokeConstructor(
            constructor(
                teamClass, arrayOf(
                    String::class.java,
                    String::class.java,
                    String::class.java
                )
            ) as Constructor<T>,
            name,
            prefix,
            suffix
        )

        teams.add(team)

        return team
    }

    open fun removeTeam(team: Team) {
        team.destroy()

        teams.remove(team)
    }

    open fun removeTeamByName(name: String) {
        removeTeam(teamByName(name)!!)
    }

    open fun removeAllTeams() {
        teams.forEach() { it.destroy() }

        teams.clear()
    }

    open fun teamByPlayer(player: OfflinePlayer): Team? {
        return teams.find { it.isAlly(player) }
    }

    open fun teamByName(name: String): Team? {
        return teams.find { it.name == name }
    }
}