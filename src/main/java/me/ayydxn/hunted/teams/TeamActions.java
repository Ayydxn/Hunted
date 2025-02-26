package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum TeamActions
{
    ADD("Add", (actionExecutor, targetTeam, targetPlayer) ->
    {
        if (targetTeam.getMembers().contains(targetPlayer))
        {
            actionExecutor.sendMessage(Component.text(String.format("Player %s is already a part of team %s!", targetPlayer.getName(), targetTeam.getName()),
                    NamedTextColor.RED));

            return;
        }

        targetTeam.addPlayer(targetPlayer);

        actionExecutor.sendMessage(Component.text(String.format("%s have been added to team %s!", targetPlayer.getName(), targetTeam.getName()),
                NamedTextColor.GREEN));

        targetPlayer.sendMessage(Component.text(String.format("You have been added to team %s!", targetTeam.getName()), NamedTextColor.GREEN));
    }),

    REMOVE("Remove", (actionExecutor, targetTeam, targetPlayer) ->
    {
        if (!targetTeam.getMembers().contains(targetPlayer))
        {
            actionExecutor.sendMessage(Component.text(String.format("Player %s is not a part of team %s and cannot be removed!", targetPlayer.getName(),
                            targetTeam.getName()), NamedTextColor.RED));

            return;
        }

        targetTeam.removePlayer(targetPlayer);

        actionExecutor.sendMessage(Component.text(String.format("%s have been removed from team %s!", targetPlayer.getName(), targetTeam.getName()),
                NamedTextColor.GREEN));

        targetPlayer.sendMessage(Component.text(String.format("You have been removed from team %s!", targetTeam.getName()), NamedTextColor.RED));
    }),

    UNKNOWN("Unknown", (actionExecutor, targetTeam, targetPlayer) -> {});

    private final String name;
    private final ActionFunction actionFunction;

    TeamActions(String name, ActionFunction actionFunction)
    {
        this.name = name;
        this.actionFunction = actionFunction;
    }

    public String getName()
    {
        return this.name;
    }

    public ActionFunction getActionFunction()
    {
        return this.actionFunction;
    }

    @FunctionalInterface
    public interface ActionFunction
    {
        // NOTE: (Ayydxn) Don't really like having a CommandSender here. Maybe a better way to do this?
        void onActionPerformed(CommandSender actionExecutor, HuntedTeam targetTeam, Player targetPlayer);
    }
}
