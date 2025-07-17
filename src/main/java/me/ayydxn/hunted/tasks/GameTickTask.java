package me.ayydxn.hunted.tasks;

import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.MatchState;
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
        MatchState currentMatchState = this.gameManager.getCurrentMatchState();
        if (currentMatchState == MatchState.ENDING || currentMatchState == MatchState.ENDED)
        {
            bukkitTask.cancel();
            return;
        }

        this.gameManager.tickGame();
    }
}
