package me.ayydxn.hunted.game;

/**
 * Represents the different stages that a game can be in while it is being initialized.
 */
public enum GameInitializationState
{
    /** Game initialization has not started. */
    NOT_STARTED,

    /** Game initialization has started and is currently underway */
    STARTED,

    /** Currently creating and preparing the game world. */
    INITIALIZING_WORLD,

    /** Scanning the world for available spawn biomes. */
    SCANNING_BIOMES,

    /** Selecting appropriate spawn biomes for teams. */
    SELECTING_SPAWN_BIOMES,

    /** Preparing players and setting up initial game state. */
    PREPARING_PLAYERS,

    /** Running the pre-game countdown. */
    STARTING_COUNTDOWN,

    /** Initialization completed successfully. */
    COMPLETE,

    /** Initialization failed and cleanup was performed. */
    FAILED
}
