package me.ayydan.hunted.gui;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.teams.HuntersTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class HunterRespawnGUI
{
    public static final Component TITLE = Component.text("Select A Teammate To Respawn On", NamedTextColor.GOLD);

    public void open(Player player)
    {
        HuntedGameManager huntedGameManager = HuntedPlugin.getInstance().getGameManager();
        if (huntedGameManager.getCurrentGameState() != HuntedGameState.Active)
            return;

        HuntersTeam huntersTeam = huntedGameManager.getHuntersTeam();
        if (!huntersTeam.isPlayerInTeam(player))
            return;

        ArrayList<Player> hunterTeammates = huntersTeam.getPlayers();
        hunterTeammates.removeIf(hunterPlayer -> hunterPlayer.getName().equalsIgnoreCase(player.getName()));

        Inventory respawnGUI = Bukkit.createInventory(player, 45, TITLE);

        for (Player hunterTeammate : hunterTeammates)
        {
            ItemStack hunterTeammateSkull = new ItemStack(Material.PLAYER_HEAD, 1);

            SkullMeta hunterTeammateSkullMeta = (SkullMeta) hunterTeammateSkull.getItemMeta();
            hunterTeammateSkullMeta.displayName(Component.text(hunterTeammate.getName(), NamedTextColor.GREEN));
            hunterTeammateSkullMeta.setOwningPlayer(hunterTeammate);

            hunterTeammateSkull.setItemMeta(hunterTeammateSkullMeta);

            respawnGUI.addItem(hunterTeammateSkull);
        }

        player.openInventory(respawnGUI);
    }
}
