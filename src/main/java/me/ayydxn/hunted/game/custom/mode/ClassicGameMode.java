package me.ayydxn.hunted.game.custom.mode;

import me.ayydxn.hunted.HuntedPlugin;
import me.ayydxn.hunted.game.HuntedGameMode;
import me.ayydxn.hunted.game.HuntedGameState;
import me.ayydxn.hunted.teams.Teams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

/**
 * The classic game mode represents normal Minecraft Manhunt gameplay as you would know it from YouTubers like Dream. Therefore, it has little going for it
 * gameplay wise.
 */
public class ClassicGameMode extends HuntedGameMode
{
    public static final String ID = "classic";

    public ClassicGameMode(HuntedPlugin plugin)
    {
        super(plugin, new HuntedGameState());
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Component huntersObjectiveMessage = Component.text("Objective: Kill all the survivors before they kill the Ender Dragon!", NamedTextColor.GOLD);
        Component survivorsObjectiveMessage = Component.text("Objective: Run from the Hunters and kill the Ender Dragon!", NamedTextColor.GOLD);

        // (Ayydxn) Probably better to refer to the game state instead?
        for (Player hunter : Teams.HUNTERS.getHandle().getMembers())
            hunter.sendActionBar(huntersObjectiveMessage);

        for (Player survivor : Teams.SURVIVORS.getHandle().getMembers())
            survivor.sendActionBar(survivorsObjectiveMessage);
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
