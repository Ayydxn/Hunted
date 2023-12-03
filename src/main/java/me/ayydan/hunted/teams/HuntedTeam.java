package me.ayydan.hunted.teams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class HuntedTeam
{
    protected final HashMap<UUID, Player> playersToUUIDMap = new HashMap<>();

    protected String teamName;

    public HuntedTeam(String name)
    {
        this.teamName = name;
    }

    public void addPlayer(Player player)
    {
        this.playersToUUIDMap.put(player.getUniqueId(), player);
    }

    public void removePlayer(Player player)
    {
        this.playersToUUIDMap.remove(player.getUniqueId());
    }

    public boolean isPlayerInTeam(Player player)
    {
        return this.playersToUUIDMap.containsKey(player.getUniqueId());
    }

    public ArrayList<Player> getPlayers()
    {
        ArrayList<Player> players = new ArrayList<>();

        for (UUID playerUUID : playersToUUIDMap.keySet())
            players.add(Bukkit.getServer().getPlayer(playerUUID));

        return players;
    }

    public String getTeamName()
    {
        return this.teamName;
    }

    public int getPlayerCount()
    {
        return this.playersToUUIDMap.size();
    }
}
