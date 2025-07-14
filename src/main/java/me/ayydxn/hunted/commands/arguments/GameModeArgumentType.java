package me.ayydxn.hunted.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.HuntedGameMode;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class GameModeArgumentType implements CustomArgumentType<HuntedGameMode, String>
{
    private GameModeArgumentType()
    {
    }

    public static GameModeArgumentType huntedGameMode()
    {
        return new GameModeArgumentType();
    }

    @Override
    public @NotNull HuntedGameMode parse(StringReader reader) throws CommandSyntaxException
    {
        return GameModeRegistry.create(reader.readString(), HuntedPlugin.getInstance());
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder)
    {
        for (var gameModeID : GameModeRegistry.getRegisteredGameModes().keySet())
            builder.suggest(gameModeID);

        return builder.buildFuture();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType()
    {
        return StringArgumentType.word();
    }
}
