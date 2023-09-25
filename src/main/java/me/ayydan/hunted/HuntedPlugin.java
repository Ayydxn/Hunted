package me.ayydan.hunted;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntedPlugin extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        this.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Welcome to Hunted!");
    }

    @Override
    public void onDisable()
    {
        this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Shutting down Hunted...");
    }
}
