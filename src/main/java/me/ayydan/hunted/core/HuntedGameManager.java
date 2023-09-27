package me.ayydan.hunted.core;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.tasks.HuntedGameUpdaterTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class HuntedGameManager
{
    private HuntedGameUpdaterTask gameUpdaterTask;
    private HuntedGameState currentGameState;

    public HuntedGameManager()
    {
        this.currentGameState = HuntedGameState.Ended;
    }

    public void startGame()
    {
        this.currentGameState = HuntedGameState.Starting;

        this.gameUpdaterTask = new HuntedGameUpdaterTask(this);
        this.gameUpdaterTask.runTaskTimer(HuntedPlugin.getInstance(), 0L, 100L);

        this.currentGameState = HuntedGameState.Active;
    }

    public void tickGame()
    {
        // TODO: (Ayydan) Remove this test code.
        Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt has been updated!", NamedTextColor.GREEN));
    }

    public void endGame()
    {
        this.currentGameState = HuntedGameState.Ending;

        this.gameUpdaterTask.cancel();

        this.currentGameState = HuntedGameState.Ended;
    }

    public HuntedGameState getCurrentGameState()
    {
        return this.currentGameState;
    }
}
