package me.ayydan.hunted.tasks;

import me.ayydan.hunted.callbacks.HuntedCountdownCompletionCallback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.time.Duration;

public abstract class HuntedCountdownTask extends BukkitRunnable
{
    protected int countdownTime;
    protected final HuntedCountdownCompletionCallback countdownCompletionCallback;

    public HuntedCountdownTask(int countdownTime, HuntedCountdownCompletionCallback countdownCompletionCallback)
    {
        this.countdownTime = countdownTime;
        this.countdownCompletionCallback = countdownCompletionCallback;
    }

    public abstract void start();

    @Override
    public final void run()
    {
        this.start();
    }

    public void displayCountdown(String message, @Nullable Player player)
    {
        Component countdownSeconds = Component.text(String.format("%d seconds", this.countdownTime), NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Title.Times countdownMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
        Title countdownMessage = Title.title(Component.text(message, NamedTextColor.YELLOW), countdownSeconds, countdownMessageDuration);

        if (player != null)
        {
            player.showTitle(countdownMessage);

            if (this.countdownTime <= 3)
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else
        {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
            {
                onlinePlayer.showTitle(countdownMessage);

                if (this.countdownTime <= 3)
                    onlinePlayer.playSound(onlinePlayer, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }


        this.countdownTime--;
    }

    public int getCountdownTime()
    {
        return this.countdownTime;
    }
}
