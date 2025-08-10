package me.ayydxn.hunted.game;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.config.HuntedMatchSettings;
import me.ayydxn.hunted.game.world.GameWorld;
import me.ayydxn.hunted.game.world.TeamSpawnBiomeSelector;
import me.ayydxn.hunted.teams.TeamManager;
import me.ayydxn.hunted.teams.Teams;
import me.ayydxn.hunted.util.ServerUtils;
import me.ayydxn.hunted.world.LocationSafetyCache;
import me.ayydxn.hunted.world.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Responsible for managing the lifecycle and state of Hunted games.
 * <p>
 * This class handles game initialization, updates, and cleanup through a state machine pattern that tracks initialization progress and ensures proper
 * sequencing of game setup operations.
 */
public class GameManager
{
    private final HuntedPlugin plugin;
    private final TeamManager teamManager;
    private final Object stateLock = new Object();

    // Game state
    private HuntedMatchSettings matchSettings;
    private MatchState currentMatchState;
    private GameInitializationState initializationState;

    // Game components
    private GameWorld activeGameWorld;
    private HuntedGameMode activeGameMode;

    /**
     * Constructs a new {@link GameManager} with the specified instance of Hunted.
     *
     * @param plugin The current instance of Hunted
     */
    public GameManager(HuntedPlugin plugin)
    {
        this.plugin = plugin;
        this.teamManager = new TeamManager();

        this.matchSettings = HuntedMatchSettings.defaults();
        this.currentMatchState = MatchState.ENDED;
        this.initializationState = GameInitializationState.NOT_STARTED;

        this.activeGameMode = null;
    }

    /**
     * Starts a new game with the specified game mode.
     * <p>
     * This method initiates the complete game initialization sequence.
     * World creation is done synchronously on the main server thread, while biome scanning and selection are performed asynchronously for better performance.
     *
     * @param gameMode The game mode to start with
     * @throws IllegalStateException If a game is already running or initializing
     */
    public void startGame(HuntedGameMode gameMode)
    {
        synchronized (this.stateLock)
        {
            if (this.currentMatchState != MatchState.ENDED)
                throw new IllegalStateException("Cannot start a game while the one is already active or currently ending!");

            if (this.initializationState != GameInitializationState.NOT_STARTED)
                throw new IllegalStateException("Cannot initialize a game while initialization of one is already in progress!");

            this.currentMatchState = MatchState.STARTING;
            this.initializationState = GameInitializationState.STARTED;
            this.activeGameMode = gameMode;
        }

        ServerUtils.broadcastMessage(this.plugin, Component.text("Starting a game of Hunted...", NamedTextColor.GREEN));

        // Notify the game mode that are we performing initialization.
        this.activeGameMode.onPreStart();

        // Start game initialization by creating the game world and going from there.
        // We create the world on the main thread as Multiverse does things during world creation that can only happen synchronously.
        this.initializeGameWorld();

        // Perform biome scan and selection asynchronously. Afterward, we go back to the main server thread and continue there.
        this.scanSpawnBiomesAsync().thenCompose(this::selectTeamSpawnBiomes)
                .thenAccept(this::completeInitializationOnMainThread)
                .exceptionally(this::onInitializationError);
    }

    /**
     * Performs one tick of the currently active game every 100 ticks (5 seconds).
     * What happens here is usually handled by the {@link HuntedGameMode} being used.
     */
    public void tickGame()
    {
        this.activeGameMode.onTick();
    }

    /**
     * Ends the currently active game and cleanups any resources that were being used.
     */
    public void endGame()
    {
        synchronized (this.stateLock)
        {
            this.currentMatchState = MatchState.ENDED;
        }

        ServerUtils.broadcastMessage(this.plugin, Component.text("Ending the current game of Hunted...", NamedTextColor.GREEN));

        if (this.activeGameMode != null)
            this.activeGameMode.onPreEnd();

        this.teamManager.clearTeams();
        this.matchSettings = HuntedMatchSettings.defaults();

        // Don't need to check for null again because, by this point, we already know it isn't.
        this.activeGameMode.onEnd();

        if (this.activeGameWorld != null)
        {
            this.activeGameWorld.unload();
            this.activeGameWorld = null;
        }

        LocationSafetyCache.clear();

        synchronized (this.stateLock)
        {
            this.currentMatchState = MatchState.ENDED;
            this.initializationState = GameInitializationState.NOT_STARTED;
            this.activeGameMode = null;
        }

        HuntedPlugin.LOGGER.info("Successfully the current game!");
    }

    /**
     * Part 1: Create the game world. This is where all gameplay will happen during the course of the game.
     */
    private void initializeGameWorld()
    {
        synchronized (this.stateLock)
        {
            this.initializationState = GameInitializationState.INITIALIZING_WORLD;
        }

        this.activeGameWorld = new GameWorld(String.format("huntedGameWorld_active_%s", UUID.randomUUID()));
        this.activeGameWorld.create();
    }

    /**
     * Part 2: Given a 1000 block radius around the spawn of the game world, find all biomes within that area.
     *
     * @return A {@link CompletableFuture} containing the result of the biome scan
     */
    private CompletableFuture<Map<Biome, List<Location>>> scanSpawnBiomesAsync()
    {
        synchronized (this.stateLock)
        {
            this.initializationState = GameInitializationState.SCANNING_BIOMES;
        }

        World gameWorldBukkitHandle = this.activeGameWorld.getBukkitWorld(World.Environment.NORMAL);

        return WorldUtils.getBiomesAroundSpawnAsync(gameWorldBukkitHandle, 1000, 32);
    }

