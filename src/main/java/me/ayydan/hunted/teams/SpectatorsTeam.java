package me.ayydan.hunted.teams;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SpectatorsTeam extends HuntedTeam
{
    public SpectatorsTeam()
    {
        super("Spectators");
    }

    @Override
    public void addPlayer(Player player)
    {
        super.addPlayer(player);

        player.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void removePlayer(Player player)
    {
        super.removePlayer(player);
    }
}
