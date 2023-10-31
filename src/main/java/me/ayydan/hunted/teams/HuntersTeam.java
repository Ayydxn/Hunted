package me.ayydan.hunted.teams;

import org.bukkit.entity.Player;

public class HuntersTeam extends HuntedTeam
{
    public HuntersTeam()
    {
        super("Hunters");
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
