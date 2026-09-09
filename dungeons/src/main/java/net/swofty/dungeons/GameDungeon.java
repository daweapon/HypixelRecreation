package net.swofty.dungeons;

// Base type for a generated dungeon layout, exposing its room count and a printable representation.
public abstract class GameDungeon {
    public abstract int getRoomCount();

    @Override
    public abstract String toString();
}
