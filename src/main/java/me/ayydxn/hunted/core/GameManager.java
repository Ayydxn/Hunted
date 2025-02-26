package me.ayydxn.hunted.core;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.tasks.GameTickTask;
import me.ayydxn.hunted.teams.TeamManager;
import me.ayydxn.hunted.util.ServerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameManager
{
    private final HuntedPlugin plugin;
    private final TeamManager teamManager;

    private GameState currentGameState;


    public GameManager(HuntedPlugin plugin)
    {
        this.plugin = plugin;
        this.teamManager = new TeamManager();

        this.currentGameState = GameState.ENDED;
    }

    public void startGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Starting a game of Hunted...", NamedTextColor.GREEN));

        this.currentGameState = GameState.STARTING;

        GameTickTask gameTickTask = new GameTickTask(this);
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, gameTickTask, 0L, 100L);

        this.currentGameState = GameState.ACTIVE;
    }

    public void tickGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Ticked Game!", NamedTextColor.GREEN));
    }

    public void endGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Ending the current game of Hunted...", NamedTextColor.GREEN));

        this.currentGameState = GameState.ENDING;

        this.teamManager.clearTeams();

        this.currentGameState = GameState.ENDED;
    }

    public TeamManager getTeamManager()
    {
        return this.teamManager;
    }

    public GameState getCurrentGameState()
    {
        return this.currentGameState;
    }
}
