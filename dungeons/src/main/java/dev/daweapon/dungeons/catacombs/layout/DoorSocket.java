package dev.daweapon.dungeons.catacombs.layout;

public record DoorSocket(Cell cell, Side side) {
    public DoorSocket rotate(RoomShape shape, Rotation rotation) {
        return new DoorSocket(shape.rotate(cell, rotation), side.rotate(rotation));
    }
}
