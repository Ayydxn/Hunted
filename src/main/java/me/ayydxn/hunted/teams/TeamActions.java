package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents various team management actions that can be performed on players.
 * Each action encapsulates the logic for adding or removing players from teams, including validation and user feedback.
 * <p>
 * Each enum constant implements a command pattern where each action contains its own execution logic through an {@link ActionFunction} functional interface.
 */
public enum TeamActions
{
    /**
     * The action for adding a player to a team.
     *
     * <p>This action performs the following validation checks:</p>
     * <ul>
     *   <li>If the player is already a member of the target team</li>
     *   <li>If the player is already a member of a different team</li>
     * </ul>
     * <p>
     * If neither of these are true, the player is added to the team and both them and the action executor receive a confirmation message.</p>
     */
    ADD("Add", (actionExecutor, targetTeam, targetPlayer) ->
    {
        if (targetTeam.getMembers().contains(targetPlayer))
        {
            actionExecutor.sendMessage(Component.text(String.format("Player %s is already a part of team %s!", targetPlayer.getName(), targetTeam.getName()),
                    NamedTextColor.RED));

            return;
        }

        if (TeamActions.isPlayerAlreadyInADifferentTeam(targetPlayer, targetTeam))
        {
            actionExecutor.sendMessage(Component.text(String.format("Player %s is already a part of a different team %s!", targetPlayer.getName(), targetTeam.getName()),
                    NamedTextColor.RED));

            return;
        }

        targetTeam.addPlayer(targetPlayer);

        actionExecutor.sendMessage(Component.text(String.format("%s has been added to team %s!", targetPlayer.getName(), targetTeam.getName()),
                NamedTextColor.GREEN));

        targetPlayer.sendMessage(Component.text(String.format("You have been added to team %s!", targetTeam.getName()), NamedTextColor.GREEN));
    }),

    /**
     * The action for removing a player from a team.
     * <p>
     * This action check that the player is actually a member of the target team before trying to remove them.
     * If the player is not a member of the target team, an error message is sent.</p>
     * <p>
     * If player is a member of the target team however, the player is removed from the team and both them and the action executor receive a confirmation message.</p>
     */
    REMOVE("Remove", (actionExecutor, targetTeam, targetPlayer) ->
    {
        if (!targetTeam.getMembers().contains(targetPlayer))
        {
            actionExecutor.sendMessage(Component.text(String.format("Player %s is not a part of team %s and cannot be removed!", targetPlayer.getName(),
                    targetTeam.getName()), NamedTextColor.RED));

            return;
        }

        targetTeam.removePlayer(targetPlayer);

        actionExecutor.sendMessage(Component.text(String.format("%s has been removed from team %s!", targetPlayer.getName(), targetTeam.getName()),
                NamedTextColor.GREEN));

        targetPlayer.sendMessage(Component.text(String.format("You have been removed from team %s!", targetTeam.getName()), NamedTextColor.RED));
    }),

    /**
     * A placeholder action used for unknown or undefined team operations.
     * <p>
     * This action performs no operation and is used as a fallback to avoid using {@code null} in the aforementioned operations.
     */
    UNKNOWN("Unknown", (actionExecutor, targetTeam, targetPlayer) ->
    {
    });

    private final String name;
    private final ActionFunction actionFunction;

    /**
     * Constructs a new TeamAction with a given name and action function.
     *
     * @param name           The name of the action
     * @param actionFunction The functional interface implementation that defines the action's behavior
     */
    TeamActions(String name, ActionFunction actionFunction)
    {
        this.name = name;
        this.actionFunction = actionFunction;
    }

    /**
     * Returns the name of this action.
     *
     * @return The name of the action
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the functional interface implementation that defines this action's behavior.
     *
     * @return the action function for this team action
     */
    public ActionFunction getActionFunction()
    {
        return this.actionFunction;
    }

    /**
     * Returns if a player is already a member of a different team than the team that they are trying to join.
     * <p>
     * This method iterates through all available teams and checks if the joining player is already a member of any team other than the requested team.
     * The {@link Teams#UNKNOWN} team type is excluded from this check.</p>
     *
     * @param joiningPlayer the player to check for existing team membership
     * @param requestedTeam the team the player wants to join
     * @return {@code true} if the player is already in a different team, {@code false} otherwise
     */
    private static boolean isPlayerAlreadyInADifferentTeam(Player joiningPlayer, HuntedTeam requestedTeam)
    {
        boolean isPlayerAlreadyInADifferentTeam = false;

        for (Teams team : Teams.values())
        {
            // Skip Unknown and the team the player is trying to join
            if (team == Teams.UNKNOWN || team.getHandle().getName().equals(requestedTeam.getName()))
                continue;

            isPlayerAlreadyInADifferentTeam = team.getHandle().getMembers().contains(joiningPlayer);
        }

        return isPlayerAlreadyInADifferentTeam;
    }

    /**
     * Represents an operation that can be performed by a command sender on a team involving a specific player.
     *
     * @see CommandSender
     * @see HuntedTeam
     * @see Player
     */
    @FunctionalInterface
    public interface ActionFunction
    {
        /**
         * Executes the team action on the specified team and player.
         *
         * @param actionExecutor The entity (for example, player or the server) executing the action
         * @param targetTeam     The team on which the action is being performed
         * @param targetPlayer   The player who is the target of the action
         */
        void onActionPerformed(CommandSender actionExecutor, HuntedTeam targetTeam, Player targetPlayer);
    }
}
