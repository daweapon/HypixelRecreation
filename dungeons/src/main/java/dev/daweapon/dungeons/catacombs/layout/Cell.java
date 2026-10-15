package dev.daweapon.dungeons.catacombs.layout;

public record Cell(int col, int row) {
    public Cell translate(int columns, int rows) {
        return new Cell(col + columns, row + rows);
    }

    public int distanceTo(Cell other) {
        return Math.abs(col - other.col) + Math.abs(row - other.row);
    }

    public boolean adjacentTo(Cell other) {
        return distanceTo(other) == 1;
    }
}
