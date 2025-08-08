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
import me.ayydxn.hunted.teams.HuntedTeam;
import me.ayydxn.hunted.teams.TeamActions;
import me.ayydxn.hunted.teams.TeamUtils;
import me.ayydxn.hunted.teams.Teams;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The root of the {@code /hunted teams} command tree which contains all team management related commands.
 *
 * @see HuntedTeam
 * @see Teams
 */
public class TeamsCommand implements AbstractHuntedCommand
{
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> createCommand()
    {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("teams");

        // Performs a given team actions on a given team.
        // This setup ensures that whenever new team actions are added, we don't have to come back here
        // and add a new branch to the command tree for that new action.
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

        // Simply sends a formatted message to the command sender which lists all the current members.
        rootCommand.then(Commands.literal("list").executes(context ->
        {
            for (Teams team : Teams.values())
            {
                if (team == Teams.UNKNOWN)
                    continue;

                Component teamMembersComponent = TeamUtils.getTeamMembersText(team);
                context.getSource().getSender().sendMessage(teamMembersComponent);
            }

            return Command.SINGLE_SUCCESS;
        }));

        return rootCommand;
    }
}
