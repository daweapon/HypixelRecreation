package dev.daweapon.dungeons.catacombs.layout;

public enum Side {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public Side rotate(Rotation rotation) {
        return values()[(ordinal() + rotation.quarterTurns()) % values().length];
    }

    public Side opposite() {
        return values()[(ordinal() + 2) % values().length];
    }

    public static Side between(Cell from, Cell to) {
        if (to.col() > from.col()) return EAST;
        if (to.col() < from.col()) return WEST;
        return to.row() > from.row() ? SOUTH : NORTH;
    }
}
