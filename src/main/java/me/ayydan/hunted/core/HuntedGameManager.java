package me.ayydan.hunted.core;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.item.SurvivorTrackingCompassItem;
import me.ayydan.hunted.tasks.HuntedGameStartCountdownTask;
import me.ayydan.hunted.tasks.HuntedGameUpdaterTask;
import me.ayydan.hunted.teams.HuntersTeam;
import me.ayydan.hunted.teams.SpectatorsTeam;
import me.ayydan.hunted.teams.SurvivorsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HuntedGameManager
{
    private final HuntersTeam huntersTeam;
    private final SurvivorsTeam survivorsTeam;
    private final SpectatorsTeam spectatorsTeam;

    private HashMap<Player, SurvivorTrackingCompassItem> huntersToSurvivorTrackingCompass;
    private HuntedGameUpdaterTask gameUpdaterTask;
    private HuntedGameState currentGameState;

    public HuntedGameManager()
    {
        this.currentGameState = HuntedGameState.Ended;

        this.huntersTeam = new HuntersTeam();
        this.survivorsTeam = new SurvivorsTeam();
        this.spectatorsTeam = new SpectatorsTeam();
    }

    public void startGame()
    {
        if (this.huntersTeam.getPlayerCount() == 0 && this.survivorsTeam.getPlayerCount() == 0)
        {
            Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt cannot be started as there are no Hunters nor Survivors!",
                    NamedTextColor.RED));
            return;
        }

        /* if (this.huntersTeam.getPlayerCount() == 0)
        {
            Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt cannot be started as there are no Hunters!", NamedTextColor.RED));
            return;
        }

        if (this.survivorsTeam.getPlayerCount() == 0)
        {
            Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt cannot be started as there are no Survivors!", NamedTextColor.RED));
            return;
        } */

        this.currentGameState = HuntedGameState.Starting;

        this.huntersToSurvivorTrackingCompass = new HashMap<>(this.huntersTeam.getPlayerCount());

        this.createStartAreaAndTeleportPlayers();
        this.clearHuntersAndSurvivorsInventories();

        // TODO: (Ayydan) Countdown timer should be configurable.
        HuntedGameStartCountdownTask huntedGameStartCountdownTask = new HuntedGameStartCountdownTask(10, () ->
        {
            this.gameUpdaterTask = new HuntedGameUpdaterTask(this);
            this.gameUpdaterTask.runTaskTimer(HuntedPlugin.getInstance(), 0L, 100L);

            this.giveHuntersSurvivorTrackers();

            this.currentGameState = HuntedGameState.Active;
        });

        huntedGameStartCountdownTask.runTaskTimer(HuntedPlugin.getInstance(), 0L, 20L);
    }

    public void tickGame()
    {
        for (Map.Entry<Player, SurvivorTrackingCompassItem> entry : this.huntersToSurvivorTrackingCompass.entrySet())
        {
            Player hunter = entry.getKey();
            SurvivorTrackingCompassItem survivorTrackingCompass = entry.getValue();

            survivorTrackingCompass.updateTrackingCompass(hunter);
        }
    }

    public void endGame()
    {
        this.currentGameState = HuntedGameState.Ending;

        this.huntersToSurvivorTrackingCompass.clear();

        this.gameUpdaterTask.cancel();

        this.currentGameState = HuntedGameState.Ended;
    }

    private void createStartAreaAndTeleportPlayers()
    {
        Location worldSpawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();

        ArrayList<Player> participatingPlayers = new ArrayList<>();
        participatingPlayers.addAll(this.huntersTeam.getPlayers());
        participatingPlayers.addAll(this.survivorsTeam.getPlayers());

        int numberOfParticipatingPlayers = participatingPlayers.size();

        int startAreaCenterX = worldSpawnLocation.getBlockX() + (numberOfParticipatingPlayers / 2);
        int startAreaCenterZ = worldSpawnLocation.getBlockZ() + (numberOfParticipatingPlayers / 2);

        for (int i = startAreaCenterX - numberOfParticipatingPlayers; i <= startAreaCenterX + numberOfParticipatingPlayers; i++)
        {
            for (int j = startAreaCenterZ - numberOfParticipatingPlayers; j <= startAreaCenterZ + numberOfParticipatingPlayers; j++)
            {
                Block block = Bukkit.getWorlds().get(0).getBlockAt(i, worldSpawnLocation.getBlockY(), j);
                block.setType(Material.AIR);
            }
        }

        for (Player participatingPlayer : participatingPlayers)
        {
            int offsetX = (int) (Math.random() * numberOfParticipatingPlayers) - (numberOfParticipatingPlayers / 2);
            int offsetZ = (int) (Math.random() * numberOfParticipatingPlayers) - (numberOfParticipatingPlayers / 2);

            Location teleportLocation = new Location(worldSpawnLocation.getWorld(), worldSpawnLocation.getX() + offsetX, worldSpawnLocation.getY(),
                    worldSpawnLocation.getZ() + offsetZ);

            participatingPlayer.teleport(teleportLocation);
        }

        for (Player spectator : this.spectatorsTeam.getPlayers())
            spectator.setGameMode(GameMode.SPECTATOR);
    }

    private void giveHuntersSurvivorTrackers()
    {
        for (Player hunter : this.huntersTeam.getPlayers())
        {
            SurvivorTrackingCompassItem survivorTrackingCompassItem = new SurvivorTrackingCompassItem();
            this.huntersToSurvivorTrackingCompass.put(hunter, survivorTrackingCompassItem);

            hunter.getInventory().addItem(survivorTrackingCompassItem.getItemStack());
        }
    }

    private void clearHuntersAndSurvivorsInventories()
    {
        for (Player player : this.huntersTeam.getPlayers())
            player.getInventory().clear();

        for (Player player : this.survivorsTeam.getPlayers())
            player.getInventory().clear();
    }

    public HuntersTeam getHuntersTeam()
    {
        return this.huntersTeam;
    }

    public SurvivorsTeam getSurvivorsTeam()
    {
        return this.survivorsTeam;
    }

    public SpectatorsTeam getSpectatorsTeam()
    {
        return this.spectatorsTeam;
    }

    public HuntedGameState getCurrentGameState()
    {
        return this.currentGameState;
    }
}
