package me.ayydxn.hunted;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntedPlugin extends JavaPlugin
{
    public static final Logger LOGGER = (Logger) LogManager.getLogger("Hunted");

    @Override
    public void onEnable()
    {
        LOGGER.info("Hello World!");
    }

    @Override
    public void onDisable()
    {
        LOGGER.error("Goodbye World!");
    }
}
