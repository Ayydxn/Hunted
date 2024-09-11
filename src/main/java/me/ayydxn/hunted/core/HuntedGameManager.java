package me.ayydxn.hunted.core;

import com.booksaw.betterTeams.Team;

public class HuntedGameManager
{
    private final Team huntersTeam;
    private final Team survivorsTeam;

    public HuntedGameManager()
    {
        this.huntersTeam = Team.getTeamManager().createNewTeam("Hunters", null);
        this.survivorsTeam = Team.getTeamManager().createNewTeam("Survivors", null);
    }
}
