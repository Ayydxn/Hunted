package me.ayydxn.hunted.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class HuntedLogger
{
    private final ConsoleCommandSender consoleSender;
    private final String loggerName;

    public HuntedLogger(String name)
    {
        this.consoleSender = Bukkit.getServer().getConsoleSender();
        this.loggerName = name;
    }

    public void trace(String message, Object ... arguments)
    {
        TextComponent traceLogMessage = Component.text("")
                .color(NamedTextColor.GRAY)
                .append(Component.text(String.format("[TRACE] (%s) ", this.loggerName)))
                .append(Component.text(ParameterizedMessage.format(message, arguments)));

        this.consoleSender.sendMessage(traceLogMessage);
    }

    public void info(String message, Object ... arguments)
    {
        TextComponent infoLogMessage = Component.text("")
                .color(NamedTextColor.GREEN)
                .append(Component.text(String.format("[INFO] (%s) ", this.loggerName)))
                .append(Component.text(ParameterizedMessage.format(message, arguments)));

        this.consoleSender.sendMessage(infoLogMessage);
    }

    public void debug(String message, Object ... arguments)
    {
        TextComponent debugLogMessage = Component.text("")
                .color(NamedTextColor.BLUE)
                .append(Component.text(String.format("[DEBUG] (%s) ", this.loggerName)))
                .append(Component.text(ParameterizedMessage.format(message, arguments)));

        this.consoleSender.sendMessage(debugLogMessage);
    }

    public void warn(String message, Object ... arguments)
    {
        TextComponent warnLogMessage = Component.text("")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(String.format("[WARN] (%s) ", this.loggerName)))
                .append(Component.text(ParameterizedMessage.format(message, arguments)));

        this.consoleSender.sendMessage(warnLogMessage);
    }

    public void error(String message, Object ... arguments)
    {
        TextComponent errorLogMessage = Component.text("")
                .color(NamedTextColor.RED)
                .append(Component.text(String.format("[ERROR] (%s) ", this.loggerName)))
                .append(Component.text(ParameterizedMessage.format(message, arguments)));

        this.consoleSender.sendMessage(errorLogMessage);
    }

    public void fatal(String message, Object ... arguments)
    {
        TextComponent fatalLogMessage = Component.text("")
                .color(NamedTextColor.DARK_RED)
                .append(Component.text(String.format("[FATAL] (%s) ", this.loggerName)))
                .append(Component.text(ParameterizedMessage.format(message, arguments)));

        this.consoleSender.sendMessage(fatalLogMessage);
    }
}
