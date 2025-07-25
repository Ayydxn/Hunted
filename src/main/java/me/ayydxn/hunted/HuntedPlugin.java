package me.ayydxn.hunted;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.ayydxn.hunted.commands.GlobalHuntedCommand;
import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.custom.mode.ClassicGameMode;
import me.ayydxn.hunted.game.world.GameWorld;
import me.ayydxn.hunted.teams.TeamManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class HuntedPlugin extends JavaPlugin
{
    public static HuntedPlugin INSTANCE;

    public static final Logger LOGGER = (Logger) LogManager.getLogger("Hunted");

    private GameManager gameManager;
    private Path gameMapsFolder;

    @Override
    public void onEnable()
    {
        INSTANCE = this;

        LOGGER.info("Initializing Hunted... (Version: {})", this.getPluginMeta().getVersion());

        this.createGameMapsFolder();
        this.registerGameModes();

        this.gameManager = new GameManager(this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar()
                .register(new GlobalHuntedCommand(this.gameManager).createCommand().build()));
    }

    @Override
    public void onDisable()
    {
        this.gameManager.getTeamManager().clearTeams();
        GameWorld.clearWorldDeletionQueue();

        INSTANCE = null;
    }

    private void createGameMapsFolder()
    {
        // Creates Hunted's data folder if it doesn't already exist.
        this.getDataFolder().mkdirs();

        this.gameMapsFolder = Path.of(new File(this.getDataFolder(), "gameMaps").toURI());

        try
        {
            Files.createDirectory(this.gameMapsFolder);
        }
        catch (IOException exception)
        {
            LOGGER.error(exception);
        }
    }

    private void registerGameModes()
    {
        GameModeRegistry.register("classic", ClassicGameMode::new);

        int registeredGameModeCount = GameModeRegistry.getRegisteredGameModes().size();
        LOGGER.info("Registered {} game {} with Hunted", registeredGameModeCount, registeredGameModeCount == 1 ? "mode" : "modes");
    }

    /**
     * Returns the instance of Hunted for the currently running server instance.
     *
     * @throws IllegalStateException If an instance of Hunted isn't available
     * @return The current instance of Hunted
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

    /**
     * Returns the path to the folder in which Hunted temporarily stores the world that a match is being played on.
     *
     * @return The path to the game maps folder.
     */
    public Path getGameMapsFolder()
    {
        return this.gameMapsFolder;
    }
}
