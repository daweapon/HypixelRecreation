package dev.daweapon.type.game.game.event;

import dev.daweapon.type.game.game.Game;

public record GameStartEvent(Game<?> game) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return game;
    }
}
