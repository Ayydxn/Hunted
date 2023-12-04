package me.ayydan.hunted.tasks;

import me.ayydan.hunted.callbacks.HuntedCountdownCompletionCallback;
import org.bukkit.entity.Player;

public class HuntedRespawnCountdownTask extends HuntedCountdownTask
{
    private final Player killedPlayer;

    public HuntedRespawnCountdownTask(Player killedPlayer, int countdownTime, HuntedCountdownCompletionCallback countdownCompletionCallback)
    {
        super(countdownTime, countdownCompletionCallback);

        this.killedPlayer = killedPlayer;
    }

    @Override
    public void start()
    {
        if (this.countdownTime == 0)
        {
            this.countdownCompletionCallback.onCountdownComplete();

            this.cancel();

            return;
        }

        this.displayCountdown("You will be respawned in", this.killedPlayer);
    }
}
