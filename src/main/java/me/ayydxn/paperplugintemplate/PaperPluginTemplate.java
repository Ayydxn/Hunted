package me.ayydxn.paperplugintemplate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPluginTemplate extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        this.getServer().getConsoleSender().sendMessage(Component.text("Hello World!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable()
    {
        this.getServer().getConsoleSender().sendMessage(Component.text("Goodbye World!", NamedTextColor.RED));
    }
}
