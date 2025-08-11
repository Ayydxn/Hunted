package me.ayydxn.hunted.util;

import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A simple cache system for storing whether locations in a world are safe for a player to be teleported to or not.
 * The main goal of this is to improve game startup times by eliminating repeated safety checks on the same location.
 * <p>
 * Additionally, it supports cache expiry where locations are removed from the cache if they have been in it for more than 5 minutes.
 */
public class LocationSafetyCache
{
    private static final Map<Location, Boolean> LOCATION_SAFETY_CACHE = Maps.newConcurrentMap();
    private static final Map<Location, Long> CACHE_TIMESTAMPS = Maps.newConcurrentMap();
    private static final long CACHE_EXPIRY_TIME = TimeUnit.MINUTES.toMillis(5);

    /**
     * Clears the entire cache, returning it to an empty state.
     */
    public static void clear()
    {
        LOCATION_SAFETY_CACHE.clear();
        CACHE_TIMESTAMPS.clear();
    }

    /**
     * Checks if a location is safe, returning its cached value if one is available.
     *
     * @param location The location to check
     * @return True if the location is safe to spawn at, false otherwise
     */
    public static boolean isLocationSafe(Location location)
    {
        Boolean cachedLocationSafety = LocationSafetyCache.getLocationSafetyFromCache(location);
        if (cachedLocationSafety != null)
        {
            return cachedLocationSafety;
        }
        else
        {
            boolean isLocationSafe = MultiverseCoreApi.get().getBlockSafety().canSpawnAtLocationSafely(location);

            LOCATION_SAFETY_CACHE.put(location, isLocationSafe);
            CACHE_TIMESTAMPS.put(location, System.currentTimeMillis());

            return isLocationSafe;
        }
    }

    /**
     * Gets cached safety status for a location from the cache, The location will be removed from the cache if it has been there for longer than the
     * specified {@link LocationSafetyCache#CACHE_EXPIRY_TIME cache expiry time}.
     *
     * @param location The location to check
     * @return Boolean safety status of the location or null if it was not cached or is expired
     */
    @Nullable
    private static Boolean getLocationSafetyFromCache(Location location)
    {
        // Check if this location entry in cache is expired and remove it if it is.
        Long timestamp = CACHE_TIMESTAMPS.get(location);
        if (timestamp != null && System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME)
        {
            LOCATION_SAFETY_CACHE.remove(location);
            CACHE_TIMESTAMPS.remove(location);

            return null;
        }

        return LOCATION_SAFETY_CACHE.get(location);
    }
}
