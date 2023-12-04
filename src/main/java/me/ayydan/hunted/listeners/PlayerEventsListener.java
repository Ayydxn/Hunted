package me.ayydan.hunted.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.item.SurvivorTrackingCompassItem;
import me.ayydan.hunted.tasks.HuntedRespawnCountdownTask;
import me.ayydan.hunted.teams.HuntersTeam;
import me.ayydan.hunted.teams.SpectatorsTeam;
import me.ayydan.hunted.teams.SurvivorsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

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
    public void onEntityDamage(EntityDamageByEntityEvent damageByEntityEvent)
    {
        if (!(damageByEntityEvent.getDamager() instanceof Player attackingPlayer))
            return;

        if (!(damageByEntityEvent.getEntity() instanceof Player attackedPlayer))
            return;

        HuntersTeam huntersTeam = HuntedPlugin.getInstance().getGameManager().getHuntersTeam();
        SurvivorsTeam survivorsTeam = HuntedPlugin.getInstance().getGameManager().getSurvivorsTeam();

        boolean areBothPlayersHunters = huntersTeam.isPlayerInTeam(attackingPlayer) && huntersTeam.isPlayerInTeam(attackedPlayer);
        boolean areBothPlayersSurvivors = survivorsTeam.isPlayerInTeam(attackingPlayer) && survivorsTeam.isPlayerInTeam(attackedPlayer);

        if (areBothPlayersHunters || areBothPlayersSurvivors)
            damageByEntityEvent.setCancelled(true);
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
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent)
    {
        if (HuntedPlugin.getInstance().getGameManager().getCurrentGameState() != HuntedGameState.Active)
            return;

        Player killedPlayer = playerDeathEvent.getPlayer();
        Location deathLocation = killedPlayer.getLocation();
        HuntersTeam huntersTeam = HuntedPlugin.getInstance().getGameManager().getHuntersTeam();
        SurvivorsTeam survivorsTeam = HuntedPlugin.getInstance().getGameManager().getSurvivorsTeam();
        SpectatorsTeam spectatorsTeam = HuntedPlugin.getInstance().getGameManager().getSpectatorsTeam();
        HuntedRespawnCountdownTask huntedRespawnCountdownTask = new HuntedRespawnCountdownTask(killedPlayer, 15, () ->
        {
            if (huntersTeam.getPlayerCount() > 1)
            {
                int randomTeammateIndex = new Random().nextInt(0, huntersTeam.getPlayerCount());
                Player teammateToBeTeleportedTo = huntersTeam.getPlayers().get(randomTeammateIndex);

                killedPlayer.setGameMode(GameMode.SURVIVAL);
                killedPlayer.teleport(teammateToBeTeleportedTo.getLocation());
            }
            else
            {
                killedPlayer.teleport(deathLocation);
                killedPlayer.setGameMode(GameMode.SURVIVAL);
            }

            for (Player player : Bukkit.getServer().getOnlinePlayers())
            {
                if (player.getName().equalsIgnoreCase(killedPlayer.getName()))
                {
                    Title.Times respawnMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
                    Title respawnMessage = Title.title(Component.text("You have respawned!", NamedTextColor.GREEN), Component.empty(), respawnMessageDuration);

                    player.showTitle(respawnMessage);

                    continue;
                }

                player.sendActionBar(Component.text(String.format("The Hunter %s has respawned!", killedPlayer.getName()), NamedTextColor.GREEN));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        });

        if (huntersTeam.isPlayerInTeam(killedPlayer))
        {
            String deathMessage = this.getHunterDeathMessage(killedPlayer, huntedRespawnCountdownTask);

            for (Player player : Bukkit.getServer().getOnlinePlayers())
            {
                if (player.getName().equalsIgnoreCase(killedPlayer.getName()) && killedPlayer.getKiller() != null)
                {
                    player.sendActionBar(Component.text(String.format("You were killed by %s!", killedPlayer.getKiller().getName()), NamedTextColor.RED));
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                    continue;
                }
                else if (player.getName().equalsIgnoreCase(killedPlayer.getName()))
                {
                    player.sendActionBar(Component.text("You died!", NamedTextColor.RED));
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                    continue;
                }

                player.sendActionBar(Component.text(deathMessage, NamedTextColor.GREEN));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            killedPlayer.setGameMode(GameMode.SPECTATOR);

            huntedRespawnCountdownTask.runTaskTimer(HuntedPlugin.getInstance(), 0L,20L);
        }
        else if (survivorsTeam.isPlayerInTeam(killedPlayer))
        {
            String deathMessage = String.format("The survivor %s has died!", killedPlayer.getName());

            if (killedPlayer.getKiller() != null)
                deathMessage = String.format("The survivor %s was killed by %s!", killedPlayer.getName(), killedPlayer.getKiller().getName());

            for (Player player : Bukkit.getServer().getOnlinePlayers())
            {
                player.sendActionBar(Component.text(deathMessage, NamedTextColor.GREEN));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            survivorsTeam.removePlayer(killedPlayer);
            spectatorsTeam.addPlayer(killedPlayer);
        }

        playerDeathEvent.setCancelled(true);
    }

    private String getHunterDeathMessage(Player killedHunter, HuntedRespawnCountdownTask huntedRespawnCountdownTask)
    {
        String deathMessage = String.format("The hunter %s has died! They will be respawned in %d seconds.", killedHunter.getName(),
                huntedRespawnCountdownTask.getCountdownTime());

        if (killedHunter.getKiller() != null)
        {
            deathMessage = String.format("The hunter %s was killed by %s! They will be respawned in %d seconds.", killedHunter.getName(),
                    killedHunter.getKiller().getName(), huntedRespawnCountdownTask.getCountdownTime());
        }

        return deathMessage;
    }

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
