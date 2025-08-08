package me.ayydxn.hunted.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.ayydxn.hunted.HuntedPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A class which contains utility functions to relating to {@link World}s.
 */
public final class WorldUtils
{
    /**
     * Asynchronously scans the biomes in a radius around the world's spawn point, sampling at the given horizontal step interval without loading any chunks.
     * <p>
     * This method uses {@link World#getBiome(Location)} to directly access biome data without triggering chunk loads, making it significantly faster than chunk-based scanning.
     * <p>
     * The scan operates in a square from (spawnX - radius, spawnZ - radius) to (spawnX + radius, spawnZ + radius), sampling every {@code step} blocks along both axes.
     * It uses a fixed Y value (64) for biome lookups, which is enough in most cases where vertical biome variation is not significant.
     * <p>
     * The result includes not just the unique biomes found, but also the exact {@link Location}s
     * at which each biome was sampled. This allows later usage of specific positions within each biome.
     * <p>
     * This method is executed asynchronously to avoid blocking the main server thread.
     * Completion is logged with performance metrics and a breakdown of found biomes and locations.
     *
     * @param world  The {@link World} in which to perform the scan
     * @param radius The scan radius in blocks from the spawn location (total area = (2 * radius)^2)
     * @param step   The sampling step size in blocks (e.g., 32 scans every 32 blocks)
     * @return A {@link CompletableFuture} that completes with a {@link Map} mapping each {@link Biome} to a list of {@link Location}s where it was found during the scan
     */
    public static CompletableFuture<Map<Biome, List<Location>>> getBiomesAroundSpawnAsync(World world, int radius, int step)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            long scanStartTime = System.nanoTime();

            Map<Biome, List<Location>> biomeLocations = Maps.newConcurrentMap();
            Location spawnLocation = world.getSpawnLocation();
            int centerX = spawnLocation.getBlockX();
            int centerZ = spawnLocation.getBlockZ();
            int numberOfSamplesPerAxis = Math.floorDiv(radius * 2, step) + 1;
            int numberOfScannedPositions = numberOfSamplesPerAxis * numberOfSamplesPerAxis;

            for (int x = centerX - radius; x <= centerX + radius; x += step)
            {
                for (int z = centerZ - radius; z <= centerZ + radius; z += step)
                {
                    // Use Y = 64 as a default mid-ground value (since biomes are usually the same height vertically)
                    Location sampleLocation = new Location(world, x, 64, z);
                    Biome biome = world.getBiome(sampleLocation);

                    biomeLocations.computeIfAbsent(biome, k -> Collections.synchronizedList(Lists.newArrayList()))
                            .add(sampleLocation);
                }
            }

            long scanEndTime = System.nanoTime();
            double elapsedSeconds = (scanEndTime - scanStartTime) / 1_000_000_000.0;

            HuntedPlugin.LOGGER.info("Completed biome scan in {} seconds ({} unique biomes, {} positions scanned)", elapsedSeconds,
                    biomeLocations.size(), numberOfScannedPositions);

            biomeLocations.forEach((biome, locations) -> HuntedPlugin.LOGGER.info("- {} ({} sampled locations)", biome.getKey().getKey(),
                    locations.size()));

            return biomeLocations;
        });
    }
}
