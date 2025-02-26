package me.ayydxn.hunted.commands.teams;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.ayydxn.hunted.commands.arguments.TeamArgumentType;
import me.ayydxn.hunted.commands.arguments.TeamActionArgumentType;
import me.ayydxn.hunted.commands.base.AbstractHuntedCommand;
import me.ayydxn.hunted.teams.TeamActions;
import me.ayydxn.hunted.teams.Teams;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamsCommand implements AbstractHuntedCommand
{
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("teams");
        rootCommand.then(Commands.argument("targetTeam", TeamArgumentType.huntedTeam())
                .then(Commands.argument("action", TeamActionArgumentType.teamAction())
                        .then(Commands.argument("targetPlayers", ArgumentTypes.players()).executes(context ->
                        {
                            PlayerSelectorArgumentResolver argumentResolver = context.getArgument("targetPlayers", PlayerSelectorArgumentResolver.class);

                            CommandSender sender = context.getSource().getSender();
                            Teams targetTeam = context.getArgument("targetTeam", Teams.class);
                            TeamActions action = context.getArgument("action", TeamActions.class);
                            List<Player> targetPlayers = argumentResolver.resolve(context.getSource());

                            for (Player targetPlayer : targetPlayers)
                                action.getActionFunction().onActionPerformed(sender, targetTeam.getHandle(), targetPlayer);

                            return Command.SINGLE_SUCCESS;
                        }))));

        return rootCommand;
    }
}
