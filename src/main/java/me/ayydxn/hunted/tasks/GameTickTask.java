package me.ayydxn.hunted.tasks;

import me.ayydxn.hunted.core.GameManager;
import me.ayydxn.hunted.core.GameStage;
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
        GameStage currentGameStage = this.gameManager.getCurrentGameStage();
        if (currentGameStage == GameStage.ENDING || currentGameStage == GameStage.ENDED)
        {
            bukkitTask.cancel();
            return;
        }

        this.gameManager.tickGame();
    }
}
