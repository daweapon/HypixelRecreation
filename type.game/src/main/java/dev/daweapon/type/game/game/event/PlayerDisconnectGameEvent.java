package dev.daweapon.type.game.game.event;

import net.minestom.server.entity.Player;
import dev.daweapon.type.game.game.Game;

/**
 * Event fired when a player disconnects during an active game.
 * Different from PlayerLeaveGameEvent - this tracks disconnection for potential rejoin.
 */
public record PlayerDisconnectGameEvent(
        Game<?> game,
        Player player,
        boolean canRejoin
) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return game;
    }
}
