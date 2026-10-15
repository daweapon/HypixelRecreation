package dev.daweapon.dungeons.catacombs.layout;

import java.util.Comparator;
import java.util.List;

public enum RoomShape {
    ONE_BY_ONE(new Cell(0, 0)),
    ONE_BY_TWO(new Cell(0, 0), new Cell(1, 0)),
    ONE_BY_THREE(new Cell(0, 0), new Cell(1, 0), new Cell(2, 0)),
    ONE_BY_FOUR(new Cell(0, 0), new Cell(1, 0), new Cell(2, 0), new Cell(3, 0)),
    TWO_BY_TWO(new Cell(0, 0), new Cell(1, 0), new Cell(0, 1), new Cell(1, 1)),
    L_SHAPE(new Cell(0, 0), new Cell(1, 0), new Cell(0, 1));

    private static final Comparator<Cell> ROW_MAJOR = Comparator.comparingInt(Cell::row).thenComparingInt(Cell::col);

    private final List<Cell> baseCells;

    RoomShape(Cell... baseCells) {
        this.baseCells = List.of(baseCells);
    }

    public List<Cell> baseCells() {
        return baseCells;
    }

    public int cellCount() {
        return baseCells.size();
    }

    public List<Cell> cells(Rotation rotation) {
        int minCol = minimum(rotation, true);
        int minRow = minimum(rotation, false);
        return baseCells.stream()
                .map(cell -> rawRotate(cell, rotation))
                .map(cell -> new Cell(cell.col() - minCol, cell.row() - minRow))
                .sorted(ROW_MAJOR)
                .toList();
    }

    public Cell rotate(Cell baseCell, Rotation rotation) {
        Cell rotated = rawRotate(baseCell, rotation);
        return new Cell(rotated.col() - minimum(rotation, true), rotated.row() - minimum(rotation, false));
    }

    public boolean containsBaseCell(Cell cell) {
        return baseCells.contains(cell);
    }

    public int widthCells(Rotation rotation) {
        return cells(rotation).stream().mapToInt(Cell::col).max().orElseThrow() + 1;
    }

    public int heightCells(Rotation rotation) {
        return cells(rotation).stream().mapToInt(Cell::row).max().orElseThrow() + 1;
    }

    private int minimum(Rotation rotation, boolean column) {
        return baseCells.stream()
                .map(cell -> rawRotate(cell, rotation))
                .mapToInt(cell -> column ? cell.col() : cell.row())
                .min()
                .orElseThrow();
    }

    private static Cell rawRotate(Cell cell, Rotation rotation) {
        Cell rotated = cell;
        for (int turn = 0; turn < rotation.quarterTurns(); turn++) {
            rotated = new Cell(-rotated.row(), rotated.col());
        }
        return rotated;
    }
}
