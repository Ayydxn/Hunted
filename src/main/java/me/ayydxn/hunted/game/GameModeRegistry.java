package me.ayydxn.hunted.game;

import com.google.common.collect.Maps;
import me.ayydxn.hunted.HuntedPlugin;

import java.util.Map;

public class GameModeRegistry
{
    private static final Map<String, GameModeProvider> GAME_MODES = Maps.newHashMap();

    public static void register(String name, GameModeProvider gameModeFactory)
    {
        GAME_MODES.put(name, gameModeFactory);
    }

    public static HuntedGameMode create(String name, HuntedPlugin plugin)
    {
        GameModeProvider gameModeProvider = GAME_MODES.get(name);
        if (gameModeProvider != null)
            return gameModeProvider.create(plugin);

        throw new IllegalArgumentException(String.format("Failed to create unknown game mode '%s'!", name));
    }

    public static Map<String, GameModeProvider> getRegisteredGameModes()
    {
        return GAME_MODES;
    }
}
