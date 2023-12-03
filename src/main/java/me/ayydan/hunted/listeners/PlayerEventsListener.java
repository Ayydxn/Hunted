package me.ayydan.hunted.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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

            // Prevents the player from moving while the game is starting
            if (oldPlayerLocation.getX() != newPlayerLocation.getX() && oldPlayerLocation.getZ() != newPlayerLocation.getZ())
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
    public void onPlayerChat(AsyncChatEvent asyncChatEvent)
    {
        boolean hasGameStarted = HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Active;
        boolean isPlayerSpectating = HuntedPlugin.getInstance().getGameManager().getSpectatorsTeam().isPlayerInTeam(asyncChatEvent.getPlayer());

        asyncChatEvent.setCancelled(hasGameStarted && isPlayerSpectating);
    }
}
