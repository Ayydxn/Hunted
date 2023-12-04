package me.ayydan.hunted.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener
{
    @EventHandler
    public void onPlayerChat(AsyncChatEvent asyncChatEvent)
    {
        boolean hasGameStarted = HuntedPlugin.getInstance().getGameManager().getCurrentGameState() == HuntedGameState.Active;
        boolean isPlayerSpectating = HuntedPlugin.getInstance().getGameManager().getSpectatorsTeam().isPlayerInTeam(asyncChatEvent.getPlayer());

        if (hasGameStarted && isPlayerSpectating)
        {
            asyncChatEvent.getPlayer().sendMessage(Component.text("You are not allowed to chat as a spectator in order to upkeep competitive integrity!",
                    NamedTextColor.RED));

            asyncChatEvent.setCancelled(true);
        }
    }
}
