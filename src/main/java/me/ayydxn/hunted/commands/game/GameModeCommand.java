package me.ayydxn.hunted.commands.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.commands.arguments.GameModeArgumentType;
import me.ayydxn.hunted.commands.base.AbstractHuntedCommand;
import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.HuntedGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Objects;

/**
 * The root of the {@code /hunted gamemode} command tree which contains all commands relating to game modes within Hunted.
 *
 * @see HuntedGameMode
 */
public class GameModeCommand implements AbstractHuntedCommand
{
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("gamemode");

        // Allows for selecting and changing the gamemode that will be used in future games.
        rootCommand.then(Commands.literal("select").then(Commands.argument("gameMode", GameModeArgumentType.huntedGameMode())
                .executes(GameModeCommand::updateSelectedGameMode)));

        // List all game modes that are registered with Hunted and available for playing.
        rootCommand.then(Commands.literal("list").executes(GameModeCommand::listAvailableGameModes));

        return rootCommand;
    }

    private static int updateSelectedGameMode(CommandContext<CommandSourceStack> commandContext)
    {
        // The newly selected game mode from the passed argument and update the match settings
        HuntedGameMode newGameMode = commandContext.getArgument("gameMode", HuntedGameMode.class);

        GameManager gameManager = HuntedPlugin.getInstance().getGameManager();
        gameManager.getMatchSettings().selectedGameMode.setValue(newGameMode);

        // Notify the command sender executed the command that the game mode has been updated.
        Component selectedGameModeMessage = Component.text("Selected Game Mode: ", NamedTextColor.GREEN)
                .append(Component.text(newGameMode.getDisplayName(), NamedTextColor.GOLD));

        commandContext.getSource().getSender().sendMessage(selectedGameModeMessage);

        return Command.SINGLE_SUCCESS;
    }

    private static int listAvailableGameModes(CommandContext<CommandSourceStack> commandContext)
    {
        Map<String, ?> registeredGameModes = GameModeRegistry.getRegisteredGameModes();
        CommandSender sender = commandContext.getSource().getSender();

        // Nothing to display if there are (somehow?) no registered game modes.
        if (registeredGameModes.isEmpty())
        {
            sender.sendMessage(Component.text("No game modes are available. Something is probably bugged...").color(NamedTextColor.RED));
            return -1;
        }

        sender.sendMessage(Component.text("Available Game Modes:").color(NamedTextColor.GREEN));

        // Iterate through all games within the game mode registry and show them to the command sender, displaying its ID, display name and description.
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
