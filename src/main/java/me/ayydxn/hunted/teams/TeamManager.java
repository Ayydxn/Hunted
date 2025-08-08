package me.ayydxn.hunted.teams;

/**
 * A simple interface for managing all the playable teams found in the {@link Teams} enum.
 */
public class TeamManager
{
    /**
     * Strips all {@link Teams} except {@link Teams#UNKNOWN} of their members and removes the players from the {@link org.bukkit.scoreboard.Team Bukkit team}.
     */
    public void clearTeams()
    {
        Teams.HUNTERS.getHandle().disband();
        Teams.SURVIVORS.getHandle().disband();
        Teams.SPECTATORS.getHandle().disband();
    }
}
