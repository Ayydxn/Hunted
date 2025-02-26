package me.ayydxn.hunted.commands.base;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

@FunctionalInterface
public interface AbstractHuntedCommand
{
    @SuppressWarnings("UnstableApiUsage")
    LiteralArgumentBuilder<CommandSourceStack> createCommand();
}
