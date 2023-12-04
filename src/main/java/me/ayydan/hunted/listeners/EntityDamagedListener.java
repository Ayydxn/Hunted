package me.ayydan.hunted.listeners;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.teams.HuntersTeam;
import me.ayydan.hunted.teams.SurvivorsTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamagedListener implements Listener
{
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
}
