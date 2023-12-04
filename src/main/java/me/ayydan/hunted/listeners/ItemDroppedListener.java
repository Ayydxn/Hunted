package me.ayydan.hunted.listeners;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.item.SurvivorTrackingCompassItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Objects;

public class ItemDroppedListener implements Listener
{
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent dropItemEvent)
    {
        Player player = dropItemEvent.getPlayer();
        Item droppedItem = dropItemEvent.getItemDrop();

        if (HuntedPlugin.getInstance().getGameManager().getCurrentGameState() != HuntedGameState.Active)
            return;

        if (!HuntedPlugin.getInstance().getGameManager().getHuntersTeam().isPlayerInTeam(player))
            return;

        if (!droppedItem.getItemStack().hasItemMeta())
            return;

        if (!droppedItem.getItemStack().getItemMeta().hasDisplayName())
            return;

        if (Objects.equals(droppedItem.getItemStack().getItemMeta().displayName(), SurvivorTrackingCompassItem.DISPLAY_NAME))
            dropItemEvent.setCancelled(true);
    }
}
