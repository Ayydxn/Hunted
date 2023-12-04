package me.ayydan.hunted.commands;

import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.core.HuntedGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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

        if (args.length == 2)
        {
            if (args[1].equalsIgnoreCase("confirm"))
                this.huntedGameManager.startGame();
        }
        else
        {
            sender.sendMessage(Component.text("-----------------------------------------------------"));

            sender.sendMessage(Component.text("This is a confirmation message about the game you are trying to start.\n", NamedTextColor.YELLOW));

            sender.sendMessage(Component.text("Hunters:", NamedTextColor.DARK_RED));

            for (Player player : this.huntedGameManager.getHuntersTeam().getPlayers())
                sender.sendMessage(Component.text(String.format("- %s", player.getName()), NamedTextColor.RED));

            sender.sendMessage(Component.text("\nSurvivors:", NamedTextColor.DARK_GREEN));

            for (Player player : this.huntedGameManager.getSurvivorsTeam().getPlayers())
                sender.sendMessage(Component.text(String.format("- %s", player.getName()), NamedTextColor.GREEN));

            if (this.huntedGameManager.getSpectatorsTeam().getPlayerCount() > 0)
            {
                sender.sendMessage(Component.text("\nSpectators:", NamedTextColor.DARK_GRAY));

                for (Player player : this.huntedGameManager.getSpectatorsTeam().getPlayers())
                    sender.sendMessage(Component.text(String.format("- %s", player.getName()), NamedTextColor.GRAY));
            }

            sender.sendMessage(Component.text("\nIf you are sure you would like to start the game, please run .", NamedTextColor.YELLOW)
                    .append(Component.text("/hunted start confirm", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)));

            sender.sendMessage(Component.text("-----------------------------------------------------"));
        }

        return true;
    }
}
