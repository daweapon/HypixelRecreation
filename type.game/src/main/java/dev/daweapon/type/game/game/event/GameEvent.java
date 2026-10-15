package dev.daweapon.type.game.game.event;

import net.minestom.server.event.Event;
import dev.daweapon.type.game.game.Game;

public interface GameEvent extends Event {
    Game<?> getGame();
}
