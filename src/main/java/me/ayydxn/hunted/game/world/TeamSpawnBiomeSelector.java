package me.ayydxn.hunted.game.world;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.world.WorldUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Responsible for randomly selecting a biome that will spawn in after performing a biome scan using {@link WorldUtils#getBiomesAroundSpawnAsync(World, int, int)}
 */
public class TeamSpawnBiomeSelector
{
    /**
     * Selects spawn biomes for teams asynchronously based on world biome data and game settings.
     * <p>
     * This method is intended to be used in combination with {@link WorldUtils#getBiomesAroundSpawnAsync(World, int, int)}
     * and thus, processes the biome data from the world scan, filters valid biomes according to the specified set,
     * and uses weighted selection to choose appropriate spawn biomes for each team. The result is delivered through the provided callback.</p>
     *
     * @param availableBiomes        A map of biomes to their sampled locations from world scanning
     * @param disallowedBiomes       A set of biomes that should not be used for spawning
     * @param world                  The world instance for fallback spawn location
     * @param biomeSelectionCallback The callback to receive the selection results
     * @return A CompletableFuture that completes when biome selection is finished
     */
    public static CompletableFuture<Void> selectSpawnBiomesAsync(Map<Biome, List<Location>> availableBiomes, Set<Biome> disallowedBiomes, World world,
                                                                 BiomeSelectionCallback biomeSelectionCallback)
    {
        return CompletableFuture.runAsync(() ->
        {
            BiomeSelectionResult biomeSelectionResult = TeamSpawnBiomeSelector.selectSpawnBiomes(availableBiomes, disallowedBiomes, world);
            biomeSelectionCallback.onBiomesSelected(biomeSelectionResult);
        });
    }

    /**
     * Selects spawn biomes for teams based on a set of available biomes and set of disallowed biomes.
     * <p>
     * This method implements the core selection logic including biome filtering, weighted selection, and team differentiation.
     * It handles edge cases such as no valid biomes or single biome availability.
     *
     * @param availableBiomes  A map of biomes to their sampled locations from a world scan
     * @param disallowedBiomes A set of biomes that should not be used for spawning
     * @param world            The world instance for fallback spawn location
     * @return A {@link BiomeSelectionResult} containing the selected biomes and locations
     */
    private static BiomeSelectionResult selectSpawnBiomes(Map<Biome, List<Location>> availableBiomes, Set<Biome> disallowedBiomes, World world)
    {
        long startTime = System.nanoTime();

        List<BiomeCandidate> validSpawnBiomes = TeamSpawnBiomeSelector.filterValidBiomes(availableBiomes, disallowedBiomes);
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

        if (validSpawnBiomes.isEmpty())
        {
            HuntedPlugin.LOGGER.warn("No valid spawn biome were found! Using world spawn as a fallback...");
            return TeamSpawnBiomeSelector.createFallbackResult(world);
        }

        validSpawnBiomes.sort((biomeCandidate1, biomeCandidate2) ->
                Integer.compare(biomeCandidate2.locations.size(), biomeCandidate1.locations.size()));

        BiomeCandidate hunterSpawnBiomeCandidate = TeamSpawnBiomeSelector.selectWeightedBiome(validSpawnBiomes, threadLocalRandom);
        Location hunterSpawnLocation = TeamSpawnBiomeSelector.selectRandomLocation(hunterSpawnBiomeCandidate.locations, threadLocalRandom);

        BiomeCandidate survivorSpawnBiomeCandidate = TeamSpawnBiomeSelector.selectWeightedBiomeExcept(validSpawnBiomes, hunterSpawnBiomeCandidate, threadLocalRandom);
        Location survivorSpawnLocation = TeamSpawnBiomeSelector.selectRandomLocation(survivorSpawnBiomeCandidate.locations, threadLocalRandom);

        long endTime = System.nanoTime();
        double executionTime = (double) (endTime - startTime) / 1_000_000_000.0;

        BiomeSelectionResult biomeSelectionResult = new BiomeSelectionResult(hunterSpawnBiomeCandidate.biome, hunterSpawnLocation,
                survivorSpawnBiomeCandidate.biome, survivorSpawnLocation, validSpawnBiomes.size(), executionTime);

        TeamSpawnBiomeSelector.logSelectionResult(biomeSelectionResult, hunterSpawnBiomeCandidate, survivorSpawnBiomeCandidate);

        return biomeSelectionResult;
    }

    /**
     * Filters the available biomes to only include valid spawn candidates.
     *
     * @param availableBiomes  A map of all available biomes and their locations
     * @param disallowedBiomes A set of biomes to exclude from selection
     * @return A list of valid biome candidates for spawning
     */
    private static List<BiomeCandidate> filterValidBiomes(Map<Biome, List<Location>> availableBiomes, Set<Biome> disallowedBiomes)
    {

        return availableBiomes.entrySet()
                .stream()
                .filter(entry -> !disallowedBiomes.contains(entry.getKey())).filter(entry -> !entry.getValue().isEmpty()) // Ensure biome has valid locations
                .map(entry -> new BiomeCandidate(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Selects a biome using weighted random selection based on location availability.
     * This means that biomes with more available locations have a higher chance of being selected.
     *
     * @param biomeCandidates A list of biome candidates sorted by location count (descending)
     * @param random          The random number generator to use
     * @return The randomly selected biome candidate
     */
    private static BiomeCandidate selectWeightedBiome(List<BiomeCandidate> biomeCandidates, ThreadLocalRandom random)
    {
        // Calculate total weight (for each biome candidate, get the number of locations and add them all together)
        int totalWeight = biomeCandidates.stream()
                .mapToInt(candidate -> candidate.locations.size())
                .sum();
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (BiomeCandidate biomeCandidate : biomeCandidates)
        {
            currentWeight += biomeCandidate.locations.size();
            if (randomWeight < currentWeight)
                return biomeCandidate;
        }

        // Fallback (shouldn't happen with valid input)
        return biomeCandidates.getFirst();
    }

    /**
     * Selects a different biome from the excluded one, preferring weighted selection.
     * If only one biome is available, the biome that was to be excluded is returned.
     *
     * @param biomeCandidates         all available biome candidates
     * @param biomeCandidateException the biome candidate to avoid if possible
     * @param random                  the random number generator to use
     * @return a biome candidate, preferably different from the excluded one
     */
    private static BiomeCandidate selectWeightedBiomeExcept(List<BiomeCandidate> biomeCandidates, BiomeCandidate biomeCandidateException, ThreadLocalRandom random)
    {

        List<BiomeCandidate> alternativeCandidates = biomeCandidates.stream()
                .filter(candidate -> !candidate.biome.equals(biomeCandidateException.biome))
                .collect(Collectors.toList());

        if (alternativeCandidates.isEmpty())
        {
            HuntedPlugin.LOGGER.info("Only one valid spawn biome available, using same biome for both teams");
            return biomeCandidateException;
        }

        return TeamSpawnBiomeSelector.selectWeightedBiome(alternativeCandidates, random);
    }

    /**
     * Selects a random location from the provided list of locations.
     *
     * @param locations list of available locations
     * @param random    the random number generator to use
     * @return a randomly selected location
     */
    private static Location selectRandomLocation(List<Location> locations, ThreadLocalRandom random)
    {
        return locations.get(random.nextInt(locations.size()));
    }

    /**
     * Creates a fallback result when no valid spawn biomes are found.
     * Uses the world spawn location as the fallback for both teams.
     *
     * @param world the world to get the spawn location from
     * @return a fallback BiomeSelectionResult using world spawn
     */
    private static BiomeSelectionResult createFallbackResult(World world)
    {
        Location worldSpawn = world.getSpawnLocation();
        Biome fallbackBiome = world.getBiome(worldSpawn);

        HuntedPlugin.LOGGER.info("Using fallback spawn biome '{}' at the world's spawn", fallbackBiome.getKey().getKey());

        return new BiomeSelectionResult(fallbackBiome, worldSpawn, fallbackBiome, worldSpawn, 0, 0.0d);
    }

    /**
     * Logs the biome selection results.
     *
     * @param biomeSelectionResult   The selection result to log
     * @param hunterSpawnCandidate   The hunter spawn biome candidate with location details
     * @param survivorSpawnCandidate The survivor spawn biome candidate with location details
     */
    private static void logSelectionResult(BiomeSelectionResult biomeSelectionResult, BiomeCandidate hunterSpawnCandidate, BiomeCandidate survivorSpawnCandidate)
    {
        HuntedPlugin.LOGGER.info("Selected Hunters spawn biome '{}' at {}, {}, {} (from {} available locations)",
                biomeSelectionResult.hunterSpawnBiome.getKey().getKey(),
                biomeSelectionResult.hunterSpawnLocation.getBlockX(),
                biomeSelectionResult.hunterSpawnLocation.getBlockY(),
                biomeSelectionResult.hunterSpawnLocation.getBlockZ(),
                hunterSpawnCandidate.locations.size());

        HuntedPlugin.LOGGER.info("Selected Survivors spawn biome '{}' at {}, {}, {} (from {} available locations)",
                biomeSelectionResult.survivorSpawnBiome.getKey().getKey(),
                biomeSelectionResult.survivorSpawnLocation.getBlockX(),
                biomeSelectionResult.survivorSpawnLocation.getBlockY(),
                biomeSelectionResult.survivorSpawnLocation.getBlockZ(),
                survivorSpawnCandidate.locations.size());

        HuntedPlugin.LOGGER.info("Biome selection took {} seconds", biomeSelectionResult.executionTime);
    }

    /**
     * Represents the result of a biome selection operation, containing the selected
     *
     * @param hunterSpawnBiome      The biome in which the hunters will spawn
     * @param hunterSpawnLocation   The location at which the hunters will spawn within their spawn biome
     * @param survivorSpawnBiome    The biome in which the survivors will spawn
     * @param survivorSpawnLocation The location at which the survivors will spawn within their spawn biome
     * @param totalValidBiomes      The total number of biomes that were valid as spawn locations
     * @param executionTime         The time it took (in seconds) for the selection to execute
     */
    public record BiomeSelectionResult(Biome hunterSpawnBiome, Location hunterSpawnLocation, Biome survivorSpawnBiome,
                                       Location survivorSpawnLocation,
                                       int totalValidBiomes, double executionTime)
    {
    }

    /**
     * Callback interface for receiving biome selection results asynchronously.
     */
    @FunctionalInterface
    public interface BiomeSelectionCallback
    {
        /**
         * Called when biome selection has completed.
         *
         * @param biomeSelectionResult the result of the biome selection process
         */
        void onBiomesSelected(BiomeSelectionResult biomeSelectionResult);
    }

    /**
     * A helper class to encapsulate a potential spawn biome and its available spawn locations.
     *
     * @param biome     The spawn biome
     * @param locations All possible spawn locations within that biome
     */
    private record BiomeCandidate(Biome biome, List<Location> locations)
    {
    }
}
