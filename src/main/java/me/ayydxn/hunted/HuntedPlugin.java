package me.ayydxn.hunted;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.ayydxn.hunted.commands.GlobalHuntedCommand;
import me.ayydxn.hunted.core.HuntedGameManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntedPlugin extends JavaPlugin
{
    public static final Logger LOGGER = (Logger) LogManager.getLogger("Hunted");

    private HuntedGameManager huntedGameManager;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable()
    {
        LOGGER.info("Initializing Hunted... (Version: {})", this.getPluginMeta().getVersion());

        this.huntedGameManager = new HuntedGameManager(this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar()
                .register(new GlobalHuntedCommand(this.huntedGameManager).createCommand().build()));
    }
}
