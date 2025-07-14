package me.ayydxn.hunted.core;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.HuntedGameMode;
import me.ayydxn.hunted.tasks.GameTickTask;
import me.ayydxn.hunted.teams.TeamManager;
import me.ayydxn.hunted.util.ServerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameManager
{
    private final HuntedPlugin plugin;
    private final TeamManager teamManager;

    private GameStage currentGameStage;
    private HuntedGameMode activeGameMode;

    public GameManager(HuntedPlugin plugin)
    {
        this.plugin = plugin;
        this.teamManager = new TeamManager();

        this.currentGameStage = GameStage.ENDED;
    }

    public void startGame(HuntedGameMode gameMode)
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Starting a game of Hunted...", NamedTextColor.GREEN));

        this.currentGameStage = GameStage.STARTING;
        this.activeGameMode = gameMode;

        this.activeGameMode.onPreStart();

        GameTickTask gameTickTask = new GameTickTask(this);
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, gameTickTask, 0L, 100L);

        this.activeGameMode.onStart();

        this.currentGameStage = GameStage.ACTIVE;
    }

    public void tickGame()
    {
        this.activeGameMode.onTick();
    }

    public void endGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Ending the current game of Hunted...", NamedTextColor.GREEN));

        this.currentGameStage = GameStage.ENDING;

        this.activeGameMode.onPreEnd();

        this.teamManager.clearTeams();

        this.activeGameMode.onEnd();

        this.currentGameStage = GameStage.ENDED;
        this.activeGameMode = null;
    }

    public TeamManager getTeamManager()
    {
        return this.teamManager;
    }

    public GameStage getCurrentGameStage()
    {
        return this.currentGameStage;
    }
}
