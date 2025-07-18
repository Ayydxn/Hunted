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

public class StopGameCommand implements AbstractHuntedCommand
{
    private final GameManager gameManager;

    public StopGameCommand(GameManager gameManager)
    {
        this.gameManager = gameManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("stop");
        rootCommand.executes(StopGameCommand::sendConfirmationMessage);
        rootCommand.then(Commands.argument("confirm", StringArgumentType.word())
                .executes(context -> StopGameCommand.stopGame(context, this.gameManager)));

        return rootCommand;
    }

    @SuppressWarnings("UnstableApiUsage")
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

    @SuppressWarnings("UnstableApiUsage")
    private static int stopGame(CommandContext<CommandSourceStack> context, GameManager gameManager)
    {
        CommandSender sender = context.getSource().getSender();

        if (!context.getArgument("confirm", String.class).equals("confirm"))
        {
            sender.sendMessage(Component.text("Did you mean to run \"/hunted stop confirm\"?", NamedTextColor.GOLD));
            return Command.SINGLE_SUCCESS;
        }

        if (gameManager.getCurrentMatchState() == MatchState.ENDING)
        {
            sender.sendMessage(Component.text("You cannot stop a match of Minecraft Manhunt while one is ending!", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (gameManager.getCurrentMatchState() == MatchState.ENDED)
        {
            sender.sendMessage(Component.text("You cannot stop a match of Minecraft Manhunt while one isn't active!", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        gameManager.endGame();

        return Command.SINGLE_SUCCESS;
    }
}
