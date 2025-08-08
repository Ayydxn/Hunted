package me.ayydxn.hunted.game;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.config.HuntedMatchSettings;
import me.ayydxn.hunted.game.world.GameWorld;
import me.ayydxn.hunted.tasks.GameTickTask;
import me.ayydxn.hunted.teams.TeamManager;
import me.ayydxn.hunted.util.ServerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

/**
 *
 */
public class GameManager
{
    private final HuntedPlugin plugin;
    private final TeamManager teamManager;

    private HuntedMatchSettings matchSettings;
    private MatchState currentMatchState;
    private GameWorld activeGameWorld;
    private HuntedGameMode activeGameMode;

    public GameManager(HuntedPlugin plugin)
    {
        this.plugin = plugin;
        this.teamManager = new TeamManager();

        this.matchSettings = HuntedMatchSettings.defaults();
        this.currentMatchState = MatchState.ENDED;
        this.activeGameMode = null;
    }

    public void startGame(HuntedGameMode gameMode)
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Starting a game of Hunted...", NamedTextColor.GREEN));

        this.currentMatchState = MatchState.STARTING;
        this.activeGameMode = gameMode;

        this.activeGameMode.onPreStart();

        GameTickTask gameTickTask = new GameTickTask(this);
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, gameTickTask, 0L, 100L);

        this.activeGameWorld = new GameWorld(String.format("huntedGameWorld_active_%s", UUID.randomUUID()));
        this.activeGameWorld.create();

        // TODO: Perform the following when preparing to start a new game (X = Completed)
        // - Generate a new world (X)
        // - Determine what biome each team will spawn in (Spectators will be left with either team at random)
        // - Transport all players to said world
        // - Restrict all player movement
        // - Start a countdown

        this.activeGameMode.onStart();

        // TODO: Perform the following once the countdown ends and the game begins (X = Completed):
        // - Unrestrict all player movement
        // - Pass all control to the game mode so that it can do whatever other setup it needs to so that it can be played properly.

        this.currentMatchState = MatchState.ACTIVE;
    }

    public void tickGame()
    {
        this.activeGameMode.onTick();
    }

    public void endGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Ending the current game of Hunted...", NamedTextColor.GREEN));

        this.currentMatchState = MatchState.ENDING;

        this.activeGameMode.onPreEnd();

        this.teamManager.clearTeams();
        this.matchSettings = HuntedMatchSettings.defaults();

        this.activeGameMode.onEnd();

        this.currentMatchState = MatchState.ENDED;
        this.activeGameMode = null;

        this.activeGameWorld.unload();
        this.activeGameMode = null;
    }

    public TeamManager getTeamManager()
    {
        return this.teamManager;
    }

    public HuntedMatchSettings getMatchSettings()
    {
        return this.matchSettings;
    }

    public MatchState getCurrentMatchState()
    {
        return this.currentMatchState;
    }
}
