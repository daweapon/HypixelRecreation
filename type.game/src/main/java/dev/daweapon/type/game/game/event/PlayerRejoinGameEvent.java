package dev.daweapon.type.game.game.event;

import net.minestom.server.entity.Player;
import dev.daweapon.type.game.game.AbstractGame;
import dev.daweapon.type.game.game.Game;

public record PlayerRejoinGameEvent(
        Game<?> gameId,
        Player player,
        AbstractGame.DisconnectedPlayerData disconnectedPlayerData
) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return gameId;
    }
}
