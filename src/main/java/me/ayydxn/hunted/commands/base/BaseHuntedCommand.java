package me.ayydxn.hunted.commands.base;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface BaseHuntedCommand
{
    @SuppressWarnings("UnstableApiUsage")
    LiteralArgumentBuilder<CommandSourceStack> createCommand();
}
