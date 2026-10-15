package dev.daweapon.type.hub.carnival;

import net.minestom.server.coordinate.Pos;

/** A Carnival game played from a fixed spot; the player can look around but not walk. */
public interface StationaryCarnivalGame {
    Pos station();
}
