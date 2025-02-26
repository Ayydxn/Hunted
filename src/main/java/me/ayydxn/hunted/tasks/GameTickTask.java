package me.ayydxn.hunted.tasks;

import me.ayydxn.hunted.core.GameManager;
import me.ayydxn.hunted.core.GameState;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class GameTickTask implements Consumer<BukkitTask>
{
    private final GameManager gameManager;

    public GameTickTask(GameManager gameManager)
    {
        this.gameManager = gameManager;
    }

    @Override
    public void accept(BukkitTask bukkitTask)
    {
        GameState currentGameState = this.gameManager.getCurrentGameState();
        if (currentGameState == GameState.ENDING || currentGameState == GameState.ENDED)
        {
            bukkitTask.cancel();
            return;
        }

        this.gameManager.tickGame();
    }
}
