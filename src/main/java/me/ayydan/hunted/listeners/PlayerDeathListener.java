package me.ayydan.hunted.listeners;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.gui.HunterRespawnGUI;
import me.ayydan.hunted.tasks.HuntedRespawnCountdownTask;
import me.ayydan.hunted.teams.HuntersTeam;
import me.ayydan.hunted.teams.SpectatorsTeam;
import me.ayydan.hunted.teams.SurvivorsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.Duration;
import java.util.Random;

public class PlayerDeathListener implements Listener
{
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
                new HunterRespawnGUI().open(killedPlayer);
            }
            else
            {
                Title.Times respawnMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
                Title respawnMessage = Title.title(Component.text("You have respawned!", NamedTextColor.GREEN), Component.empty(), respawnMessageDuration);

                killedPlayer.showTitle(respawnMessage);

                killedPlayer.teleport(deathLocation);
                killedPlayer.setGameMode(GameMode.SURVIVAL);

                for (Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    player.sendActionBar(Component.text(String.format("The Hunter %s has respawned!", killedPlayer.getName()), NamedTextColor.GREEN));
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
        });

        if (huntersTeam.isPlayerInTeam(killedPlayer))
        {
            this.onHunterDeath(killedPlayer, huntedRespawnCountdownTask);
        }
        else if (survivorsTeam.isPlayerInTeam(killedPlayer))
        {
            this.onSurvivorDeath(killedPlayer, survivorsTeam, spectatorsTeam);
        }

        playerDeathEvent.setCancelled(true);
    }

    private void onHunterDeath(Player killedHunter, HuntedRespawnCountdownTask huntedRespawnCountdownTask)
    {
        String deathMessage = this.getHunterDeathMessage(killedHunter, huntedRespawnCountdownTask);

        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if (player.getName().equalsIgnoreCase(killedHunter.getName()) && killedHunter.getKiller() != null)
            {
                player.sendActionBar(Component.text(String.format("You were killed by %s!", killedHunter.getKiller().getName()), NamedTextColor.RED));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                continue;
            }
            else if (player.getName().equalsIgnoreCase(killedHunter.getName()))
            {
                player.sendActionBar(Component.text("You died!", NamedTextColor.RED));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                continue;
            }

            player.sendActionBar(Component.text(deathMessage, NamedTextColor.GREEN));
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        killedHunter.setGameMode(GameMode.SPECTATOR);

        huntedRespawnCountdownTask.runTaskTimer(HuntedPlugin.getInstance(), 0L,20L);
    }

    private void onSurvivorDeath(Player killedSurvivor, SurvivorsTeam survivorsTeam, SpectatorsTeam spectatorsTeam)
    {
        String deathMessage = String.format("The survivor %s has died!", killedSurvivor.getName());

        if (killedSurvivor.getKiller() != null)
            deathMessage = String.format("The survivor %s was killed by %s!", killedSurvivor.getName(), killedSurvivor.getKiller().getName());

        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            player.sendActionBar(Component.text(deathMessage, NamedTextColor.GREEN));
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        survivorsTeam.removePlayer(killedSurvivor);
        spectatorsTeam.addPlayer(killedSurvivor);
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
}
