package me.ayydxn.hunted.game;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import me.ayydxn.hunted.HuntedPlugin;

import java.util.Map;

/**
 * A static registry for managing game mode providers and creating game modes from them.
 * <p>
 * This class serves as a central registry for all available game modes in the Hunted.
 * It provides functionality to register game mode providers by name and create instances of game modes through a factory pattern implementation.
 * <p>
 * The registry uses a simple name-based lookup system where each game mode is identified by a unique string key.
 * Game mode providers are responsible for creating instances of their respective game modes when requested.
 *
 * @see GameModeProvider
 */
public class GameModeRegistry
{
    private static final Map<String, GameModeProvider> GAME_MODES = Maps.newConcurrentMap();

    /**
     * Registers a game mode provider with the specified name.
     * <p>
     * This method associates a game mode provider with a unique string identifier, allowing it to be retrieved and instantiated later through
     * the {@link #create(String, HuntedPlugin)} method.
     * <p>
     * If a game mode with the same name is already registered, this function will early return and log a warning message.
     *
     * @param identifier       The unique identifier for this game mode (must be in all lower-case)
     * @param gameModeProvider The provider responsible for creating instances of this game mode
     */
    public static void register(String identifier, GameModeProvider gameModeProvider)
    {
        if (GAME_MODES.containsKey(identifier))
        {
            HuntedPlugin.LOGGER.warn("Game mode '{}' is already registered!", identifier);
            return;
        }

        GAME_MODES.put(identifier, gameModeProvider);
    }

    /**
     * Creates a new instance of the game mode with specified identifier.
     * <p>
     * This method looks up the game mode provider by name and delegates the creation
     * of the game mode instance to that provider. The provider receives the plugin
     * instance to properly initialize the game mode.
     *
     * @param identifier The unique identifier of the game mode to create
     * @param plugin     The plugin instance to pass to the game mode provider
     * @return A new instance of the requested game mode
     * @throws IllegalArgumentException If no game mode is registered with the specified identifier
     */
    public static HuntedGameMode create(String identifier, HuntedPlugin plugin)
    {
        GameModeProvider gameModeProvider = GAME_MODES.get(identifier);
        if (gameModeProvider != null)
            return gameModeProvider.create(plugin);

        throw new IllegalArgumentException(String.format("Failed to create unknown game mode '%s'!", identifier));
    }

    /**
     * Returns an {@link ImmutableMap immutable map} of all currently registered game mode providers.
     * <p>
     * The returned map contains all registered game mode providers with their associated names as keys. We return an immutable map to prevent accidental modifications
     * to the registry's internal state.
     *
     * @return An immutable map containing all registered game mode providers, keyed by their names
     */
    public static ImmutableMap<String, GameModeProvider> getRegisteredGameModes()
    {
        return ImmutableMap.copyOf(GAME_MODES);
    }
}

