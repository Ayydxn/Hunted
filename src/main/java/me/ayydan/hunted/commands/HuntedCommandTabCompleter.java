package me.ayydan.hunted.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HuntedCommandTabCompleter implements TabCompleter
{
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (command.getName().equalsIgnoreCase("hunted"))
        {
            List<String> autoCompletions = new ArrayList<>();
            autoCompletions.add("start");
            autoCompletions.add("stop");
            autoCompletions.add("teams");

            if (args[0].equalsIgnoreCase("start"))
            {
                autoCompletions.clear();

                autoCompletions.add("confirm");
            }

            if (args[0].equalsIgnoreCase("stop"))
                return null;

            if (args[0].equalsIgnoreCase("teams"))
            {
                autoCompletions.clear();

                autoCompletions.add("hunters");
                autoCompletions.add("survivors");
                autoCompletions.add("spectators");

                if (args.length >= 2)
                {
                    boolean isArgumentTeamName = args[1].equalsIgnoreCase("hunters") || args[1].equalsIgnoreCase("survivors") ||
                            args[1].equalsIgnoreCase("spectators");

                    if (isArgumentTeamName)
                    {
                        autoCompletions.clear();

                        autoCompletions.add("add");
                        autoCompletions.add("remove");
                    }

                    if (args.length >= 3)
                    {
                        if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"))
                        {
                            autoCompletions.clear();

                            for (Player player : Bukkit.getServer().getOnlinePlayers())
                                autoCompletions.add(player.getName());
                        }
                    }
                }
            }

            return autoCompletions;
        }

        return null;
    }
}
