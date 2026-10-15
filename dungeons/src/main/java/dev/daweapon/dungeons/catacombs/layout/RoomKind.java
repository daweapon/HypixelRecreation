package dev.daweapon.dungeons.catacombs.layout;

public enum RoomKind {
    ENTRANCE(RoomColor.GREEN, 'S'),
    NORMAL(RoomColor.BROWN, 'N'),
    RARE(RoomColor.BROWN, 'R'),
    PUZZLE(RoomColor.PURPLE, 'P'),
    MINIBOSS(RoomColor.YELLOW, 'M'),
    TRAP(RoomColor.ORANGE, 'T'),
    FAIRY(RoomColor.PINK, 'F'),
    BLOOD(RoomColor.RED, 'B');

    private final RoomColor color;
    private final char symbol;

    RoomKind(RoomColor color, char symbol) {
        this.color = color;
        this.symbol = symbol;
    }

    public RoomColor color() {
        return color;
    }

    public char symbol() {
        return symbol;
    }

    public boolean requiresSingleCell() {
        return this != NORMAL;
    }
}
