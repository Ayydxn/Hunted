package me.ayydan.hunted;

import me.ayydan.hunted.commands.GlobalHuntedCommand;
import me.ayydan.hunted.commands.HuntedCommandTabCompleter;
import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.listeners.*;
import me.ayydan.hunted.utils.HuntedLogger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;

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

        for (Class<?> listenerClass : new Reflections(getClass().getPackage().getName() + ".listeners").getSubTypesOf(Listener.class))
        {
            try
            {
                Listener listenerClassInstance = (Listener) listenerClass.getDeclaredConstructor().newInstance();
                this.getServer().getPluginManager().registerEvents(listenerClassInstance, this);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exception)
            {
                exception.printStackTrace();
            }
        }

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
