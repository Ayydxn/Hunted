package me.ayydxn.hunted.core;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.tasks.HuntedGameTickTask;
import me.ayydxn.hunted.util.ServerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HuntedGameManager
{
    private final HuntedPlugin plugin;

    private HuntedGameState currentGameState;

    public HuntedGameManager(HuntedPlugin plugin)
    {
        this.plugin = plugin;

        this.currentGameState = HuntedGameState.ENDED;
    }

    public void startGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Starting a game of Hunted...", NamedTextColor.GREEN));

        this.currentGameState = HuntedGameState.STARTING;

        HuntedGameTickTask huntedGameTickTask = new HuntedGameTickTask(this);
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, huntedGameTickTask, 0L, 100L);

        this.currentGameState = HuntedGameState.ACTIVE;
    }

    public void tickGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Ticked Game!", NamedTextColor.GREEN));
    }

    public void endGame()
    {
        ServerUtils.broadcastMessage(this.plugin, Component.text("Ending the current game of Hunted...", NamedTextColor.GREEN));

        this.currentGameState = HuntedGameState.ENDING;

        // TODO: (Ayydan) Whatever needs to be done when we end the game goes here.

        this.currentGameState = HuntedGameState.ENDED;
    }

    public HuntedGameState getCurrentGameState()
    {
        return this.currentGameState;
    }
}
