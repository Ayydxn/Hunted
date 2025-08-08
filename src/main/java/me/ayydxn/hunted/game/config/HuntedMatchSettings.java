package me.ayydxn.hunted.game.config;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.HuntedGameMode;
import me.ayydxn.hunted.game.custom.mode.ClassicGameMode;

/**
 * Stores and manages all the settings that will be used when running a game of Hunted.
 */
public class HuntedMatchSettings
{
    public final MatchSetting<HuntedGameMode> selectedGameMode;

    private HuntedMatchSettings()
    {
        // Use the classic game mode as the default game mode
        this.selectedGameMode = new MatchSetting<>(GameModeRegistry.create(ClassicGameMode.ID, HuntedPlugin.getInstance()));
    }

    /**
     * Creates a new instance of this class with the default settings applied.
     *
     * @return A new instance of {@link HuntedMatchSettings} with default settings
     */
    public static HuntedMatchSettings defaults()
    {
        return new HuntedMatchSettings();
    }

    /**
     * A simple container to store an individual setting for a match.
     */
    public static final class MatchSetting<T>
    {
        private T value;

        public MatchSetting(T initialValue)
        {
            this.value = initialValue;
        }

        public T getValue()
        {
            return this.value;
        }

        public void setValue(T value)
        {
            this.value = value;
        }
    }
}
