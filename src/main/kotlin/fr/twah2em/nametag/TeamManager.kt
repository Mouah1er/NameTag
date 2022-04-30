package fr.twah2em.nametag

import org.bukkit.OfflinePlayer

class TeamManager {
    private val teams = mutableSetOf<Team>()

    private fun removeAllTeams() {
        teams.forEach() { it.destroy() }

        teams.clear()
    }

    fun createTeam(name: String): Team {
        return createTeam(name, "")
    }

    fun createTeam(name: String, prefix: String): Team {
        return createTeam(name, prefix, "")
    }

    fun createTeam(name: String, prefix: String, suffix: String): Team {
        if (teamByName(name) != null) {
            throw IllegalArgumentException("Team with name $name already exists")
        }

        val team = Team(name, prefix, suffix)

        teams.add(team)

        return team
    }

    fun removeTeam(team: Team) {
        team.destroy()

        teams.remove(team)
    }

    fun removeTeamByName(name: String) {
        removeTeam(teamByName(name)!!)
    }

    fun teamByPlayer(player: OfflinePlayer): Team? {
        return teams.find { it.isAlly(player) }
    }

    fun teamByName(name: String): Team? {
        return teams.find { it.name == name }
    }
}