package me.ayydxn.hunted.game;

import com.google.common.collect.Sets;
import me.ayydxn.hunted.core.GameStage;
import me.ayydxn.hunted.teams.Teams;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * The base representation of a game state within Hunted.
 * <p>
 * Similarly to {@link HuntedGameMode}, this class also takes after the <a href="https://dev.epicgames.com/documentation/en-us/unreal-engine/game-mode-and-game-state-in-unreal-engine">
 * game state design that exists within Unreal Engine<a/>. As the name implies, the game state is a way to define, store and manage the current state of the game.
 * This "state" can simply be known as details about the game at any given point in time that all players in the game need to be aware of. Examples of this could be
 * how much time is remaining in the current match, how many players are currently alive on a specific team and a team's current progress towards their goal.
 * <p>
 * The game state is not the best place to store properties that are specific to any one player and those should be stored elsewhere. Generally, it should be used to
 * store properties which change over the course of gameplay and are both relevant and visible to all players in the match.
 */
public class HuntedGameState
{
    private final Set<UUID> survivors = Sets.newHashSet();
    private final Set<UUID> hunters = Sets.newHashSet();
    private final Set<UUID> spectators = Sets.newHashSet();
    private GameStage gameStage = GameStage.ENDED;

    public void onTick()
    {
    }

    public void reset()
    {
        this.survivors.clear();
        this.hunters.clear();
        this.spectators.clear();

        this.gameStage = GameStage.ENDED;
    }

    public void addSurvivor(Player player)
    {
        Validate.isTrue(this.survivors.add(player.getUniqueId()), String.format("Player '%s' is already a survivor!", player.displayName()));

        Teams.SURVIVORS.getHandle().addPlayer(player);
    }

    public void removeSurvivor(Player player)
    {
        Validate.isTrue(this.survivors.remove(player.getUniqueId()), String.format("Player '%s' was not a survivor!", player.displayName()));

        Teams.SURVIVORS.getHandle().removePlayer(player);
    }

    public void addHunter(Player player)
    {
        Validate.isTrue(this.hunters.add(player.getUniqueId()), String.format("Player '%s' is already a hunter!", player.displayName()));

        Teams.HUNTERS.getHandle().addPlayer(player);
    }

    public void removeHunter(Player player)
    {
        Validate.isTrue(this.hunters.remove(player.getUniqueId()), String.format("Player '%s' was not a hunter!", player.displayName()));

        Teams.HUNTERS.getHandle().removePlayer(player);
    }

    public void addSpectator(Player player)
    {
        Validate.isTrue(this.spectators.add(player.getUniqueId()), String.format("Player '%s' is already a spectator!", player.displayName()));

        Teams.SPECTATORS.getHandle().addPlayer(player);
    }

    public void removeSpectator(Player player)
    {
        Validate.isTrue(this.spectators.remove(player.getUniqueId()), String.format("Player '%s' was not a spectator!", player.displayName()));

        Teams.SPECTATORS.getHandle().removePlayer(player);
    }

    public Set<UUID> getSurvivors()
    {
        return Collections.unmodifiableSet(this.survivors);
    }

    public Set<UUID> getHunters()
    {
        return Collections.unmodifiableSet(this.hunters);
    }

    public Set<UUID> getSpectators()
    {
        return Collections.unmodifiableSet(this.spectators);
    }

    public GameStage getGameStage()
    {
        return this.gameStage;
    }

    public void setGameStage(GameStage newGameStage)
    {
        this.gameStage = newGameStage;
    }
}
