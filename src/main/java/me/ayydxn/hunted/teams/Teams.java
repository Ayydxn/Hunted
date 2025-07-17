package me.ayydxn.hunted.teams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum Teams
{
    HUNTERS("Hunters", new HuntedTeam("Hunters", Component.text("[HUNTER]"), TeamColors.HUNTERS.getColor())),
    SURVIVORS("Survivors", new HuntedTeam("Survivors", Component.text("[SURVIVOR]"), TeamColors.SURVIVORS.getColor())),
    SPECTATORS("Spectators", new HuntedTeam("Spectators", Component.text("[SPECTATOR]"), TeamColors.SPECTATORS.getColor())),

    // (Ayydxn) Should NEVER be used in-game in any way. It's just a placeholder team for places where returning null isn't really the best thing to do.
    UNKNOWN("Unknown", new HuntedTeam("Unknown", Component.text("[UNKNOWN]"), NamedTextColor.BLACK));

    private final String name;
    private final HuntedTeam handle;

    Teams(String name, HuntedTeam handle)
    {
        this.name = name;
        this.handle = handle;
    }

    public String getName()
    {
        return this.name;
    }

    public HuntedTeam getHandle()
    {
        return this.handle;
    }
}
