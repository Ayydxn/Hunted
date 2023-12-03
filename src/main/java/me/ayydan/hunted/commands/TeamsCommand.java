package me.ayydan.hunted.commands;

import me.ayydan.hunted.core.HuntedGameManager;
import me.ayydan.hunted.teams.HuntedTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamsCommand implements CommandExecutor
{
    private final HuntedGameManager huntedGameManager;
    
    public TeamsCommand(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (args.length > 0)
        {
            HuntedTeam targetTeam = switch (args[1])
            {
                case "hunters" -> this.huntedGameManager.getHuntersTeam();
                case "survivors" -> this.huntedGameManager.getSurvivorsTeam();
                case "spectators" -> this.huntedGameManager.getSpectatorsTeam();
                default -> null;
            };

            if (targetTeam == null)
            {
                sender.sendMessage(Component.text(String.format("Cannot perform operations on unknown team '%s'!", args[1]), NamedTextColor.RED));
                return true;
            }

            if (args[2].equalsIgnoreCase("add"))
            {
                Player playerToAdd = Bukkit.getPlayerExact(args[3]);
                if (playerToAdd == null)
                {
                    sender.sendMessage(Component.text(String.format("The player '%s' doesn't exist and can't be added!", args[3]), NamedTextColor.RED));
                    return true;
                }

                return new AddPlayerToTeamCommand(targetTeam, playerToAdd).onCommand(sender, command, label, args);
            }
            else if (args[2].equalsIgnoreCase("remove"))
            {
                Player playerToRemove = Bukkit.getPlayerExact(args[3]);
                if (playerToRemove == null)
                {
                    sender.sendMessage(Component.text(String.format("The player '%s' doesn't exist and can't be removed!", args[3]), NamedTextColor.RED));
                    return true;
                }

                return new RemovePlayerFromTeamCommand(targetTeam, playerToRemove).onCommand(sender, command, label, args);
            }
        }
        else
        {
            // TODO: (Ayydan) What the message being sent says.
            sender.sendMessage(Component.text("Some sort of the documentation listing all of Hunted's team related commands should be here.",
                    NamedTextColor.YELLOW));
        }

        return true;
    }
}
