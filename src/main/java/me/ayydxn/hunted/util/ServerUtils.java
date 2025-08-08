package me.ayydxn.hunted.util;

import me.ayydxn.hunted.HuntedPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;

public class ServerUtils
{
    /**
     * Simple wrapper method for doing {@link Server#broadcast}
     *
     * @param plugin An instance of Hunted
     * @param message The message to broadcast
     */
    public static void broadcastMessage(HuntedPlugin plugin, Component message)
    {
        plugin.getServer().broadcast(message);
    }
}
