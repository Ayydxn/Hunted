package me.ayydxn.hunted.commands.base;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * The base interface by which all Hunted commands are created.
 * <p>
 * Implementations are responsible for creating the Brigadier command tree that will be registered with Minecraft.
 */
@FunctionalInterface
public interface AbstractHuntedCommand
{
    /**
     * Creates and returns the root Brigadier command node for this command.
     *
     * @return The {@link LiteralArgumentBuilder} representing the root node for this command.
     */
    LiteralArgumentBuilder<CommandSourceStack> createCommand();
}
