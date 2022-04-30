package fr.twah2em.nametag

import org.bukkit.OfflinePlayer

open class TeamManager {
    private val teams = mutableSetOf<Team>()

    open fun createTeam(name: String): Team {
        return createTeam(name, "")
    }

    open fun createTeam(name: String, prefix: String): Team {
        return createTeam(name, prefix, "")
    }

    open fun createTeam(name: String, prefix: String, suffix: String): Team {
        if (teamByName(name) != null) {
            throw IllegalArgumentException("Team with name $name already exists")
        }

        val team = Team(name, prefix, suffix)

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