package me.ayydxn.hunted.game.world;

import com.google.common.collect.Sets;
import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.world.BiomeUtils;
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
 * Represents the world on which each match of Hunted is played on.
 * <p>
 * Each of these worlds is temporary and are disposed of once the match using them has ended.
 */
public class GameWorld
{
    private static final Set<GameWorld> WORLD_DELETION_QUEUE = Sets.newConcurrentHashSet();

    private final MultiverseCoreApi multiverseCoreApi;
    private final WorldManager worldManager;
    private final String name;

    private LoadedMultiverseWorld overworldWorld;
    private LoadedMultiverseWorld netherWorld;
    private LoadedMultiverseWorld theEndWorld;
    private Runnable worldDeletionTask;

    public GameWorld(String name)
    {
        this.multiverseCoreApi = MultiverseCoreApi.get();
        this.worldManager = this.multiverseCoreApi.getWorldManager();
        this.name = name;
    }

    public static void clearWorldDeletionQueue()
    {
        if (WORLD_DELETION_QUEUE.isEmpty())
            return;

        for (GameWorld gameWorld : WORLD_DELETION_QUEUE)
            gameWorld.worldDeletionTask.run();
    }

    public void create()
    {
        this.overworldWorld = this.createWorld(World.Environment.NORMAL);
        this.netherWorld = this.createWorld(World.Environment.NETHER);
        this.theEndWorld = this.createWorld(World.Environment.THE_END);

        this.worldDeletionTask = () ->
        {
            this.worldManager.deleteWorld(DeleteWorldOptions.world(this.overworldWorld));
            this.worldManager.deleteWorld(DeleteWorldOptions.world(this.netherWorld));
            this.worldManager.deleteWorld(DeleteWorldOptions.world(this.theEndWorld));
        };

        MultiverseNetherPortals mvNetherPortals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
        Objects.requireNonNull(mvNetherPortals);

        Validate.isTrue(mvNetherPortals.addWorldLink(this.overworldWorld.getName(), this.netherWorld.getName(), PortalType.NETHER));
        Validate.isTrue(mvNetherPortals.addWorldLink(this.netherWorld.getName(), this.overworldWorld.getName(), PortalType.NETHER));

        Validate.isTrue(mvNetherPortals.addWorldLink(this.overworldWorld.getName(), this.theEndWorld.getName(), PortalType.ENDER));
        Validate.isTrue(mvNetherPortals.addWorldLink(this.theEndWorld.getName(), this.overworldWorld.getName(), PortalType.ENDER));

        HuntedPlugin.LOGGER.info("Successfully created game world '{}'!", this.name);
    }

    public void unload()
    {
        // We can only delete worlds when the server is shutting down or else, if we try to do it while the server is still running,
        // it'll get stuck waiting until it can delete the worlds. So, we add the current instance to a queue so that we can delete it when the server is shutting down.
        WORLD_DELETION_QUEUE.add(this);
    }

    public void teleportPlayers(List<Player> players, Location destination)
    {
        HuntedPlugin.LOGGER.info("Teleporting {} players...", players.size());

        players.forEach(player -> this.teleportPlayer(player, destination));
    }

    public void teleportPlayer(Player player, Location destination)
    {
        this.multiverseCoreApi.getSafetyTeleporter().to(destination)
                .checkSafety(true)
                .teleportSingle(player)
                .onFailureCount(teleportFailureReasonMap ->
                {
                    for (var entry : teleportFailureReasonMap.entrySet())
                        HuntedPlugin.LOGGER.error("Failed to teleport player to {}: {}", destination, entry.getKey());
                });
    }

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

        newWorld.setKeepSpawnInMemory(true);
        newWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new)
                .setAutoSave(false);

        return newWorld;
    }

    public World getBukkitWorld(World.Environment environment)
    {
        return switch (environment)
        {
            case NORMAL -> this.overworldWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new);
            case NETHER -> this.netherWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new);
            case THE_END -> this.theEndWorld.getBukkitWorld().getOrElseThrow(NullPointerException::new);
            default ->
                    throw new IllegalArgumentException(String.format("Cannot get Bukkit world for environment '%s'!", environment.name()));
        };
    }
}
