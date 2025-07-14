package me.ayydxn.hunted.commands.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.commands.arguments.GameModeArgumentType;
import me.ayydxn.hunted.commands.base.AbstractHuntedCommand;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.HuntedGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Objects;

public class GameModeCommand implements AbstractHuntedCommand
{
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("gamemode");
        rootCommand.then(Commands.literal("select").then(Commands.argument("gameMode", GameModeArgumentType.huntedGameMode())
                        .executes(GameModeCommand::updateSelectedGameMode)));
        rootCommand.then(Commands.literal("list").executes(GameModeCommand::listAvailableGameModes));

        return rootCommand;
    }

    private static int updateSelectedGameMode(CommandContext<CommandSourceStack> commandContext)
    {
        HuntedGameMode newGameMode = commandContext.getArgument("gameMode", HuntedGameMode.class);

        commandContext.getSource().getSender().sendMessage("Selected Game Mode: " + newGameMode.getDisplayName());

        return Command.SINGLE_SUCCESS;
    }

    private static int listAvailableGameModes(CommandContext<CommandSourceStack> commandContext)
    {
        Map<String, ?> registeredGameModes = GameModeRegistry.getRegisteredGameModes();
        CommandSender sender = commandContext.getSource().getSender();

        if (registeredGameModes.isEmpty())
        {
            sender.sendMessage(Component.text("No game modes are available. Something is probably bugged...").color(NamedTextColor.RED));
            return -1;
        }

        sender.sendMessage(Component.text("Available Game Modes:").color(NamedTextColor.GREEN));

        for (String gameModeID : registeredGameModes.keySet())
        {
            // Create a temporary instance of the game mode so that we can get the display name.
            HuntedGameMode mode = Objects.requireNonNull(GameModeRegistry.create(gameModeID, HuntedPlugin.getInstance()));
            String displayName = !mode.getDisplayName().isEmpty() ? mode.getDisplayName() : "Unknown";

            sender.sendMessage(Component.text(String.format("- %s (%s) - %s", gameModeID, displayName, mode.getDescription()))
                    .color(NamedTextColor.YELLOW));
        }

        return Command.SINGLE_SUCCESS;
    }
}
