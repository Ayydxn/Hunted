package me.ayydxn.hunted.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.commands.base.AbstractHuntedCommand;
import me.ayydxn.hunted.commands.game.GameModeCommand;
import me.ayydxn.hunted.commands.game.StartGameCommand;
import me.ayydxn.hunted.commands.game.StopGameCommand;
import me.ayydxn.hunted.commands.teams.TeamsCommand;
import me.ayydxn.hunted.game.GameManager;

/**
 * The root command for Hunted's command interface.
 * <p>
 * This is the top-level command by then which all commands used to interact with and configure Hunted are then accessed.
 */
public class GlobalHuntedCommand implements AbstractHuntedCommand
{
    private final GameManager gameManager;

    public GlobalHuntedCommand(GameManager gameManager)
    {
        this.gameManager = gameManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("hunted");
        rootCommand.then(new StartGameCommand(this.gameManager).createCommand());
        rootCommand.then(new StopGameCommand(this.gameManager).createCommand());
        rootCommand.then(new TeamsCommand().createCommand());
        rootCommand.then(new GameModeCommand().createCommand());

        return rootCommand;
    }
}
