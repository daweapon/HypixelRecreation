package dev.daweapon.type.game.game.event;

import dev.daweapon.type.game.game.Game;

public record CountdownCancelledEvent(
        Game<?> game,
	String reason
) implements GameEvent {
	@Override
    public Game<?> getGame() {
        return game;
	}
}
