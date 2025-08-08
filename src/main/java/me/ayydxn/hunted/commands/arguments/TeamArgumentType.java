package me.ayydxn.hunted.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.ayydxn.hunted.game.HuntedGameMode;
import me.ayydxn.hunted.teams.Teams;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * An {@link ArgumentType} implementation which allows us to use {@link me.ayydxn.hunted.teams.HuntedTeam}s in commands as arguments.
 */
public class TeamArgumentType implements CustomArgumentType<Teams, String>
{
    private TeamArgumentType()
    {
    }

    /**
     * Creates and returns a new instance of this class.
     *
     * @return A new instance of {@link TeamArgumentType}
     */
    public static TeamArgumentType huntedTeam()
    {
        return new TeamArgumentType();
    }

    @Override
    public @NotNull Teams parse(StringReader stringReader) throws CommandSyntaxException
    {
        return switch (stringReader.readString())
        {
            case "hunters" -> Teams.HUNTERS;
            case "survivors" -> Teams.SURVIVORS;
            case "spectators" -> Teams.SPECTATORS;
            default -> Teams.UNKNOWN;
        };
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder)
    {
        for (Teams huntedTeam : Teams.values())
        {
            if (huntedTeam == Teams.UNKNOWN)
                continue;

            builder.suggest(huntedTeam.getName().toLowerCase(Locale.ROOT));
        }

        return builder.buildFuture();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType()
    {
        return StringArgumentType.word();
    }
}
