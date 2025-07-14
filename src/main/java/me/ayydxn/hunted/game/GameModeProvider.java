package me.ayydxn.hunted.game;

import me.ayydxn.hunted.HuntedPlugin;

@FunctionalInterface
public interface GameModeProvider
{
    HuntedGameMode create(HuntedPlugin plugin);
}
