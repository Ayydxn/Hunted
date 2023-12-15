package me.ayydan.hunted.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlayerUtils
{
    @Nullable
    public static Player getNearestPlayerFromPlayer(Player targetPlayer)
    {
        Player nearestPlayer = null;
        double maxNearestPlayerDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if (player.getWorld().getEnvironment() != targetPlayer.getWorld().getEnvironment())
                break;

            if (player != targetPlayer)
            {
                double distanceBetweenThem = player.getLocation().distance(targetPlayer.getLocation());

                if (distanceBetweenThem < maxNearestPlayerDistance)
                {
                    nearestPlayer = player;
                    maxNearestPlayerDistance = distanceBetweenThem;
                }
            }
        }

        return nearestPlayer;
    }
}
