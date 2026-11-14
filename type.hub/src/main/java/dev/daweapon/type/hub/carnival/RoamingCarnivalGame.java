package dev.daweapon.type.hub.carnival;

import net.minestom.server.coordinate.Pos;

/** A Carnival game played on foot around a dock; whoever ends up where they should not be is sent back to it. */
public interface RoamingCarnivalGame {
    Pos dock();

    boolean mustReturn(Pos position);
}
