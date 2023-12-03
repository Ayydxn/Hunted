package me.ayydan.hunted.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.item.SurvivorTrackingCompassItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class PlayerEventsListener implements Listener
{
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent)
    {
        boolean isGameStarting = HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Starting;
        boolean hasPlayerPosChanged = playerMoveEvent.getFrom() != playerMoveEvent.getTo();

        if (isGameStarting && hasPlayerPosChanged)
        {
            Location oldPlayerLocation = playerMoveEvent.getFrom();
            Location newPlayerLocation = playerMoveEvent.getTo();

            boolean hasPlayerTriedToMove = (oldPlayerLocation.getX() != newPlayerLocation.getX() && oldPlayerLocation.getZ() != newPlayerLocation.getZ()) ||
                    oldPlayerLocation.getY() != newPlayerLocation.getY();

            // Prevents the player from moving or jumping while the game is starting
            if (hasPlayerTriedToMove)
                playerMoveEvent.setTo(oldPlayerLocation);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent)
    {
        if (HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Starting)
            blockBreakEvent.setCancelled(true);
    }

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

    @EventHandler
    public void onPlayerChat(AsyncChatEvent asyncChatEvent)
    {
        boolean hasGameStarted = HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Active;
        boolean isPlayerSpectating = HuntedPlugin.getInstance().getGameManager().getSpectatorsTeam().isPlayerInTeam(asyncChatEvent.getPlayer());

        asyncChatEvent.getPlayer().sendMessage(Component.text("You are not allowed to chat as a spectator in order to upkeep competitive integrity!",
                NamedTextColor.RED));

        asyncChatEvent.setCancelled(hasGameStarted && isPlayerSpectating);
    }
}
