package me.ayydxn.hunted.game.world;

import com.google.common.collect.Sets;
import me.ayydxn.hunted.HuntedPlugin;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the world on which each match of Hunted is played on. Each of these consists of 3 {@link LoadedMultiverseWorld} instances for an Overworld,
 * Nether and The End.
 * <p>
 * Using the Multiverse plugin APIs, the class handles the creation, teleportation of players and deferred deletion of the worlds as well as correctly
 * linking the Nether and End portals between them.
 * <p>
 * To avoid unsafe world deletions during runtime, game world deletions are deferred by adding worlds to a global queue and invoking
 * {@link GameWorld#clearWorldDeletionQueue()} when the server is shutting down.
 */
public class GameWorld
{
    // Global queue of game worlds to delete when the server is shutting down.
    private static final Set<GameWorld> WORLD_DELETION_QUEUE = Sets.newConcurrentHashSet();

    private final MultiverseCoreApi multiverseCoreApi;
    private final WorldManager worldManager;
    private final String name;

    private LoadedMultiverseWorld overworldWorld;
    private LoadedMultiverseWorld netherWorld;
    private LoadedMultiverseWorld theEndWorld;

    public GameWorld(String name)
    {
        this.multiverseCoreApi = MultiverseCoreApi.get();
        this.worldManager = this.multiverseCoreApi.getWorldManager();
        this.name = name;
    }

    /**
     * Deletes all the game worlds in the global deletion queue by running their individual deletion tasks.
     * <p>
     * This function should only be called when the server is shutting down to avoid issues with deleting worlds that are actively being used.
     */
    public static void clearWorldDeletionQueue()
    {
        if (WORLD_DELETION_QUEUE.isEmpty())
            return;

        for (GameWorld gameWorld : WORLD_DELETION_QUEUE)
            gameWorld.delete();
    }

    /**
     * Creates a new world with the underlying {@link LoadedMultiverseWorld} instances for an Overworld, Nether and The End.
     */
    public void create()
    {
        this.overworldWorld = this.createWorld(World.Environment.NORMAL);
        this.netherWorld = this.createWorld(World.Environment.NETHER);
        this.theEndWorld = this.createWorld(World.Environment.THE_END);

        // Using the Multiverse-NetherPortals plugin, we link three worlds together via their respective portals.
        MultiverseNetherPortals mvNetherPortals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
        Objects.requireNonNull(mvNetherPortals);

        Validate.isTrue(mvNetherPortals.addWorldLink(this.overworldWorld.getName(), this.netherWorld.getName(), PortalType.NETHER));
        Validate.isTrue(mvNetherPortals.addWorldLink(this.netherWorld.getName(), this.overworldWorld.getName(), PortalType.NETHER));

        Validate.isTrue(mvNetherPortals.addWorldLink(this.overworldWorld.getName(), this.theEndWorld.getName(), PortalType.ENDER));
        Validate.isTrue(mvNetherPortals.addWorldLink(this.theEndWorld.getName(), this.overworldWorld.getName(), PortalType.ENDER));

        HuntedPlugin.LOGGER.info("Successfully created game world '{}'!", this.name);
    }

    /**
     * Unloads this game world by adding it to a global deletion queue.
     * <p>
     * This function should only be called when a game has ended and all players have been teleported back to the server's original world.
     * The actual deletion of the world will occur in {@link GameWorld#clearWorldDeletionQueue()}
     */
    public void unload()
    {
        // We can only delete worlds when the server is shutting down or else, if we try to do it while the server is still running,
        // it'll get stuck waiting until it can delete the worlds. So, we add the current instance to a queue so that we can delete it when the server is shutting down.
        WORLD_DELETION_QUEUE.add(this);
    }

    /**
     * Teleports a list of players to a given location using Multiverse's safety teleporter
     *
     * @param players The players who will be teleported
     * @param destination Where the players will the teleported to
     */
    public void teleportPlayers(List<Player> players, Location destination)
    {
        HuntedPlugin.LOGGER.info("Teleporting {} players...", players.size());

        players.forEach(player -> this.teleportPlayer(player, destination));
    }

    /**
     * Teleports a single player to a given location using Multiverse's safety teleporter
     *
     * @param player The player who will be teleported
     * @param destination Where the player will the teleported to
     */
    public void teleportPlayer(Player player, Location destination)
    {
        this.multiverseCoreApi.getSafetyTeleporter().to(destination)
                .checkSafety(false)
                .teleportSingle(player)
                .onFailureCount(teleportFailureReasonMap ->
                {
                    for (var entry : teleportFailureReasonMap.entrySet())
                        HuntedPlugin.LOGGER.error("Failed to teleport player to {}: {}", destination, entry.getKey());
                });
    }

    /**
     * Deletes all the worlds off the disk.
     * <p>
     * This function is called during server shutdown when iterating the world deletion queue and can be overridden to allow for custom deletion behavior.
     */
    protected void delete()
    {
        this.worldManager.deleteWorld(DeleteWorldOptions.world(this.overworldWorld));
        this.worldManager.deleteWorld(DeleteWorldOptions.world(this.netherWorld));
        this.worldManager.deleteWorld(DeleteWorldOptions.world(this.theEndWorld));
    }

    /**
     * Creates a Multiverse world, making it an overworld, nether or end based on the provided {@link World.Environment} argument.
     *
     * @param environment The world's environment (Overworld, Nether, The End, etc.)
     * @return A new {@link LoadedMultiverseWorld} of the given environment
     */
    private LoadedMultiverseWorld createWorld(World.Environment environment)
    {
        String worldNameExtension = switch (environment)
        {
            case NORMAL -> "overworld";
            case NETHER -> "nether";
            case THE_END -> "the_end";
            case CUSTOM -> "custom";
        };

        CreateWorldOptions worldCreationOptions = CreateWorldOptions.worldName(this.name + "_" + worldNameExtension)
                .environment(environment);

        LoadedMultiverseWorld newWorld = this.worldManager.createWorld(worldCreationOptions)
                .onFailure(reasonFailure -> HuntedPlugin.LOGGER.error(reasonFailure.getFailureMessage().raw()))
                .get();

        // Keep the world's spawn chunks loaded in memory
        newWorld.setKeepSpawnInMemory(true);

        // Disable auto save
        newWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new)
                .setAutoSave(false);

        return newWorld;
    }

    /**
     * Returns the {@link World Bukkit world} from the corresponding {@link LoadedMultiverseWorld Multiverse world} based on the specified environment.
     *
     * @param environment The world environment to get
     * @return The Bukkit world of the Multiverse world for that environment
     * @throws NullPointerException If (somehow?) there is no Bukkit world for the Multiverse world
     * @throws IllegalArgumentException If an unsupported {@link World.Environment} enumeration was used.
     */
    public World getBukkitWorld(World.Environment environment)
    {
        return switch (environment)
        {
            case NORMAL -> this.overworldWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new);
            case NETHER -> this.netherWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new);
            case THE_END -> this.theEndWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new);
            default -> throw new IllegalArgumentException(String.format("Cannot get Bukkit world for environment '%s'!", environment.name()));
        };
    }
}
