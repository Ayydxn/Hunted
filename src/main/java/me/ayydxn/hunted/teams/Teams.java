package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Represents the predefined teams that are available within the Hunted by the default.
 * Each enum constant corresponds to a distinct team with an associated {@link HuntedTeam} instance and metadata.
 * <p>
 * This enum provides a convenient, centralized definition for all possible team players can be a part of during a match.
 */
public enum Teams
{
    /**
     * The Hunters Team. These players are tasked with hunting down and killing all survivors before they can kill the Ender Dragon.
     */
    HUNTERS("Hunters", new HuntedTeam("Hunters", Component.text("[HUNTER]"), TeamColors.HUNTERS.getColor())),

    /**
     * The Survivors Team. These players are tasked with killing the Ender Dragon before the Hunters can kill them all.
     */
    SURVIVORS("Survivors", new HuntedTeam("Survivors", Component.text("[SURVIVOR]"), TeamColors.SURVIVORS.getColor())),

    /**
     * The Spectators Team. These players simply observe the game with no abilities to interact with it.
     */
    SPECTATORS("Spectators", new HuntedTeam("Spectators", Component.text("[SPECTATOR]"), TeamColors.SPECTATORS.getColor())),

    /**
     * The Unknown Team. This is a fallback placeholder team that should never be used in actual gameplay.
     * Its primary use for cases where returning {@code null} is not desirable.
     */
    UNKNOWN("Unknown", new HuntedTeam("Unknown", Component.text("[UNKNOWN]"), NamedTextColor.BLACK));

    private final String name;
    private final HuntedTeam handle;

    /**
     * Constructs a {@link Teams} enum constant with a given name and {@link HuntedTeam} instance.
     *
     * @param name The name of the team
     * @param handle The underlying {@link HuntedTeam} instance for the team
     */
    Teams(String name, HuntedTeam handle)
    {
        this.name = name;
        this.handle = handle;
    }

    /**
     * Returns the name of the team.
     *
     * @return The name of team
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the underlying {@link HuntedTeam} instance associated with this team.
     *
     * @return The underlying {@link HuntedTeam} instance
     */
    public HuntedTeam getHandle()
    {
        return this.handle;
    }
}
