package me.ayydan.hunted.listeners;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.core.HuntedGameState;
import me.ayydan.hunted.tasks.HuntedResetWorldCountdown;
import me.ayydan.hunted.teams.HuntersTeam;
import me.ayydan.hunted.teams.SurvivorsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.time.Duration;

public class EnderDragonDeathListener implements Listener
{
    @EventHandler
    public void onEntityDeath(EntityDeathEvent entityDeathEvent)
    {
        HuntedGameManager huntedGameManager = HuntedPlugin.getInstance().getGameManager();
        if (huntedGameManager.getCurrentGameState() != HuntedGameState.Active)
            return;

        Player killer = entityDeathEvent.getEntity().getKiller();
        Entity killedEntity = entityDeathEvent.getEntity();

        if (killer == null)
            return;

        if (!huntedGameManager.getSurvivorsTeam().isPlayerInTeam(killer))
            return;

        if (killedEntity.getType() != EntityType.ENDER_DRAGON)
            return;

        this.displaySurvivorsWinMessage(huntedGameManager.getSurvivorsTeam());
        this.displayHuntersLoseMessage(huntedGameManager.getHuntersTeam());
        this.displayWorldResetMessage();
    }

    private void displaySurvivorsWinMessage(SurvivorsTeam survivorsTeam)
    {
        for (Player survivor : survivorsTeam.getPlayers())
        {
            Title.Times winMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
            Title winMessage = Title.title(Component.text("You Win!").color(NamedTextColor.GREEN),
                    Component.text("You successfully killed the Ender Dragon!").color(NamedTextColor.GREEN), winMessageDuration);

            survivor.showTitle(winMessage);
        }
    }

    private void displayHuntersLoseMessage(HuntersTeam huntersTeam)
    {
        for (Player hunter : huntersTeam.getPlayers())
        {
            Title.Times loseMessageDuration = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
            Title loseMessage = Title.title(Component.text("You Lost!").color(NamedTextColor.RED),
                    Component.text("You failed to kill all of the survivors!").color(NamedTextColor.RED), loseMessageDuration);

            hunter.showTitle(loseMessage);
        }
    }

    private void displayWorldResetMessage()
    {
        HuntedResetWorldCountdown resetWorldCountdown = new HuntedResetWorldCountdown(10, () ->
                HuntedPlugin.getInstance().getGameManager().endGame());

        resetWorldCountdown.runTaskTimer(HuntedPlugin.getInstance(), 0L, 20L);
    }
}
