package dev.daweapon.dungeons.catacombs.layout;

public record LayoutDoor(Cell from, Cell to, int fromRoom, int toRoom, DoorType type) {
    public boolean plain() {
        return type == DoorType.PLAIN;
    }

    public boolean witherDoor() {
        return type == DoorType.WITHER;
    }

    public boolean bloodDoor() {
        return type == DoorType.BLOOD;
    }

    public boolean connects(int room) {
        return fromRoom == room || toRoom == room;
    }

    public int otherRoom(int room) {
        return room == fromRoom ? toRoom : fromRoom;
    }
}
