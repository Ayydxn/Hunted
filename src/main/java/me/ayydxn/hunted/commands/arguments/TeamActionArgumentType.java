package me.ayydxn.hunted.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.ayydxn.hunted.teams.TeamActions;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An {@link ArgumentType} implementation which allows us to use {@link TeamActions} in commands as arguments.
 */
public class TeamActionArgumentType implements CustomArgumentType<@NotNull TeamActions, @NotNull String>
{
    private TeamActionArgumentType()
    {
    }

    /**
     * Creates and returns a new instance of this class.
     *
     * @return A new instance of {@link TeamActionArgumentType}
     */
    public static TeamActionArgumentType teamAction()
    {
        return new TeamActionArgumentType();
    }

    @Override
    public @NotNull TeamActions parse(@NotNull StringReader stringReader) throws CommandSyntaxException
    {
        return switch (stringReader.readString())
        {
            case "add" -> TeamActions.ADD;
            case "remove" -> TeamActions.REMOVE;
            default -> TeamActions.UNKNOWN;
        };
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType()
    {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder)
    {
        for (TeamActions teamAction : TeamActions.values())
        {
            if (teamAction == TeamActions.UNKNOWN)
                continue;

            builder.suggest(teamAction.getName().toLowerCase());
        }

        return builder.buildFuture();
    }
}
