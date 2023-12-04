package me.ayydan.hunted.tasks;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.callbacks.HuntedCountdownCompletionCallback;
import me.ayydan.hunted.core.HuntedGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class HuntedGameStartCountdownTask extends HuntedCountdownTask
{
    public HuntedGameStartCountdownTask(int countdownTime, HuntedCountdownCompletionCallback countdownCompletionCallback)
    {
        super(countdownTime, countdownCompletionCallback);
    }

    @Override
    public void start()
    {
        if (HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Ending ||
                HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Ended)
        {
            this.displayGameCanceledMessage();

            this.cancel();

            return;
        }

        if (this.countdownTime == 0)
        {
            Title.Times gameStartedMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
            Component huntersSubtitle = Component.text("Objective: Kill all the survivors before they kill the Ender Dragon!", NamedTextColor.GOLD);
            Component survivorsSubtitle = Component.text("Objective: Run from the Hunters and kill the Ender Dragon!", NamedTextColor.GOLD);

            for (Player player : Bukkit.getServer().getOnlinePlayers())
            {
                if (HuntedPlugin.getInstance().getGameManager().getHuntersTeam().isPlayerInTeam(player))
                {
                    Title gameStartedMessage = Title.title(Component.text("Game Started!", NamedTextColor.GREEN), huntersSubtitle,
                            gameStartedMessageDuration);

                    player.showTitle(gameStartedMessage);
                }
                else if (HuntedPlugin.getInstance().getGameManager().getSurvivorsTeam().isPlayerInTeam(player))
                {
                    Title gameStartedMessage = Title.title(Component.text("Game Started!", NamedTextColor.GREEN), survivorsSubtitle,
                            gameStartedMessageDuration);

                    player.showTitle(gameStartedMessage);
                }
                else
                {
                    Title gameStartedMessage = Title.title(Component.text("Game Started!", NamedTextColor.GREEN), Component.empty(),
                            gameStartedMessageDuration);

                    player.showTitle(gameStartedMessage);
                }

                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            this.countdownCompletionCallback.onCountdownComplete();

            this.cancel();
            return;
        }

        this.displayCountdown("Starting Game in", null);
    }

    private void displayGameCanceledMessage()
    {
        Title.Times gameCanceledMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
        Title gameCanceledMessage = Title.title(Component.text("Game Canceled!", NamedTextColor.RED), Component.empty(), gameCanceledMessageDuration);

        for (Player player : Bukkit.getServer().getOnlinePlayers())
            player.showTitle(gameCanceledMessage);
    }
}
