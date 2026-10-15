package dev.daweapon.type.game.game.event;

import dev.daweapon.type.game.game.Game;

public record CountdownTickEvent(
        Game<?> game,
        int remainingSeconds,
        boolean shouldAnnounce
) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return game;
    }

    public boolean isComplete() {
        return remainingSeconds <= 0;
    }
}
