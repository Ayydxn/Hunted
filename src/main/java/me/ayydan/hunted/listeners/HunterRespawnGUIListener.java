package me.ayydan.hunted.listeners;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.gui.HunterRespawnGUI;
import me.ayydan.hunted.teams.HuntersTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Objects;

public class HunterRespawnGUIListener implements Listener
{
    private BukkitTask reopenGUITask;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent)
    {
        HuntedGameManager huntedGameManager = HuntedPlugin.getInstance().getGameManager();
        if (huntedGameManager.getCurrentGameState() != HuntedGameState.Active)
            return;

        Player playerWhoClicked = (Player) inventoryClickEvent.getWhoClicked();

        HuntersTeam huntersTeam = huntedGameManager.getHuntersTeam();
        if (!huntersTeam.isPlayerInTeam(playerWhoClicked))
            return;

        if (Objects.equals(inventoryClickEvent.getView().title(), HunterRespawnGUI.TITLE))
        {
            ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
            if (clickedItem == null)
                return;

            ItemMeta clickedItemMeta = clickedItem.getItemMeta();

            if (clickedItem.getType() == Material.PLAYER_HEAD)
            {
                String teammateName = ((TextComponent) Objects.requireNonNull(clickedItemMeta.displayName())).content();
                Player teammateToTeleportTo = Objects.requireNonNull(Bukkit.getServer().getPlayerExact(teammateName));

                playerWhoClicked.teleport(teammateToTeleportTo.getLocation());
                playerWhoClicked.setGameMode(GameMode.SURVIVAL);

                inventoryClickEvent.getView().close();

                if (this.reopenGUITask != null)
                    this.reopenGUITask.cancel();

                Title.Times respawnMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
                Title respawnMessage = Title.title(Component.text("You have respawned!", NamedTextColor.GREEN), Component.empty(), respawnMessageDuration);

                playerWhoClicked.showTitle(respawnMessage);
                playerWhoClicked.playSound(playerWhoClicked, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                for (Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    if (Objects.equals(player.getName(), playerWhoClicked.getName()))
                        continue;

                    if (Objects.equals(player.getName(), teammateName))
                    {
                        player.sendActionBar(Component.text(String.format("Your teammate %s has respawned on you!", player.getName()), NamedTextColor.GREEN));
                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        break;
                    }

                    player.sendActionBar(Component.text(String.format("The Hunter %s has respawned!", playerWhoClicked.getName()), NamedTextColor.GREEN));
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent inventoryInteractEvent)
    {
        if (Objects.equals(inventoryInteractEvent.getView().title(), HunterRespawnGUI.TITLE))
            inventoryInteractEvent.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent)
    {
        HuntedGameManager huntedGameManager = HuntedPlugin.getInstance().getGameManager();
        if (huntedGameManager.getCurrentGameState() != HuntedGameState.Active)
            return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        Inventory closedInventory = inventoryCloseEvent.getInventory();

        if (Objects.equals(inventoryCloseEvent.getView().title(), HunterRespawnGUI.TITLE))
            this.reopenGUITask = Bukkit.getServer().getScheduler().runTaskLater(HuntedPlugin.getInstance(), () -> player.openInventory(closedInventory), 5L);
    }
}
