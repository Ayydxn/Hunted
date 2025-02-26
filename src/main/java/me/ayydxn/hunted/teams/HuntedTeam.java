package me.ayydxn.hunted.teams;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class HuntedTeam
{
    private final List<Player> players;
    private final String name;
    private final Component tag;
    private final Team scoreboardTeam;

    public HuntedTeam(String name, Component tag)
    {
        this.players = Lists.newArrayList();
        this.name = name;
        this.tag = tag;

        // We use this to get the prefix that appears next to the player's name in accordance with their team.
        this.scoreboardTeam = this.createScoreboardTeam();
    }

    public void addPlayer(Player player)
    {
        this.players.add(player);
        this.scoreboardTeam.addEntry(player.getName());
    }

    public void removePlayer(Player player)
    {
        this.players.remove(player);
        this.scoreboardTeam.removeEntry(player.getName());
    }

    private Team createScoreboardTeam()
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team bukkitTeam = scoreboard.getTeam(this.name);
        if (bukkitTeam == null)
            bukkitTeam = scoreboard.registerNewTeam(this.name);

        bukkitTeam.prefix(this.tag.append(Component.text(" ")));

        return bukkitTeam;
    }

    public void disband()
    {
        this.players.clear();
    }

    public List<Player> getMembers()
    {
        return this.players;
    }

    public String getName()
    {
        return this.name;
    }
}
