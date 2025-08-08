package me.ayydxn.hunted.teams;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

/**
 * Represents a team in a match.
 * <p>
 * This class handles all team-related functionality including player management, player-team identification through name tags and automatic
 * scoreboard team integration for displaying team prefixes next to player names.
 * <p>
 * Each team maintains its own list of players and automatically manages the corresponding Bukkit scoreboard team for visual consistency in the game.
 */
public class HuntedTeam
{
    private final List<Player> players;
    private final String name;
    private final Component tag;
    private final NamedTextColor teamColor;
    private final Team scoreboardTeam;

    /**
     * Constructs a {@link HuntedTeam} with the corresponding
     *
     * @param name  The name for this team
     * @param tag   The visual tag/prefix component to be displayed at the start of each player's name
     * @param color The color that will be applied to the team tag
     */
    public HuntedTeam(String name, Component tag, NamedTextColor color)
    {
        this.players = Lists.newArrayList();
        this.name = name;
        this.tag = tag.color(color);
        this.teamColor = color;

        // We use this to get the prefix that appears next to the player's name in accordance with their team.
        this.scoreboardTeam = this.createScoreboardTeam();
    }

    /**
     * Adds a player to this team. This will also add the player to the {@link Team Bukkit scoreboard team}.
     *
     * @param player The player to add to this team
     */
    public void addPlayer(Player player)
    {
        this.players.add(player);
        this.scoreboardTeam.addEntry(player.getName());
    }

    /**
     * Removes a player from this team. This will also remove the player to the {@link Team Bukkit scoreboard team}.
     *
     * @param player the player to remove from this team
     */
    public void removePlayer(Player player)
    {
        this.players.remove(player);
        this.scoreboardTeam.removeEntry(player.getName());
    }

    public void disband()
    {
        this.players.forEach(player -> this.scoreboardTeam.removeEntry(player.getName()));
        this.players.clear();
    }

    /**
     * Creates and configures the Bukkit scoreboard team associated with this instance.
     * <p>
     * If a scoreboard team already exists, it will retrieve and use that instance. Otherwise, it'll register a new team and return the new instance.
     *
     * @return The configured Bukkit scoreboard team
     */
    private Team createScoreboardTeam()
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team bukkitTeam = scoreboard.getTeam(this.name);
        if (bukkitTeam == null)
            bukkitTeam = scoreboard.registerNewTeam(this.name);

        bukkitTeam.prefix(this.tag.append(Component.text(" ")));

        return bukkitTeam;
    }

    /**
     * Returns an {@link ImmutableList immutable list} of the members of this team. We return an immutable list in order to prevent accidental modifications
     * to team membership outside the intended methods.
     *
     * @return An immutable list of the players who currently belong to this team
     */
    public ImmutableList<Player> getMembers()
    {
        return ImmutableList.copyOf(this.players);
    }

    /**
     * Returns the name of this team.
     *
     * @return The name of this team
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the color used by this team for any visual elements such as name tags.
     *
     * @return The team's color
     */
    public NamedTextColor getTeamColor()
    {
        return this.teamColor;
    }
}
