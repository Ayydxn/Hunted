package me.ayydxn.hunted.commands.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.commands.base.AbstractHuntedCommand;
import me.ayydxn.hunted.game.GameManager;
import me.ayydxn.hunted.game.MatchState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/**
 * The command responsible for stopping a currently running game of Hunted.
 */
public class StopGameCommand implements AbstractHuntedCommand
{
    private final GameManager gameManager;

    public StopGameCommand(GameManager gameManager)
    {
        this.gameManager = gameManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("stop");
        // Display a confirmation message so that user can be sure if they want to stop the game or not.
        rootCommand.executes(StopGameCommand::sendConfirmationMessage);

        // Actually stops the game
        rootCommand.then(Commands.argument("confirm", StringArgumentType.word())
                .executes(context -> StopGameCommand.stopGame(context, this.gameManager)));

        return rootCommand;
    }

    private static int sendConfirmationMessage(CommandContext<CommandSourceStack> context)
    {
        CommandSender sender = context.getSource().getSender();

        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        sender.sendMessage(Component.text("This is a confirmation message about the game you are attempting to stop.\n", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("\nIf you are sure you would like to stop the game, please run:\n", NamedTextColor.YELLOW)
                .append(Component.text("/hunted stop confirm", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));

        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<CommandSourceStack> context, GameManager gameManager)
    {
        CommandSender sender = context.getSource().getSender();

        // // Check and make sure the user actually wants to stop the current game.
        if (!context.getArgument("confirm", String.class).equals("confirm"))
        {
            sender.sendMessage(Component.text("Did you mean to run \"/hunted stop confirm\"?", NamedTextColor.GOLD));
            return -1;
        }

        // To prevent players from stopping a non-existent game.
        if (gameManager.getCurrentMatchState() == MatchState.ENDING)
        {
            sender.sendMessage(Component.text("You cannot stop a match of Minecraft Manhunt while one is ending!", NamedTextColor.RED));
            return -1;
        }

        if (gameManager.getCurrentMatchState() == MatchState.ENDED)
        {
            sender.sendMessage(Component.text("You cannot stop a match of Minecraft Manhunt while one isn't active!", NamedTextColor.RED));
            return -1;
        }

        // Actually end the game.
        gameManager.endGame();

        return Command.SINGLE_SUCCESS;
    }
}
