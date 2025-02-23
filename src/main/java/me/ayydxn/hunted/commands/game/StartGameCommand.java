package me.ayydxn.hunted.commands.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.commands.base.BaseHuntedCommand;
import me.ayydxn.hunted.core.HuntedGameManager;
import me.ayydxn.hunted.core.HuntedGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class StartGameCommand implements BaseHuntedCommand
{
    private final HuntedGameManager huntedGameManager;

    public StartGameCommand(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("start");
        rootCommand.executes(StartGameCommand::sendConfirmationMessage);
        rootCommand.then(Commands.argument("confirm", StringArgumentType.word())
                .executes(context -> StartGameCommand.startGame(context, this.huntedGameManager)));

        return rootCommand;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static int sendConfirmationMessage(CommandContext<CommandSourceStack> context)
    {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        sender.sendMessage(Component.text("This is a confirmation message about the game you are attempting to start.\n", NamedTextColor.YELLOW));

        // TODO: (Ayydxn) Display information about the game here such as all hunters and survivors.
        sender.sendMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit");

        sender.sendMessage(Component.text("\nIf you are sure you would like to start the game, please run:\n", NamedTextColor.YELLOW)
                .append(Component.text("/hunted start confirm", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));

        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static int startGame(CommandContext<CommandSourceStack> context, HuntedGameManager huntedGameManager)
    {
        CommandSender sender = context.getSource().getSender();

        if (!context.getArgument("confirm", String.class).equals("confirm"))
        {
            sender.sendMessage(Component.text("Did you mean to run \"/hunted start confirm\"?", NamedTextColor.GOLD));
            return Command.SINGLE_SUCCESS;
        }

        if (huntedGameManager.getCurrentGameState() == HuntedGameState.STARTING)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already starting!", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (huntedGameManager.getCurrentGameState() == HuntedGameState.ACTIVE)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already active!", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        huntedGameManager.startGame();

        return Command.SINGLE_SUCCESS;
    }
}
