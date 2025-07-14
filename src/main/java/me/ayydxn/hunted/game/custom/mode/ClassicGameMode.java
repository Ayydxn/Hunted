package me.ayydxn.hunted.game.custom.mode;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.HuntedGameMode;
import me.ayydxn.hunted.game.HuntedGameState;
import org.bukkit.entity.Player;

public class ClassicGameMode extends HuntedGameMode
{
    public ClassicGameMode(HuntedPlugin plugin)
    {
        super(plugin, new HuntedGameState());
    }

    @Override
    public void onTick()
    {
        super.onTick();
    }

    @Override
    public void onPlayerJoin(Player player)
    {
    }

    @Override
    public void onPlayerLeave(Player player)
    {
    }

    @Override
    public String getDisplayName()
    {
        return "Classic";
    }

    @Override
    public String getDescription()
    {
        return """
                Classic Minecraft Manhunt as you know it. Hunters win by killing all survivors. Survivors win by killing the Ender Dragon and beating the game.\
                
                
                Hunters get infinite lives and a compass which points to the survivor nearest. On the other hand Survivors get nothing and only 1 life. All with no time limit.""";
    }
}
