package me.ayydxn.hunted.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.ayydxn.hunted.commands.base.BaseHuntedCommand;
import me.ayydxn.hunted.commands.game.StartGameCommand;
import me.ayydxn.hunted.commands.game.StopGameCommand;
import me.ayydxn.hunted.core.HuntedGameManager;

public class GlobalHuntedCommand implements BaseHuntedCommand
{
    private final HuntedGameManager huntedGameManager;

    public GlobalHuntedCommand(HuntedGameManager huntedGameManager)
    {
        this.huntedGameManager = huntedGameManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("hunted");
        rootCommand.then(new StartGameCommand(this.huntedGameManager).createCommand());
        rootCommand.then(new StopGameCommand(this.huntedGameManager).createCommand());

        return rootCommand;
    }
}
