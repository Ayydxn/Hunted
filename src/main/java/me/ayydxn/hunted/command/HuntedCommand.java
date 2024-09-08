package me.ayydxn.hunted.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;

public interface HuntedCommand
{
    @SuppressWarnings("UnstableApiUsage")
    LiteralCommandNode<CommandSourceStack> register();

    String getLabel();

    String getDescription();

    Collection<String> getAliases();
}
