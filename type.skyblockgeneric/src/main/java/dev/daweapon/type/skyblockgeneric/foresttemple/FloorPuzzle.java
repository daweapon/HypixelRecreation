package dev.daweapon.type.skyblockgeneric.foresttemple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class FloorPuzzle {
    public enum Facing {
        NORTH, EAST, SOUTH, WEST;

        public Facing next() {
            return values()[(ordinal() + 1) % 4];
        }

        public String property() {
            return name().toLowerCase();
        }
    }

    public static final int COLUMNS = 8;
    public static final int ROWS = 2;
    public static final int CELLS = COLUMNS * ROWS;
    public static final int MIN_X = -640;
    public static final int FIRST_Z = 75;
    public static final int FLOOR_Y = 59;

    private static final Facing[][] WALL = {
            parse("WWSWSWSS"),
            parse("NEENENNW")
    };

    private static final List<Facing[]> TARGETS = targets();

    private final Facing[] cells = new Facing[CELLS];

    private FloorPuzzle() {
    }

    public static FloorPuzzle scrambled(Random random) {
        FloorPuzzle puzzle = new FloorPuzzle();
        do {
            for (int i = 0; i < CELLS; i++) puzzle.cells[i] = Facing.values()[random.nextInt(4)];
        } while (puzzle.solved());
        return puzzle;
    }

    public static Facing[] solvedLayout() {
        return TARGETS.getFirst().clone();
    }

    public static int cellAt(int x, int z) {
        int column = x - MIN_X;
        int row = z - FIRST_Z;
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) return -1;
        return row * COLUMNS + column;
    }

    public static int x(int cell) {
        return MIN_X + cell % COLUMNS;
    }

    public static int z(int cell) {
        return FIRST_Z + cell / COLUMNS;
    }

    public Facing facing(int cell) {
        return cells[cell];
    }

    public void rotate(int cell) {
        cells[cell] = cells[cell].next();
    }

    public boolean solved() {
        for (Facing[] target : TARGETS) {
            if (java.util.Arrays.equals(cells, target)) return true;
        }
        return false;
    }

    private static Facing[] parse(String letters) {
        Facing[] row = new Facing[letters.length()];
        for (int i = 0; i < row.length; i++) {
            row[i] = switch (letters.charAt(i)) {
                case 'N' -> Facing.NORTH;
                case 'E' -> Facing.EAST;
                case 'S' -> Facing.SOUTH;
                default -> Facing.WEST;
            };
        }
        return row;
    }

    private static List<Facing[]> targets() {
        List<Facing[]> all = new ArrayList<>();
        for (boolean swapRows : new boolean[]{false, true}) {
            for (boolean flipColumns : new boolean[]{false, true}) {
                for (boolean mirror : new boolean[]{false, true}) {
                    for (int offset = 0; offset < 4; offset++) {
                        Facing[] target = new Facing[CELLS];
                        for (int row = 0; row < ROWS; row++) {
                            for (int column = 0; column < COLUMNS; column++) {
                                Facing wall = WALL[swapRows ? ROWS - 1 - row : row][flipColumns ? COLUMNS - 1 - column : column];
                                int index = mirror ? (4 - wall.ordinal()) % 4 : wall.ordinal();
                                target[row * COLUMNS + column] = Facing.values()[(index + offset) % 4];
                            }
                        }
                        all.add(target);
                    }
                }
            }
        }
        return all;
    }
}
