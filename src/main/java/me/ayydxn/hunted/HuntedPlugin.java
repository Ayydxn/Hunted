package me.ayydxn.hunted;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.ayydxn.hunted.commands.GlobalHuntedCommand;
import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.custom.mode.ClassicGameMode;
import me.ayydxn.hunted.game.world.GameWorld;
import me.ayydxn.hunted.listeners.GlobalGameListeners;
import me.ayydxn.hunted.util.LocationSafetyCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entrypoint for Hunted.
 */
public final class HuntedPlugin extends JavaPlugin
{
    public static HuntedPlugin INSTANCE;

    public static final Logger LOGGER = (Logger) LogManager.getLogger("Hunted");

    private GameManager gameManager;

    @Override
    public void onEnable()
    {
        INSTANCE = this;

        LOGGER.info("Initializing Hunted... (Version: {})", this.getPluginMeta().getVersion());

        this.registerGameModes();

        this.gameManager = new GameManager(this);

        this.getServer().getPluginManager().registerEvents(new GlobalGameListeners(this.gameManager), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar()
                .register(new GlobalHuntedCommand(this.gameManager).createCommand().build()));
    }

    @Override
    public void onDisable()
    {
        GameWorld.clearWorldDeletionQueue();
        LocationSafetyCache.clear();

        this.gameManager.getTeamManager().clearTeams();

        INSTANCE = null;
    }

    /**
     * Registers all the default game modes that come with Hunted.
     */
    private void registerGameModes()
    {
        GameModeRegistry.register("classic", ClassicGameMode::new);

        int registeredGameModeCount = GameModeRegistry.getRegisteredGameModes().size();
        LOGGER.info("Registered {} game {} with Hunted", registeredGameModeCount, registeredGameModeCount == 1 ? "mode" : "modes");
    }

    /**
     * Returns the instance of Hunted for the currently running server instance.
     *
     * @return The current instance of Hunted
     * @throws IllegalStateException If an instance of Hunted isn't available
     */
    public static HuntedPlugin getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Hunted when one wasn't available!");

        return INSTANCE;
    }

    /**
     * Returns the currently active instance of the game manager.
     *
     * @return The current instance of the game manager.
     */
    public GameManager getGameManager()
    {
        return this.gameManager;
    }
}
