package me.ayydan.hunted.core;

import me.ayydan.hunted.HuntedPlugin;
import me.ayydan.hunted.tasks.HuntedGameUpdaterTask;
import me.ayydan.hunted.teams.HuntersTeam;
import me.ayydan.hunted.teams.SurvivorsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class HuntedGameManager
{
    private final HuntersTeam huntersTeam;
    private final SurvivorsTeam survivorsTeam;

    private HuntedGameUpdaterTask gameUpdaterTask;
    private HuntedGameState currentGameState;

    public HuntedGameManager()
    {
        this.currentGameState = HuntedGameState.Ended;

        this.huntersTeam = new HuntersTeam();
        this.survivorsTeam = new SurvivorsTeam();
    }

    public void startGame()
    {
        if (this.huntersTeam.getPlayerCount() == 0)
        {
            Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt cannot be started as there are no Hunters!", NamedTextColor.RED));
            return;
        }

        if (this.survivorsTeam.getPlayerCount() == 0)
        {
            Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt cannot be started as there are no Survivors!", NamedTextColor.RED));
            return;
        }

        this.currentGameState = HuntedGameState.Starting;

        this.gameUpdaterTask = new HuntedGameUpdaterTask(this);
        this.gameUpdaterTask.runTaskTimer(HuntedPlugin.getInstance(), 0L, 100L);

        this.currentGameState = HuntedGameState.Active;
    }

    public void tickGame()
    {
        // TODO: (Ayydan) Remove this test code.
        Bukkit.getServer().broadcast(Component.text("A match of Minecraft Manhunt has been updated!", NamedTextColor.GREEN));
    }

    public void endGame()
    {
        this.currentGameState = HuntedGameState.Ending;

        this.gameUpdaterTask.cancel();

        this.currentGameState = HuntedGameState.Ended;
    }

    public HuntersTeam getHuntersTeam()
    {
        return this.huntersTeam;
    }

    public SurvivorsTeam getSurvivorsTeam()
    {
        return this.survivorsTeam;
    }

    public HuntedGameState getCurrentGameState()
    {
        return this.currentGameState;
    }
}
