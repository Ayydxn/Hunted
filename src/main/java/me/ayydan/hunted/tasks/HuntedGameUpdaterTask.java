package me.ayydan.hunted.tasks;

import me.ayydan.hunted.core.HuntedGameManager;
import org.bukkit.scheduler.BukkitRunnable;

public class HuntedGameUpdaterTask extends BukkitRunnable
{
    private final HuntedGameManager huntedGameManager;

    public HuntedGameUpdaterTask(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @Override
    public void run()
    {
        this.huntedGameManager.tickGame();
    }
}
