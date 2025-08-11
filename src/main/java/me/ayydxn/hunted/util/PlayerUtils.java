package me.ayydxn.hunted.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * A class which contains utility functions to relating to {@link Player}s.
 */
public class PlayerUtils
{
    public static void allowPlayerFlight(Player player, boolean allowFlight)
    {
        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight);
        player.setGravity(!allowFlight);
    }

    public static boolean isPlayerGrounded(Player player)
    {
        Location playerLocation = player.getLocation();
        Block blockBelowPlayer = player.getWorld().getBlockAt(playerLocation).getRelative(0, -1, 0);

        return blockBelowPlayer.getType() == Material.AIR || blockBelowPlayer.isSolid();
    }
}
