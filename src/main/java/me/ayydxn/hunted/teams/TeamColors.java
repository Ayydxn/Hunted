package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * A simple enum which contains all the colors for each of the available {@link Teams} except for {@link Teams#UNKNOWN}
 */
public enum TeamColors
{
    HUNTERS(NamedTextColor.RED),
    SURVIVORS(NamedTextColor.AQUA),
    SPECTATORS(NamedTextColor.DARK_GRAY);

    private final NamedTextColor color;

    TeamColors(NamedTextColor color)
    {
        this.color = color;
    }

    public NamedTextColor getColor()
    {
        return this.color;
    }
}
