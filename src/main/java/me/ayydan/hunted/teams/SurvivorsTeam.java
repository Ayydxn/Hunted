package me.ayydan.hunted.teams;

import org.bukkit.entity.Player;

public class SurvivorsTeam extends HuntedTeam
{
    public SurvivorsTeam()
    {
        super("Survivors");
    }

    @Override
    public void addPlayer(Player player)
    {
        super.addPlayer(player);
    }

    @Override
    public void removePlayer(Player player)
    {
        super.removePlayer(player);
    }
}
