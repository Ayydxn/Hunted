package me.ayydan.hunted;

import me.ayydan.hunted.commands.GlobalHuntedCommand;
import me.ayydan.hunted.commands.HuntedCommandTabCompleter;
import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.listeners.PlayerEventsListener;
import me.ayydan.hunted.utils.HuntedLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntedPlugin extends JavaPlugin
{
    private static HuntedPlugin INSTANCE;

    private static final HuntedLogger LOGGER = new HuntedLogger("Hunted Core");

    private HuntedGameManager huntedGameManager;

    @Override
    public void onEnable()
    {
        LOGGER.info("Initializing Hunted v{}...", this.getPluginMeta().getVersion());

        INSTANCE = this;

        this.huntedGameManager = new HuntedGameManager();

        this.getServer().getPluginManager().registerEvents(new PlayerEventsListener(), this);

        this.getCommand("hunted").setExecutor(new GlobalHuntedCommand(this.huntedGameManager));
        this.getCommand("hunted").setTabCompleter(new HuntedCommandTabCompleter());
    }

    @Override
    public void onDisable()
    {
        LOGGER.info("Shutting down Hunted...");
    }

    public static HuntedPlugin getInstance()
    {
        return INSTANCE;
    }

    public static HuntedLogger getHuntedLogger()
    {
        return LOGGER;
    }

    public HuntedGameManager getGameManager()
    {
        return this.huntedGameManager;
    }
}
