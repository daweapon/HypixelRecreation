package dev.daweapon.dungeons.catacombs.puzzle.boulder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Boulder: a grid of wooden boxes in front of the chest. A box moves one cell away from the button that is pressed,
 * only into an empty cell; pushed off the edge of the grid it disappears. The way is open once empty cells join the
 * front row (where the party comes in) to the back row (in front of the chest). Row 0 is the front.
 */
public final class BoulderBoard {
    public enum Face {
        NORTH(0, -1),
        SOUTH(0, 1),
        WEST(-1, 0),
        EAST(1, 0);

        private final int dx;
        private final int dz;

        Face(int dx, int dz) {
            this.dx = dx;
            this.dz = dz;
        }

        public int dx() {
            return dx;
        }

        public int dz() {
            return dz;
        }

        public Face opposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case WEST -> EAST;
                case EAST -> WEST;
            };
        }
    }

    public record Cell(int column, int row) {
        Cell step(Face toward) {
            return new Cell(column + toward.dx(), row + toward.dz());
        }
    }

    public record Press(Cell cell, Face face) {
    }

    public record Box(String wood, Set<Face> buttons) {
        public Box {
            buttons = Set.copyOf(buttons);
        }
    }

    public enum Push {
        MOVED,
        REMOVED,
        BLOCKED,
        NO_BUTTON
    }

    private final int columns;
    private final int rows;
    private final Map<Cell, Box> boxes = new LinkedHashMap<>();

    public BoulderBoard(int columns, int rows, Map<Cell, Box> boxes) {
        this.columns = columns;
        this.rows = rows;
        this.boxes.putAll(boxes);
    }

    public BoulderBoard copy() {
        return new BoulderBoard(columns, rows, boxes);
    }

    /** Presses the button on {@code face} of the box in {@code cell}: the box goes the other way. */
    public Push press(Cell cell, Face face) {
        Box box = boxes.get(cell);
        if (box == null || !box.buttons().contains(face)) return Push.NO_BUTTON;
        Cell target = cell.step(face.opposite());
        if (!inside(target)) {
            boxes.remove(cell);
            return Push.REMOVED;
        }
        if (boxes.containsKey(target)) return Push.BLOCKED;
        boxes.remove(cell);
        boxes.put(target, box);
        return Push.MOVED;
    }

    public boolean inside(Cell cell) {
        return cell.column() >= 0 && cell.column() < columns && cell.row() >= 0 && cell.row() < rows;
    }

    public boolean pathOpen() {
        return reachable().stream().anyMatch(cell -> cell.row() == rows - 1);
    }

    /** The empty cells a player can walk to from the front of the grid. */
    public Set<Cell> reachable() {
        Deque<Cell> queue = new ArrayDeque<>();
        Set<Cell> seen = new HashSet<>();
        for (int column = 0; column < columns; column++) {
            Cell front = new Cell(column, 0);
            if (!boxes.containsKey(front) && seen.add(front)) queue.add(front);
        }
        while (!queue.isEmpty()) {
            Cell cell = queue.poll();
            for (Face face : Face.values()) {
                Cell next = cell.step(face);
                if (inside(next) && !boxes.containsKey(next) && seen.add(next)) queue.add(next);
            }
        }
        return seen;
    }

    /** Whether a player coming from the front can stand at that button: in front of the grid or in a reachable cell. */
    public boolean pressable(Cell cell, Face face, Set<Cell> reachable) {
        Cell standing = cell.step(face);
        if (standing.row() == -1 && standing.column() >= 0 && standing.column() < columns) return true;
        return inside(standing) && reachable.contains(standing);
    }

    /** The fewest presses that open the way, found by searching every reachable arrangement; empty when none does. */
    public Optional<List<Press>> shortestSolution(int maxStates) {
        record State(BoulderBoard board, List<Press> pressed) {
        }
        Deque<State> queue = new ArrayDeque<>(List.of(new State(copy(), List.of())));
        Set<Map<Cell, Box>> seen = new HashSet<>(List.of(Map.copyOf(boxes)));
        while (!queue.isEmpty() && seen.size() < maxStates) {
            State state = queue.poll();
            if (state.board().pathOpen()) return Optional.of(state.pressed());
            Set<Cell> reachable = state.board().reachable();
            for (Map.Entry<Cell, Box> entry : state.board().boxes.entrySet()) {
                for (Face face : entry.getValue().buttons()) {
                    if (!state.board().pressable(entry.getKey(), face, reachable)) continue;
                    BoulderBoard next = state.board().copy();
                    if (next.press(entry.getKey(), face) == Push.BLOCKED) continue;
                    if (!seen.add(Map.copyOf(next.boxes))) continue;
                    List<Press> pressed = new ArrayList<>(state.pressed());
                    pressed.add(new Press(entry.getKey(), face));
                    queue.add(new State(next, pressed));
                }
            }
        }
        return Optional.empty();
    }

    public Map<Cell, Box> boxes() {
        return Map.copyOf(boxes);
    }

    public Optional<Box> at(Cell cell) {
        return Optional.ofNullable(boxes.get(cell));
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }
}
