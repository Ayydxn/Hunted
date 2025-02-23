package me.ayydxn.hunted.util;

import me.ayydxn.hunted.HuntedPlugin;
import net.kyori.adventure.text.Component;

public class ServerUtils
{
    public static void broadcastMessage(HuntedPlugin plugin, Component message)
    {
        plugin.getServer().broadcast(message);
    }
}
