package me.ayydxn.hunted.game;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.config.HuntedMatchSettings;
import me.ayydxn.hunted.tasks.GameTickTask;
import me.ayydxn.hunted.teams.TeamManager;
import me.ayydxn.hunted.util.ServerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameManager
{
    private final HuntedPlugin plugin;
    private final TeamManager teamManager;

    private HuntedMatchSettings matchSettings;
    private MatchState currentMatchState;
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

        this.activeGameMode.onStart();

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
    }

    public HuntedMatchSettings getMatchSettings()
    {
        return this.matchSettings;
    }

    public MatchState getCurrentMatchState()
    {
        return this.currentMatchState;
    }

    public HuntedGameState getGameState()
    {
        return this.activeGameMode.gameState;
    }
}
