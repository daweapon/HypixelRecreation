package dev.daweapon.dungeons.catacombs.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

final class RoomTiler {
    record Tile(RoomShape shape, Rotation rotation, List<Cell> cells) {
    }

    private final int width;
    private final int height;
    private final boolean[][] occupied;
    private final Map<Cell, Integer> pathIndex;
    private final Set<RoomShape> allowedShapes;
    private final Random random;

    private RoomTiler(int width, int height, boolean[][] occupied, Map<Cell, Integer> pathIndex,
                      Set<RoomShape> allowedShapes, Random random) {
        this.width = width;
        this.height = height;
        this.occupied = occupied;
        this.pathIndex = pathIndex;
        this.allowedShapes = allowedShapes;
        this.random = random;
    }

    static List<Tile> tile(int width, int height, boolean[][] reserved, Map<Cell, Integer> pathIndex,
                           Set<RoomShape> allowedShapes, Random random) {
        boolean[][] occupied = new boolean[width][height];
        for (int col = 0; col < width; col++) {
            occupied[col] = reserved[col].clone();
        }
        return new RoomTiler(width, height, occupied, pathIndex, allowedShapes, random).fill();
    }

    private List<Tile> fill() {
        List<Cell> freeCells = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (!occupied[col][row]) {
                    freeCells.add(new Cell(col, row));
                }
            }
        }
        Collections.shuffle(freeCells, random);
        List<Tile> tiles = new ArrayList<>();
        for (Cell cell : freeCells) {
            if (!occupied[cell.col()][cell.row()]) {
                Tile tile = place(cell);
                tile.cells().forEach(placed -> occupied[placed.col()][placed.row()] = true);
                tiles.add(tile);
            }
        }
        return tiles;
    }

    private Tile place(Cell cell) {
        for (RoomShape shape : shapeOrder()) {
            List<Tile> placements = placementsCovering(shape, cell);
            if (!placements.isEmpty()) {
                return placements.get(random.nextInt(placements.size()));
            }
        }
        return new Tile(RoomShape.ONE_BY_ONE, Rotation.DEG_0, List.of(cell));
    }

    private List<RoomShape> shapeOrder() {
        List<RoomShape> remaining = new ArrayList<>(allowedShapes.stream().sorted().toList());
        List<RoomShape> order = new ArrayList<>();
        while (!remaining.isEmpty()) {
            int totalWeight = remaining.stream().mapToInt(RoomTiler::weight).sum();
            int roll = random.nextInt(totalWeight);
            for (RoomShape shape : remaining) {
                roll -= weight(shape);
                if (roll < 0) {
                    order.add(shape);
                    remaining.remove(shape);
                    break;
                }
            }
        }
        return order;
    }

    private List<Tile> placementsCovering(RoomShape shape, Cell cell) {
        List<Tile> placements = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            List<Cell> shapeCells = shape.cells(rotation);
            for (Cell anchor : shapeCells) {
                int shiftCols = cell.col() - anchor.col();
                int shiftRows = cell.row() - anchor.row();
                List<Cell> placed = shapeCells.stream().map(shapeCell -> shapeCell.translate(shiftCols, shiftRows)).toList();
                if (fits(placed)) {
                    placements.add(new Tile(shape, rotation, placed));
                }
            }
        }
        return placements;
    }

    private boolean fits(List<Cell> cells) {
        for (Cell cell : cells) {
            if (cell.col() < 0 || cell.col() >= width || cell.row() < 0 || cell.row() >= height || occupied[cell.col()][cell.row()]) {
                return false;
            }
        }
        return pathCellsAreContiguous(cells);
    }

    private boolean pathCellsAreContiguous(List<Cell> cells) {
        List<Integer> indexes = cells.stream()
                .map(cell -> pathIndex.getOrDefault(cell, -1))
                .filter(index -> index >= 0)
                .sorted()
                .toList();
        for (int position = 1; position < indexes.size(); position++) {
            if (indexes.get(position) != indexes.get(position - 1) + 1) {
                return false;
            }
        }
        return true;
    }

    private static int weight(RoomShape shape) {
        return switch (shape) {
            case ONE_BY_ONE -> 50;
            case ONE_BY_TWO -> 20;
            case ONE_BY_THREE -> 8;
            case ONE_BY_FOUR -> 4;
            case TWO_BY_TWO -> 8;
            case L_SHAPE -> 10;
        };
    }
}