    /**
     * Part 3: Given the biomes found from the scan done in the previous part, randomly determine a biome that each team will spawn in using a weight-based approach
     * where biomes with more available locations are preferred more than those with less.
     *
     * @param availableBiomes The available biomes found from the aforementioned scan
     * @return A {@link CompletableFuture} containing the results of the biome selection
     */
    private CompletableFuture<TeamSpawnBiomeSelector.BiomeSelectionResult> selectTeamSpawnBiomes(Map<Biome, List<Location>> availableBiomes)
    {
        synchronized (this.stateLock)
        {
            this.initializationState = GameInitializationState.SELECTING_SPAWN_BIOMES;
        }

        Set<Biome> disallowedSpawnBiomes = this.matchSettings.disallowedSpawnBiomes.getValue();
        World gameWorldBukkitHandle = this.activeGameWorld.getBukkitWorld(World.Environment.NORMAL);

        CompletableFuture<TeamSpawnBiomeSelector.BiomeSelectionResult> result = new CompletableFuture<>();

        TeamSpawnBiomeSelector.selectSpawnBiomesAsync(availableBiomes, disallowedSpawnBiomes, gameWorldBukkitHandle, result::complete);

        return result;
    }

    /**
     * Part 4: Now that we've finished all world related operations, we can continue with game initialization
     * on the main server thread.
     */
    private void completeInitializationOnMainThread(TeamSpawnBiomeSelector.BiomeSelectionResult biomeSelectionResult)
    {
        Bukkit.getScheduler().runTask(this.plugin, () ->
        {
            synchronized (this.stateLock)
            {
                this.initializationState = GameInitializationState.PREPARING_PLAYERS;
            }

            this.transportPlayersToGameWorld(biomeSelectionResult);

            // TODO: Perform the following when preparing to start a new game
            // - Transport all players to said world (Done) // STILL NEED TO TEST THIS!!!!!!!!!
            // - Restrict all player movement (Done)
            // - Start a countdown

            synchronized (this.stateLock)
            {
                this.initializationState = GameInitializationState.STARTING_COUNTDOWN;
            }

            // TODO: Perform the following once the countdown ends and the game begins (X = Completed):
            // - Unrestrict all player movement (Done)
            // - Pass all control to the game mode so that it can do whatever other setup it needs to so that it can be played properly.

            synchronized (this.stateLock)
            {
                this.initializationState = GameInitializationState.STARTED;
            }
        });
    }

    /**
     * Teleports teams to their randomly selected spawns from the spawn biome selection process.
     * Spectators are left with either team at random.
     */
    private void transportPlayersToGameWorld(TeamSpawnBiomeSelector.BiomeSelectionResult biomeSelectionResult)
    {
        List<Player> hunters = Teams.HUNTERS.getHandle().getMembers();
        List<Player> survivors = Teams.SURVIVORS.getHandle().getMembers();
        List<Player> spectators = Teams.SURVIVORS.getHandle().getMembers();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        Location hunterSpawnLocation = biomeSelectionResult.hunterSpawnLocation();
        Location survivorSpawnLocation = biomeSelectionResult.survivorSpawnLocation();

        // Add an offset to the spawn locations so that players don't all spawn on the same block.
        // This is also done for the survivors, but not the spectators.
        this.teleportPlayersWithRandomOffset(hunters, hunterSpawnLocation, threadLocalRandom);
        this.teleportPlayersWithRandomOffset(survivors, survivorSpawnLocation, threadLocalRandom);

        // Randomly teleport spectators to either team with no random offset
        for (Player spectator : spectators)
            this.activeGameWorld.teleportPlayer(spectator, threadLocalRandom.nextBoolean() ? hunterSpawnLocation : survivorSpawnLocation);
    }

    private void teleportPlayersWithRandomOffset(List<Player> players, Location baseLocation, ThreadLocalRandom random)
    {
        // (Ayydxn) Maybe make this configurable?
        int maxOffset = 15;

        for (Player player : players)
        {
            double offset = Math.floor(random.nextDouble(maxOffset)); // Each player will have a different offset
            Location finalLocation = baseLocation.add(offset + 0.5d, 0.0d, offset + 0.5d); // Add 0.5 so we're in the center of the block

            this.activeGameWorld.teleportPlayer(player, finalLocation);
        }
    }

    private Void onInitializationError(Throwable throwable)
    {
        Bukkit.getScheduler().runTask(this.plugin, () ->
        {
            synchronized (this.stateLock)
            {
                this.initializationState = GameInitializationState.FAILED;
                this.currentMatchState = MatchState.ENDED;
            }

            HuntedPlugin.LOGGER.error("Game initialization failed!", throwable);

            this.cleanupFailedInitialization();

            Component errorMessage = Component.text("Failed to start the game. Refer to the console for more details and please try again")
                            .color(NamedTextColor.RED);

            ServerUtils.broadcastMessage(this.plugin, errorMessage);
        });

        return null;
    }

    private void cleanupFailedInitialization()
    {
        HuntedPlugin.LOGGER.info("Cleaning created resources from the failed initialization...");

        if (this.activeGameWorld != null)
        {
            this.activeGameWorld.unload();
            this.activeGameWorld = null;
        }

        if (this.activeGameMode != null)
        {
            this.activeGameMode.onEnd();
            this.activeGameMode = null;
        }

        this.teamManager.clearTeams();
        this.matchSettings = HuntedMatchSettings.defaults();
    }

    public TeamManager getTeamManager()
    {
        return this.teamManager;
    }

    public HuntedMatchSettings getMatchSettings()
    {
        return this.matchSettings;
    }

    public MatchState getCurrentMatchState()
    {
        return this.currentMatchState;
    }

    public GameInitializationState getInitializationState()
    {
        return this.initializationState;
    }
}
