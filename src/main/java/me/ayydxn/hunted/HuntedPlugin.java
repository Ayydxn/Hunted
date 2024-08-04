package me.ayydxn.hunted;

import me.ayydxn.hunted.utils.HuntedLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntedPlugin extends JavaPlugin
{
    private static HuntedPlugin INSTANCE;

    private static final HuntedLogger LOGGER = new HuntedLogger("Hunted");

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable()
    {
        INSTANCE = this;

        LOGGER.info("Initializing Hunted... (Version: {})", this.getPluginMeta().getVersion());
    }

    @Override
    public void onDisable()
    {
        LOGGER.info("Shutting down Hunted...");
    }

    public static HuntedPlugin getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Hunted before one was available!");

        return INSTANCE;
    }

    public static HuntedLogger getHuntedLogger()
    {
        return LOGGER;
    }
}
