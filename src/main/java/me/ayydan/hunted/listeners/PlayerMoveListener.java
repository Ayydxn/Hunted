package me.ayydan.hunted.listeners;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener
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
}
