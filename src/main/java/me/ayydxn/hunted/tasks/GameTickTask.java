package me.ayydxn.hunted.tasks;

import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.MatchState;
import org.apache.commons.lang3.Validate;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A custom Bukkit task that handles periodic game ticks and lifecycle management.
 * <p>
 * This task will automatically cancel itself when a game is ending, preventing unnecessary continued game ticks.
 */
public class GameTickTask implements Consumer<BukkitTask>
{
    private final GameManager gameManager;

    /**
     * Constructs a new instance of this class with an associated {@link GameManager game manager}.
     *
     * @param gameManager The game manager will be used for executing game ticks
     */
    public GameTickTask(@NotNull GameManager gameManager)
    {
        Validate.notNull(gameManager, "The game manager cannot be null!");

        this.gameManager = gameManager;
    }

    /**
     * Executes a single game tick.
     * <p>
     * The task will automatically cancel itself when the match states becomes {@link MatchState#ENDING} or {@link MatchState#ENDED}, therefore preventing continued
     * game ticks.
     *
     * @param bukkitTask The Bukkit task instance that can be used for self-cancellation
     */
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
