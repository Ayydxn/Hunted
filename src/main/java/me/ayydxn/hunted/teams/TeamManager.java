package me.ayydxn.hunted.teams;

public class TeamManager
{
    public void clearTeams()
    {
        Teams.HUNTERS.getHandle().disband();
        Teams.SURVIVORS.getHandle().disband();
        Teams.SPECTATORS.getHandle().disband();
    }
}
