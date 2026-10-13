package net.swofty.type.skyblockgeneric.garden.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DnaBoard {
    public static final int COLOURS = 4;
    public static final int COLUMNS = 7;

    private final int[][] cells = new int[COLOURS][COLUMNS];
    private final int[][] solution = new int[COLOURS][COLUMNS];
    private final int[] left = new int[COLOURS];
    private final int[] right = new int[COLOURS];

    private DnaBoard() {
    }

    public static DnaBoard generate(Random random) {
        DnaBoard board = new DnaBoard();
        List<Integer> order = new ArrayList<>(List.of(0, 1, 2, 3));
        do {
            Collections.shuffle(order, random);
            for (int row = 0; row < COLOURS; row++) board.left[row] = order.get(row);
            Collections.shuffle(order, random);
            for (int row = 0; row < COLOURS; row++) board.right[row] = order.get(row);
        } while (java.util.Arrays.equals(board.left, board.right));
        int[] column = board.left.clone();
        for (int col = 0; col < COLUMNS; col++) {
            for (int row = 0; row < COLOURS; row++) board.solution[row][col] = column[row];
            sortStep(column, board.right, col % 2);
        }
        do {
            for (int col = 0; col < COLUMNS; col++) {
                List<Integer> shuffled = new ArrayList<>(List.of(0, 1, 2, 3));
                Collections.shuffle(shuffled, random);
                for (int row = 0; row < COLOURS; row++) board.cells[row][col] = shuffled.get(row);
            }
        } while (board.solved());
        return board;
    }

    private static void sortStep(int[] column, int[] target, int parity) {
        for (int i = parity; i + 1 < column.length; i += 2) {
            if (rank(target, column[i]) > rank(target, column[i + 1])) {
                int swap = column[i];
                column[i] = column[i + 1];
                column[i + 1] = swap;
            }
        }
    }

    private static int rank(int[] target, int colour) {
        for (int i = 0; i < target.length; i++) if (target[i] == colour) return i;
        return -1;
    }

    public int get(int row, int col) {
        return cells[row][col];
    }

    public int left(int row) {
        return left[row];
    }

    public int right(int row) {
        return right[row];
    }

    public boolean swap(int col, int rowA, int rowB) {
        if (col < 0 || col >= COLUMNS || Math.abs(rowA - rowB) != 1 || rowA < 0 || rowB < 0 || rowA >= COLOURS || rowB >= COLOURS) return false;
        int swap = cells[rowA][col];
        cells[rowA][col] = cells[rowB][col];
        cells[rowB][col] = swap;
        return true;
    }

    public void solve() {
        for (int row = 0; row < COLOURS; row++) cells[row] = solution[row].clone();
    }

    public boolean solved() {
        for (int colour = 0; colour < COLOURS; colour++) {
            if (!connected(colour)) return false;
        }
        return true;
    }

    private boolean connected(int colour) {
        boolean[] reach = new boolean[COLOURS];
        int leftRow = rank(left, colour);
        for (int row = 0; row < COLOURS; row++) reach[row] = cells[row][0] == colour && Math.abs(row - leftRow) <= 1;
        for (int col = 1; col < COLUMNS; col++) {
            boolean[] next = new boolean[COLOURS];
            for (int row = 0; row < COLOURS; row++) {
                if (cells[row][col] != colour) continue;
                for (int from = Math.max(0, row - 1); from <= Math.min(COLOURS - 1, row + 1); from++) {
                    if (reach[from]) next[row] = true;
                }
            }
            reach = next;
        }
        int rightRow = rank(right, colour);
        for (int row = 0; row < COLOURS; row++) {
            if (reach[row] && Math.abs(row - rightRow) <= 1) return true;
        }
        return false;
    }
}
