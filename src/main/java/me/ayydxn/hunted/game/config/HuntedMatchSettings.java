package me.ayydxn.hunted.game.config;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.GameModeRegistry;
import me.ayydxn.hunted.game.HuntedGameMode;
import me.ayydxn.hunted.game.custom.mode.ClassicGameMode;

public class HuntedMatchSettings
{
    public final MatchSetting<HuntedGameMode> selectedGameMode;

    private HuntedMatchSettings()
    {
        this.selectedGameMode = new MatchSetting<>(GameModeRegistry.create(ClassicGameMode.ID, HuntedPlugin.getInstance()));
    }

    public static HuntedMatchSettings defaults()
    {
        return new HuntedMatchSettings();
    }

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
