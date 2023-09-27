package me.ayydan.hunted.commands.impl;

import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.core.HuntedGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StopManhuntCommand implements CommandExecutor
{
    private final HuntedGameManager huntedGameManager;

    public StopManhuntCommand(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (this.huntedGameManager.getCurrentGameState() == HuntedGameState.Ending)
        {
            sender.sendMessage(Component.text("You cannot stop a match of Minecraft Manhunt while one is ending!", NamedTextColor.RED));
            return true;
        }

        if (this.huntedGameManager.getCurrentGameState() == HuntedGameState.Ended)
        {
            sender.sendMessage(Component.text("You cannot stop a match of Minecraft Manhunt while one isn't active!", NamedTextColor.RED));
            return true;
        }

        this.huntedGameManager.endGame();

        return true;
    }
}
