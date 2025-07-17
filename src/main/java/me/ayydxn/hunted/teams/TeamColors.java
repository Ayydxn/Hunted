package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.format.NamedTextColor;

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
