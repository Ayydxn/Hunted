package me.ayydxn.hunted.tasks.countdown;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Consumer;

public abstract class HuntedCountdownTask implements Consumer<BukkitTask>
{
    private final CountdownCompleteCallback countdownCompleteCallback;

    private int countdownTime;

    public HuntedCountdownTask(int countdownTime, CountdownCompleteCallback countdownCompleteCallback)
    {
        this.countdownTime = countdownTime;
        this.countdownCompleteCallback = countdownCompleteCallback;
    }

    public abstract void start(BukkitTask bukkitTask);

    @Override
    public void accept(BukkitTask bukkitTask)
    {
        this.start(bukkitTask);
    }

    protected void displayCountdown(String message, @Nullable Player player, boolean displayOnActionBar)
    {
        Component secondsLeft = Component.text(String.format("%d seconds", this.countdownTime), NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);
        Component messageComponent = Component.text(message, NamedTextColor.YELLOW);
        Title.Times messageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
        Title titleMessage = Title.title(messageComponent, secondsLeft, messageDuration);

        if (player != null)
        {
            if (displayOnActionBar)
            {
                player.sendActionBar(messageComponent.append(Component.text(" ")).append(secondsLeft));
            }
            else
            {
                player.showTitle(titleMessage);
            }

            if (this.countdownTime <= 3)
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else
        {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
            {
                if (displayOnActionBar)
                {
                    onlinePlayer.sendActionBar(messageComponent.append(Component.text(" ")).append(secondsLeft));
                }
                else
                {
                    onlinePlayer.showTitle(titleMessage);
                }

                if (this.countdownTime <= 3)
                    onlinePlayer.playSound(onlinePlayer, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }


        this.countdownTime--;
    }

    public CountdownCompleteCallback getCompletionCallback()
    {
        return this.countdownCompleteCallback;
    }

    public int getTimeRemaining()
    {
        return this.countdownTime;
    }

    @FunctionalInterface
    public interface CountdownCompleteCallback
    {
        void onCountdownComplete();
    }
}
