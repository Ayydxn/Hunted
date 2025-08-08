package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

/**
 * A class which contains utility functions relating to the available {@link Teams}
 */
public class TeamUtils
{
    /**
     * Returns a stylized component of all the members within a team.
     *
     * @param team The team to list the members of
     * @return A stylized component message of all the team's members
     */
    public static Component getTeamMembersText(Teams team)
    {
        Component teamName = Component.text(team.getName(), team.getHandle().getTeamColor())
                .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED);
        Component teamMembersList = Component.text(builder ->
        {
            List<Player> teamMembers = team.getHandle().getMembers();
            if (teamMembers.isEmpty())
            {
                builder.append(Component.text("- No players present...\n", NamedTextColor.RED));
                return;
            }

            for (Player teamMember : teamMembers)
            {
                String teamMemberEntry = String.format("- %s", teamMember.getName());
                builder.append(Component.text(teamMemberEntry, NamedTextColor.GOLD)
                        .decorations(Set.of(TextDecoration.BOLD, TextDecoration.UNDERLINED), false));
            }

            builder.append(Component.text("\n"));
        });

        return teamName.append(Component.text("\n").append(teamMembersList));
    }
}
