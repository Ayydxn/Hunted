package me.ayydan.hunted.tasks;

import me.ayydan.hunted.callbacks.HuntedCountdownCompletionCallback;

public class HuntedResetWorldCountdown extends HuntedCountdownTask
{
    public HuntedResetWorldCountdown(int countdownTime, HuntedCountdownCompletionCallback countdownCompletionCallback)
    {
        super(countdownTime, countdownCompletionCallback);
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

        this.displayCountdown("The world will be reset in", null, true);
    }
}
