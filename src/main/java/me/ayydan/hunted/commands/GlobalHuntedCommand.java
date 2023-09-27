package me.ayydan.hunted.commands;

import me.ayydan.hunted.commands.impl.StartManhuntCommand;
import me.ayydan.hunted.commands.impl.StopManhuntCommand;
import me.ayydan.hunted.core.HuntedGameManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GlobalHuntedCommand implements CommandExecutor
{
    private final HuntedGameManager huntedGameManager;

    public GlobalHuntedCommand(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("start"))
                return new StartManhuntCommand(this.huntedGameManager).onCommand(sender, command, label, args);

            if (args[0].equalsIgnoreCase("stop"))
                return new StopManhuntCommand(this.huntedGameManager).onCommand(sender, command, label, args);
        }
        else
        {
            // TODO: (Ayydan) What the message being sent says.
            sender.sendMessage(NamedTextColor.YELLOW + "Some sort of the documentation listing all of Hunted's commands should be here.");
        }

        return true;
    }
}
