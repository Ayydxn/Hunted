package me.ayydan.hunted.item;

import me.ayydan.hunted.HuntedPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.joml.Math;

import java.util.ArrayList;

public class SurvivorTrackingCompassItem
{
    public static final Component DISPLAY_NAME = Component.text("Survivor Tracking Compass", NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD);

    private final ItemStack survivorTrackingCompassItem;
    private CompassMeta survivorTrackingCompassMeta;

    public SurvivorTrackingCompassItem()
    {
        this.survivorTrackingCompassItem = new ItemStack(Material.COMPASS);

        this.survivorTrackingCompassMeta = (CompassMeta) this.survivorTrackingCompassItem.getItemMeta();
        this.survivorTrackingCompassMeta.displayName(DISPLAY_NAME);
        this.survivorTrackingCompassMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

        this.survivorTrackingCompassItem.setItemMeta(survivorTrackingCompassMeta);
    }

    public void updateTrackingCompass(Player hunterUser)
    {
        if (hunterUser == null)
            return;

        boolean isUserASurvivor = HuntedPlugin.getInstance().getGameManager().getSurvivorsTeam().isPlayerInTeam(hunterUser);
        boolean isUserASpectator = HuntedPlugin.getInstance().getGameManager().getSpectatorsTeam().isPlayerInTeam(hunterUser);

        if (isUserASurvivor || isUserASpectator)
            return;

        Player nearestSurvivor = this.getNearestSurvivorToHunter(hunterUser);

        if (nearestSurvivor == null)
        {
            hunterUser.sendActionBar(Component.text("No survivors are nearby or at least one of them is too far away to be tracked!"));
            return;
        }

        this.survivorTrackingCompassMeta = (CompassMeta) this.survivorTrackingCompassItem.getItemMeta();
        this.survivorTrackingCompassMeta.displayName(DISPLAY_NAME);
        this.survivorTrackingCompassMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

        this.survivorTrackingCompassItem.setItemMeta(this.survivorTrackingCompassMeta);

        if (hunterUser.getInventory().getItemInMainHand().getType() == this.survivorTrackingCompassItem.getType())
        {
            int distanceDifference = (int) hunterUser.getLocation().distance(nearestSurvivor.getLocation());
            String nearestPlayerInfo = this.getNearestSurvivorLocationInfo(hunterUser, nearestSurvivor, distanceDifference);

            hunterUser.setCompassTarget(nearestSurvivor.getLocation());
            hunterUser.sendActionBar(Component.text(nearestPlayerInfo));
        }
    }

    private String getNearestSurvivorLocationInfo(Player hunterUser, Player nearestSurvivor, int distanceDifference)
    {
        int heightDifference = hunterUser.getLocation().getBlockY() - nearestSurvivor.getLocation().getBlockY();
        String heightDifferenceString = "Same Height";

        if (heightDifference > 0)
        {
            heightDifferenceString = String.format("%d Blocks Above", heightDifference);
        }
        else if (heightDifference < 0)
        {
            heightDifferenceString = String.format("%d Blocks Below", Math.abs(heightDifference));
        }

        return String.format("Nearest Player: %s | Distance: %d Blocks | Height: %s", nearestSurvivor.getName(), distanceDifference, heightDifferenceString);
    }

    public ItemStack getItemStack()
    {
        return this.survivorTrackingCompassItem;
    }

    private Player getNearestSurvivorToHunter(Player hunter)
    {
        Player nearestSurvivor = null;
        double nearestSurvivorDistance = Double.MAX_VALUE;

        for (Player survivor : HuntedPlugin.getInstance().getGameManager().getSurvivorsTeam().getPlayers())
        {
            if (survivor.getWorld().getEnvironment() != hunter.getWorld().getEnvironment())
                break;

            if (survivor != hunter)
            {
                double distanceBetweenThem = survivor.getLocation().distance(hunter.getLocation());

                if (distanceBetweenThem < nearestSurvivorDistance)
                {
                    nearestSurvivor = survivor;
                    nearestSurvivorDistance = distanceBetweenThem;
                }
            }
        }

        return nearestSurvivor;
    }
}
