package me.ayydxn.hunted.commands.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.commands.base.AbstractHuntedCommand;
import me.ayydxn.hunted.core.GameManager;
import me.ayydxn.hunted.core.GameStage;
import me.ayydxn.hunted.game.GameModeRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class StartGameCommand implements AbstractHuntedCommand
{
    private final GameManager gameManager;

    public StartGameCommand(GameManager gameManager)
    {
        this.gameManager = gameManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("start");
        rootCommand.executes(StartGameCommand::sendConfirmationMessage);
        rootCommand.then(Commands.argument("confirm", StringArgumentType.word())
                .executes(context -> StartGameCommand.startGame(context, this.gameManager)));

        return rootCommand;
    }

    private static int sendConfirmationMessage(CommandContext<CommandSourceStack> context)
    {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        sender.sendMessage(Component.text("This is a confirmation message about the game you are attempting to start.\n", NamedTextColor.YELLOW));

        sender.sendMessage("Selected Game Mode: TODO");

        // TODO: (Ayydxn) Display information about the game here such as all hunters and survivors.
        sender.sendMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit");

        sender.sendMessage(Component.text("\nIf you are sure you would like to start the game, please run:\n", NamedTextColor.YELLOW)
                .append(Component.text("/hunted start confirm", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));

        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<CommandSourceStack> context, GameManager gameManager)
    {
        CommandSender sender = context.getSource().getSender();

        if (!context.getArgument("confirm", String.class).equals("confirm"))
        {
            sender.sendMessage(Component.text("Did you mean to run \"/hunted start confirm\"?", NamedTextColor.GOLD));
            return -1;
        }

        if (gameManager.getCurrentGameStage() == GameStage.STARTING)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already starting!", NamedTextColor.RED));
            return -1;
        }

        if (gameManager.getCurrentGameStage() == GameStage.ACTIVE)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already active!", NamedTextColor.RED));
            return -1;
        }

        gameManager.startGame(GameModeRegistry.create("classic", HuntedPlugin.getInstance()));

        return Command.SINGLE_SUCCESS;
    }
}
