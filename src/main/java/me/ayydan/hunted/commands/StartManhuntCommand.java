package me.ayydan.hunted.commands;

import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.core.HuntedGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StartManhuntCommand implements CommandExecutor
{
    private final HuntedGameManager huntedGameManager;

    public StartManhuntCommand(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (this.huntedGameManager.getCurrentGameState() == HuntedGameState.Starting)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already starting!", NamedTextColor.RED));
            return true;
        }

        if (this.huntedGameManager.getCurrentGameState() == HuntedGameState.Active)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already active!", NamedTextColor.RED));
            return true;
        }

        this.huntedGameManager.startGame();

        return true;
    }
}
