package me.ayydan.hunted;

import me.ayydan.hunted.utils.HuntedLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntedPlugin extends JavaPlugin
{
    private static final HuntedLogger LOGGER = new HuntedLogger("Hunted Core");

    @Override
    public void onEnable()
    {
        LOGGER.info("Initializing Hunted v{}...", this.getPluginMeta().getVersion());
    }

    @Override
    public void onDisable()
    {
        LOGGER.info("Shutting down Hunted...");
    }

    public static HuntedLogger getHuntedLogger()
    {
        return LOGGER;
    }
}
