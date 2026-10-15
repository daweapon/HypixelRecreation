package dev.daweapon.type.bedwarsgame.events.custom;

import dev.daweapon.type.game.game.Game;
import dev.daweapon.type.game.game.event.GameEvent;

// Custom game event fired when the Bedwars match timeline advances to its next scheduled event.
public record BedWarsGameEventAdvanceEvent(
        Game<?> game,
    String previousEvent,
    String currentEvent,
    long secondsUntilNext
) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return game;
    }
}
