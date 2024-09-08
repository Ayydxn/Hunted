package me.ayydxn.hunted.command;

import com.google.common.collect.Lists;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.ayydxn.hunted.HuntedPlugin;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HuntedCommandManager
{
    private static final List<HuntedCommand> REGISTERED_COMMANDS = Lists.newArrayList();

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommands()
    {
        HuntedPlugin.getHuntedLogger().info("Registering commands...");

        LifecycleEventManager<Plugin> lifecycleEventManager = HuntedPlugin.getInstance().getLifecycleManager();
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event ->
        {
            Commands commandsRegistrar = event.registrar();
            String packageName = HuntedCommandManager.class.getPackage().getName();

            for (Class<?> commandClass : new Reflections(packageName + ".impl").getSubTypesOf(HuntedCommand.class))
            {
                try
                {
                    HuntedCommand command = (HuntedCommand) commandClass.getDeclaredConstructor().newInstance();

                    commandsRegistrar.register(command.register(), command.getDescription(), command.getAliases());

                    REGISTERED_COMMANDS.add(command);
                }
                catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    public static List<HuntedCommand> getRegisteredCommands()
    {
        return REGISTERED_COMMANDS;
    }
}
