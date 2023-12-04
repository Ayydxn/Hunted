package me.ayydan.hunted.tasks;

import me.ayydan.hunted.HuntedPlugin;
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

        if (HuntedPlugin.getInstance().getGameManager().getHuntersTeam().getPlayerCount() > 1)
        {
            this.displayCountdown("You will be able to respawn in", this.killedPlayer);
        }
        else
        {
            this.displayCountdown("You will be respawned in", this.killedPlayer);
        }
    }
}
