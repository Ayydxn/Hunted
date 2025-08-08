package me.ayydxn.hunted.game;

import me.ayydxn.hunted.HuntedPlugin;

/**
 * A functional interface that defines a factory for creating game mode instances.
 * <p>
 * This interface follows the factory pattern, allowing different implementations to provide their own game mode creation logic.
 * It is designed to work with the {@link GameModeRegistry} system, where providers are registered by name and used to instantiate game modes on demand.
 * <p>
 * Example:
 * <pre>{@code
 * // Lambda expression
 * GameModeRegistry.register("my_game_mode", plugin -> new MyGameMode(plugin));
 *
 * // Method reference
 * GameModeRegistry.register("my_game_mode", MyGameMode::new);
 * }</pre>
 *
 * <p>Implementations should ensure that the created game mode is properly initialized
 * and ready for use with the provided plugin instance.</p>
 *
 * @see GameModeRegistry
 */
@FunctionalInterface
public interface GameModeProvider
{
    /**
     * Creates and returns a new instance of a game mode.
     * <p>
     * This method is responsible for instantiating and initializing a specific game mode implementation.
     * The provided plugin instance should be used to access plugin resources, configuration, and other dependencies required by the game mode.
     *
     * @param plugin The Hunted instance that the game mode will be associated with
     * @return A new, fully initialized instance of the game mode
     */
    HuntedGameMode create(HuntedPlugin plugin);
}
