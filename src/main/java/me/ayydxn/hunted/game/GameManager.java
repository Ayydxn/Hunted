package me.ayydxn.hunted.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.config.HuntedMatchSettings;
import me.ayydxn.hunted.game.world.GameWorld;
import me.ayydxn.hunted.game.world.TeamSpawnBiomeSelector;
import me.ayydxn.hunted.tasks.GameTickTask;
import me.ayydxn.hunted.tasks.countdown.GameStartCountdownTask;
import me.ayydxn.hunted.teams.TeamManager;
import me.ayydxn.hunted.teams.Teams;
import me.ayydxn.hunted.util.LocationSafetyCache;
import me.ayydxn.hunted.util.PlayerUtils;
import me.ayydxn.hunted.util.ServerUtils;
import me.ayydxn.hunted.util.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;

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
public class GameManager implements Listener
{
    private final HuntedPlugin plugin;
    private final TeamManager teamManager;
    private final Object stateLock = new Object();

    // Game state
    private final Set<Player> readyPlayers = Sets.newConcurrentHashSet();
    private final Map<Player, Integer> playerLoadedChunks = Maps.newHashMap();
    private HuntedMatchSettings matchSettings;
    private MatchState currentMatchState;
    private GameInitializationState initializationState;

    // Game components
    private GameWorld activeGameWorld;
    private HuntedGameMode activeGameMode;
    private GameTickTask gameTickTask;

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

        if (this.gameTickTask != null)
            this.gameTickTask = null;

        LocationSafetyCache.clear();

        // Teleport players back to the default server world
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
        {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                this.transportBackToMainWorld(onlinePlayer);

        }, 100L);

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

        HuntedPlugin.LOGGER.info("Determining team spawn biomes...");

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
            this.waitUntilAllPlayersAreReady();
        });
    }

    /**
     * Teleports teams to their randomly selected spawns from the spawn biome selection process.
     * Spectators are left with either team at random.
     */
    private void transportPlayersToGameWorld(TeamSpawnBiomeSelector.BiomeSelectionResult biomeSelectionResult)
    {
        HuntedPlugin.LOGGER.info("Transporting players to game world...");

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

    /**
     * Waits until all players are ready before starting the countdown
     *
     * @see GameManager#onPlayerChunkLoad(PlayerChunkLoadEvent)
     */
    private void waitUntilAllPlayersAreReady()
    {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    private void startGameCountdown()
    {
        synchronized (this.stateLock)
        {
            this.initializationState = GameInitializationState.STARTING_COUNTDOWN;
        }

        HuntedPlugin.LOGGER.info("Starting game countdown...");

        GameStartCountdownTask gameStartCountdownTask = new GameStartCountdownTask(this, this::onGameCountdownComplete);

        Bukkit.getScheduler().runTaskTimer(this.plugin, gameStartCountdownTask, 0L, 20L);
    }

    private void onGameCountdownComplete()
    {
        synchronized (this.stateLock)
        {
            if (this.initializationState != GameInitializationState.STARTING_COUNTDOWN)
            {
                HuntedPlugin.LOGGER.info("Game started countdown ended in an unexpected state ({})", this.initializationState);
                return;
            }

            this.initializationState = GameInitializationState.COMPLETE;
            this.currentMatchState = MatchState.ACTIVE;

            var activePlayers = ImmutableList.<Player>builder()
                    .addAll(Teams.HUNTERS.getHandle().getMembers())
                    .addAll(Teams.SURVIVORS.getHandle().getMembers())
                    .build();

            for (Player player : activePlayers)
                PlayerUtils.allowPlayerFlight(player, false);

            HuntedPlugin.LOGGER.info("Game initialization successfully completed! Beginning active gameplay...");

            this.gameTickTask = new GameTickTask(this);
            Bukkit.getScheduler().runTaskTimer(this.plugin, gameTickTask, 0L, 100L);

            this.activeGameMode.onStart();
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

        if (this.gameTickTask != null)
            this.gameTickTask = null;

        this.teamManager.clearTeams();
        this.matchSettings = HuntedMatchSettings.defaults();
    }

    /**
     * Teleports a given player back to spawn location of the main server world (aka the world that the server makes when you run it and there is no world)
     *
     * @param player The player to teleport
     */
    private void transportBackToMainWorld(Player player)
    {
        MultiverseCoreApi multiverseCoreApi = MultiverseCoreApi.get();
        AsyncSafetyTeleporter safetyTeleporter = multiverseCoreApi.getSafetyTeleporter();
        World mainWorld = Bukkit.getWorlds().getFirst(); // TODO: (Ayydxn) Make this configurable. Can't always assume it's the first world.
        Location mainWorldSpawnLocation = mainWorld.getSpawnLocation();

        safetyTeleporter.to(mainWorldSpawnLocation)
                .checkSafety(false)
                .teleportSingle(player)
                .onFailureCount(teleportFailureReasonMap ->
                {
                    for (var entry : teleportFailureReasonMap.entrySet())
                        HuntedPlugin.LOGGER.error("Failed to teleport player to {}: {}", mainWorldSpawnLocation, entry.getKey());
                });
    }

    /**
     * We use players loading chunks as a method of checking if they are ready or not.
     * "Ready" meaning that they have fully loaded into the game world and can play the game.
     * <p>
     * Once all players are ready, we can start the countdown.
     */
    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent playerChunkLoadEvent)
    {
        if (this.initializationState !=  GameInitializationState.PREPARING_PLAYERS)
            return;

        if (playerChunkLoadEvent.getWorld() != this.activeGameWorld.getBukkitWorld(World.Environment.NORMAL))
            return;

        Player player = playerChunkLoadEvent.getPlayer();

        ImmutableList<Player> players = ImmutableList.<Player>builder()
                .addAll(Teams.HUNTERS.getHandle().getMembers())
                .addAll(Teams.SURVIVORS.getHandle().getMembers())
                .addAll(Teams.SPECTATORS.getHandle().getMembers())
                .build();

        if (!players.contains(player))
            return;

        int loadedChunkCount = this.playerLoadedChunks.merge(player, 1, Integer::sum);
        int minChunksNeeded = 10; // Minimum number of chunks that a player needs to load to be considered ready.

        if (loadedChunkCount >= minChunksNeeded)
        {
            this.readyPlayers.add(player);

            if (this.readyPlayers.size() == this.playerLoadedChunks.size())
            {
                Bukkit.getScheduler().runTask(this.plugin, this::startGameCountdown);

                // Don't need this event anymore, so we can unregister this class as a listener.
                PlayerChunkLoadEvent.getHandlerList().unregister(this);
            }
        }
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
