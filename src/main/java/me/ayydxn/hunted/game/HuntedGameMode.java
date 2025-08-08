package me.ayydxn.hunted.game;

import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import me.ayydxn.hunted.HuntedPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.BiFunction;

/**
 * The base representation of a game mode within Hunted.
 * <p>
 * Similar to the <a href="https://dev.epicgames.com/documentation/en-us/unreal-engine/game-mode-and-game-state-in-unreal-engine">game mode system that exists within Unreal Engine</a>,
 * a game mode is a way to define the rules of a match. On a very basic level, these rules can be things like what are the maximum number of players allowed,
 * where do players spawn and whether friendly fire is allowed.
 * <p>
 * It is also responsible for defining the flow of the game and how certain events are handled. For example, if a player dies or a player joins while a match is
 * in progress, the game mode is responsible for handling and responding to that accordingly. Since this class implements the {@link Listener} interface from
 * Bukkit, subclasses are able to add whatever event handler methods they need to do just that.
 * <p>
 * While a game mode is perfectly capable of storing any state that results from these events, anything that needs to be sent all players in the match should be
 * relegated to and stored in a game state via the {@link HuntedGameState} class.
 */
public abstract class HuntedGameMode implements Listener
{
    protected final HuntedPlugin plugin;
    protected final HuntedGameState gameState;

    /**
     * @param plugin An instance of Hunted
     * @param gameState The game state implementation associated with this game mode. If a custom game state isn't used, you can simply pass a new instance of {@link HuntedGameState}
     */
    public HuntedGameMode(HuntedPlugin plugin, HuntedGameState gameState)
    {
        this.plugin = plugin;
        this.gameState = gameState;

        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when the game is starting.
     */
    public void onPreStart()
    {
        this.gameState.setGameStage(MatchState.STARTING);
    }

    /**
     * Called when the game starts.
     */
    public void onStart()
    {
        this.gameState.setGameStage(MatchState.ACTIVE);
    }

    /**
     * Called every time the game ticks.
     */
    public void onTick()
    {
        this.gameState.onTick();
    }

    /**
     * Called when the game is ending.
     */
    public void onPreEnd()
    {
        this.gameState.setGameStage(MatchState.ENDING);
    }

    /**
     * Called when the game ends.
     */
    public void onEnd()
    {
        this.gameState.setGameStage(MatchState.ENDED);
        this.gameState.reset();
    }

    /**
     * Called when a player joins while the game is active.
     */
    public abstract void onPlayerJoin(Player player);

    /**
     * Called when a player joins while the game is active.
     */
    public abstract void onPlayerLeave(Player player);

    /**
     * Returns the in-game display name of this game mode.
     *
     * @return Returns the game mode's in-game display name.
     */
    public abstract String getDisplayName();

    /**
     * Returns the description of this game mode.
     *
     * @return Returns game mode's description.
     */
    public abstract String getDescription();

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    private void onPlayerLoginValidation(PlayerConnectionValidateLoginEvent playerConnectionValidateLoginEvent)
    {
        BiFunction<String, String, Component> disconnectComponent = (presentTenseGameState, pastTenseGameState) ->
                Component.text(String.format("You cannot join while a game of Hunted is %s! Please wait until the game has %s.", presentTenseGameState, pastTenseGameState))
                        .color(NamedTextColor.RED);

        if (this.gameState.getMatchState() == MatchState.STARTING)
        {
            playerConnectionValidateLoginEvent.kickMessage(disconnectComponent.apply("starting", "started"));
            return;
        }

        if (this.gameState.getMatchState() == MatchState.ENDING)
        {
            playerConnectionValidateLoginEvent.kickMessage(disconnectComponent.apply("ending", "ended"));
            return;
        }

        playerConnectionValidateLoginEvent.allow();
    }

    @EventHandler
    private void onPlayerJoinImpl(PlayerJoinEvent playerJoinEvent)
    {
        if (this.gameState.getMatchState() == MatchState.ACTIVE)
            this.onPlayerJoin(playerJoinEvent.getPlayer());
    }

    @EventHandler
    private void onPlayerLeaveImpl(PlayerQuitEvent playerQuitEvent)
    {
        if (this.gameState.getMatchState() == MatchState.ACTIVE)
            this.onPlayerLeave(playerQuitEvent.getPlayer());
    }
}
