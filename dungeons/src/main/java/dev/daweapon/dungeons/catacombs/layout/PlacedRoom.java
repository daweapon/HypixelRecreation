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

    public boolean allowsDoor(Cell cell, Side side) {
        return template.allowsDoor(rotation, new Cell(cell.col() - origin.col(), cell.row() - origin.row()), side);
    }

    public PlacedRoom withRotation(Rotation turned) {
        return new PlacedRoom(index, template, turned, origin, cells, onCriticalPath);
    }
}
