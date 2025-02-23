package me.ayydxn.hunted.tasks;

import me.ayydxn.hunted.core.HuntedGameManager;
import me.ayydxn.hunted.core.HuntedGameState;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class HuntedGameTickTask implements Consumer<BukkitTask>
{
    private final HuntedGameManager huntedGameManager;

    public HuntedGameTickTask(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @Override
    public void accept(BukkitTask bukkitTask)
    {
        HuntedGameState currentGameState = this.huntedGameManager.getCurrentGameState();
        if (currentGameState == HuntedGameState.ENDING || currentGameState == HuntedGameState.ENDED)
        {
            bukkitTask.cancel();
            return;
        }

        this.huntedGameManager.tickGame();
    }
}
