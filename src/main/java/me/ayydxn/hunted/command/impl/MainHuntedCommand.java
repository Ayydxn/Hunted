package me.ayydxn.hunted.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.command.HuntedCommand;
import me.ayydxn.hunted.command.HuntedCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public class MainHuntedCommand implements HuntedCommand
{
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public LiteralCommandNode<CommandSourceStack> register()
    {
        return Commands.literal(this.getLabel()).executes(context ->
                {
                    CommandSender commandSender = context.getSource().getSender();
                    commandSender.sendMessage(Component.text("To see all of the available commands within Hunted, run \"/hunted help\".")
                            .color(NamedTextColor.GOLD));

                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("help").executes(context ->
                {
                    CommandSender commandSender = context.getSource().getSender();
                    commandSender.sendMessage(Component.text("----------- Hunted Command Index -----------", NamedTextColor.YELLOW));

                    for (HuntedCommand huntedCommand : HuntedCommandManager.getRegisteredCommands())
                    {
                        Component commandName = Component.text("/" + huntedCommand.getLabel() + ": ", NamedTextColor.GOLD);
                        Component commandDescription = Component.text(huntedCommand.getDescription(), NamedTextColor.WHITE);

                        commandSender.sendMessage(Component.text("- ")
                                .append(commandName)
                                .append(commandDescription));
                    }

                    return Command.SINGLE_SUCCESS;
                }))
                .build();
    }

    @Override
    public String getLabel()
    {
        return "hunted";
    }

    @Override
    public String getDescription()
    {
        return "The main command to Hunted's command interface.";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of();
    }
}
