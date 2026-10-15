package dev.daweapon.dungeons.catacombs.layout;

import java.util.List;

public record PlacedRoom(
        int index,
        RoomTemplate template,
        Rotation rotation,
        Cell origin,
        List<Cell> cells,
        boolean onCriticalPath
) {
    public PlacedRoom {
        cells = List.copyOf(cells);
    }

    public RoomKind kind() {
        return template.kind();
    }

    public RoomColor color() {
        return template.kind().color();
    }

    public RoomShape shape() {
        return template.shape();
    }

    public int secretCount() {
        return template.secretCount();
    }

    public boolean occupies(Cell cell) {
        return cells.contains(cell);
    }
}
