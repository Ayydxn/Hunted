package me.ayydan.hunted.game;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.utils.PlayerUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class SurvivorDangerMeter
{
    private final BossBar dangerMeter;

    public SurvivorDangerMeter()
    {
        this.dangerMeter = BossBar.bossBar(Component.text("Danger Meter").color(NamedTextColor.GOLD), 0.0f,  BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10);
    }

    public void updateMeter(Player targetSurvivorPlayer)
    {
        HuntedGameManager huntedGameManager = HuntedPlugin.getInstance().getGameManager();
        if (!huntedGameManager.getSurvivorsTeam().isPlayerInTeam(targetSurvivorPlayer))
            return;

        Player nearestPlayer = PlayerUtils.getNearestPlayerFromPlayer(targetSurvivorPlayer);
        if (nearestPlayer == null)
            return;

        if (!huntedGameManager.getHuntersTeam().isPlayerInTeam(nearestPlayer))
            return;

        int distanceInBlocks = (int) targetSurvivorPlayer.getLocation().distance(nearestPlayer.getLocation());
        targetSurvivorPlayer.sendMessage(Component.text("Distance From Nearest Hunter: " + distanceInBlocks + " Blocks"));

        this.dangerMeter.progress(this.getMeterProgress(distanceInBlocks));
    }

    public void displayMeter(Player targetSurvivorPlayer)
    {
        if (!HuntedPlugin.getInstance().getGameManager().getSurvivorsTeam().isPlayerInTeam(targetSurvivorPlayer))
            return;

        targetSurvivorPlayer.showBossBar(this.dangerMeter);
    }

    private float getMeterProgress(int distanceBetweenSurvivorAndHunter)
    {
        float meterProgress = 0.0f;

        if (distanceBetweenSurvivorAndHunter <= 100)
            meterProgress = 1.0f;

        if (distanceBetweenSurvivorAndHunter >= 100 && distanceBetweenSurvivorAndHunter <= 200)
            meterProgress = 0.9f;

        if (distanceBetweenSurvivorAndHunter >= 200 && distanceBetweenSurvivorAndHunter <= 300)
            meterProgress = 0.8f;

        if (distanceBetweenSurvivorAndHunter >= 300 && distanceBetweenSurvivorAndHunter <= 400)
            meterProgress = 0.7f;

        if (distanceBetweenSurvivorAndHunter >= 400 && distanceBetweenSurvivorAndHunter <= 500)
            meterProgress = 0.6f;

        if (distanceBetweenSurvivorAndHunter >= 500 && distanceBetweenSurvivorAndHunter <= 600)
            meterProgress = 0.5f;

        if (distanceBetweenSurvivorAndHunter >= 600 && distanceBetweenSurvivorAndHunter <= 700)
            meterProgress = 0.4f;

        if (distanceBetweenSurvivorAndHunter >= 700 && distanceBetweenSurvivorAndHunter <= 800)
            meterProgress = 0.3f;

        if (distanceBetweenSurvivorAndHunter >= 800 && distanceBetweenSurvivorAndHunter <= 900)
            meterProgress = 0.2f;

        if (distanceBetweenSurvivorAndHunter >= 900 && distanceBetweenSurvivorAndHunter <= 1000)
            meterProgress = 0.1f;

        return meterProgress;
    }
}
