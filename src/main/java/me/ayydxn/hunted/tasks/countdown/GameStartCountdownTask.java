package me.ayydxn.hunted.tasks.countdown;

import me.ayydxn.hunted.game.GameInitializationState;
import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.teams.Teams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class GameStartCountdownTask extends HuntedCountdownTask
{
    private final GameManager gameManager;

    public GameStartCountdownTask(GameManager gameManager, CountdownCompleteCallback onCountdownComplete)
    {
        super(10, onCountdownComplete);

        this.gameManager = gameManager;
    }

    @Override
    public void start(BukkitTask bukkitTask)
    {
        GameInitializationState initializationState = this.gameManager.getInitializationState();
        if (initializationState == GameInitializationState.NOT_STARTED || initializationState == GameInitializationState.FAILED)
        {
            this.displayGameCancelledMessage();

            bukkitTask.cancel();

            return;
        }

        if (this.getTimeRemaining() == 0)
        {
            Title.Times messageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
            Component gameStartedComponent = Component.text("Game Started!", NamedTextColor.GREEN);

            for (Player player : Bukkit.getServer().getOnlinePlayers())
            {
                Title gameStartedMessage = Title.title(gameStartedComponent, Component.empty(), messageDuration);
                player.showTitle(gameStartedMessage);

                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            this.getCompletionCallback().onCountdownComplete();

            bukkitTask.cancel();

            return;
        }

        this.displayCountdown("Starting Game in", null, false);
    }

    private void displayGameCancelledMessage()
    {
        Title.Times messageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
        Title message = Title.title(Component.text("Game Cancelled!", NamedTextColor.RED), Component.empty(), messageDuration);

        for (Player player : Bukkit.getServer().getOnlinePlayers())
            player.showTitle(message);
    }
}
