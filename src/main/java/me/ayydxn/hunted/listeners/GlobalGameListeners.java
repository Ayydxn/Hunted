package me.ayydxn.hunted.listeners;


import me.ayydxn.hunted.game.GameInitializationState;
import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.HuntedGameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * A listener used to control certain game aspects that are not specific to any {@link HuntedGameMode game mode} implementation.
 */
public record GlobalGameListeners(GameManager gameManager) implements Listener
{
    /**
     * Stops the player from moving before the game has officially started.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent)
    {
        GameInitializationState gameInitializationState = this.gameManager.getInitializationState();
        if (gameInitializationState == GameInitializationState.PREPARING_PLAYERS || gameInitializationState == GameInitializationState.STARTING_COUNTDOWN)
        {
            Location oldLocation = playerMoveEvent.getFrom();
            Location newLocation = playerMoveEvent.getTo();

            boolean hasPlayerTriedToMove = (oldLocation.getX() != newLocation.getX() && oldLocation.getZ() != newLocation.getZ()) ||
                    oldLocation.getY() != newLocation.getY();

            if (hasPlayerTriedToMove)
                playerMoveEvent.setTo(oldLocation);
        }
    }
}
