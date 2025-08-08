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
import me.ayydxn.hunted.game.config.HuntedMatchSettings;
import me.ayydxn.hunted.teams.TeamUtils;
import me.ayydxn.hunted.teams.Teams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/**
 * The command responsible for starting a game of Hunted.
 */
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

        // Displays a confirmation message so that the user can ensure that the game is configured correctly before starting it.
        rootCommand.executes(context -> StartGameCommand.sendConfirmationMessage(context, this.gameManager));

        // Actually starts the game.
        rootCommand.then(Commands.argument("confirm", StringArgumentType.word())
                .executes(context -> StartGameCommand.startGame(context, this.gameManager)));

        return rootCommand;
    }

    private static int sendConfirmationMessage(CommandContext<CommandSourceStack> context, GameManager gameManager)
    {
        CommandSender sender = context.getSource().getSender();
        HuntedMatchSettings matchSettings = gameManager.getMatchSettings();

        sender.sendMessage(Component.text("This is a confirmation message about the game you are attempting to start.",
                NamedTextColor.YELLOW));

        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        StartGameCommand.displayMatchSettings(matchSettings, sender);

        // Display all the members within each team.
        for (Teams team : Teams.values())
        {
            if (team == Teams.UNKNOWN)
                continue;

            Component teamMembersComponent = TeamUtils.getTeamMembersText(team);
            sender.sendMessage(teamMembersComponent);
        }

        sender.sendMessage(Component.text("-----------------------------------------------------", NamedTextColor.DARK_GRAY));

        sender.sendMessage(Component.text("\nIf you are sure you would like to start the game, please run:\n", NamedTextColor.YELLOW)
                .append(Component.text("/hunted start confirm", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<CommandSourceStack> context, GameManager gameManager)
    {
        CommandSender sender = context.getSource().getSender();
        HuntedMatchSettings matchSettings = gameManager.getMatchSettings();

        // Check and make sure the user actually wants to start the game.
        if (!context.getArgument("confirm", String.class).equals("confirm"))
        {
            sender.sendMessage(Component.text("Did you mean to run \"/hunted start confirm\"?", NamedTextColor.GOLD));
            return -1;
        }

        // To prevent players from starting and running multiple games at the same time.
        if (gameManager.getCurrentMatchState() == MatchState.STARTING)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already starting!", NamedTextColor.RED));
            return -1;
        }

        if (gameManager.getCurrentMatchState() == MatchState.ACTIVE)
        {
            sender.sendMessage(Component.text("You cannot start a match of Minecraft Manhunt while one is already active!", NamedTextColor.RED));
            return -1;
        }

        // Actually start the game
        gameManager.startGame(matchSettings.selectedGameMode.getValue());

        return Command.SINGLE_SUCCESS;
    }

    private static void displayMatchSettings(HuntedMatchSettings matchSettings, CommandSender sender)
    {
        sender.sendMessage(Component.text("Selected Game Mode: ", NamedTextColor.GREEN)
                .append(Component.text(matchSettings.selectedGameMode.getValue().getDisplayName() + "\n", NamedTextColor.GOLD)));
    }
}
