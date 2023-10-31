package me.ayydan.hunted.commands;

import me.ayydan.hunted.teams.HuntedTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AddPlayerToTeamCommand implements CommandExecutor
{
    private final HuntedTeam targetTeam;
    private final Player targetPlayer;

    public AddPlayerToTeamCommand(HuntedTeam targetTeam, Player targetPlayer)
    {
        this.targetTeam = targetTeam;
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (targetTeam.isPlayerInTeam(targetPlayer))
        {
            sender.sendMessage(Component.text(String.format("The player '%s' is already in that team!", args[3]), NamedTextColor.RED));
            return true;
        }

        targetTeam.addPlayer(targetPlayer);

        sender.sendMessage(Component.text(String.format("Added player '%s' to team '%s'!", this.targetPlayer.getName(), this.targetTeam.getTeamName()),
                NamedTextColor.GREEN));

        this.targetPlayer.sendMessage(Component.text(String.format("You have been added to the team '%s'!", this.targetTeam.getTeamName()),
                NamedTextColor.GREEN));

        return true;
    }
}
