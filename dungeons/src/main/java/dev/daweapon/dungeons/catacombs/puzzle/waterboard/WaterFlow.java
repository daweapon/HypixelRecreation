package dev.daweapon.dungeons.catacombs.puzzle.waterboard;

import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * Vanilla water flow in one vertical plane, as the Water Board runs it (every captured water block stayed in the board's
 * plane, one block every five ticks): water falls first; water that cannot fall spreads sideways, one weaker per block
 * and only toward the nearest drop within four blocks; water whose feed is cut dries up. Each {@link #step()} is one
 * five-tick water update.
 */
public final class WaterFlow {
    public static final int FULL = 8;
    private static final int SLOPE_FIND_DISTANCE = 4;
    private static final int NO_SLOPE = 1000;

    private final int width;
    private final int height;
    private final BiPredicate<Integer, Integer> open;
    private int[][] amount;
    private boolean[][] falling;
    private final boolean[][] source;

    /** {@code open.test(column, row)} says whether water can stand there; row 0 is the top. */
    public WaterFlow(int width, int height, BiPredicate<Integer, Integer> open) {
        this.width = width;
        this.height = height;
        this.open = open;
        this.amount = new int[width][height];
        this.falling = new boolean[width][height];
        this.source = new boolean[width][height];
    }

    public void setSource(int column, int row, boolean on) {
        source[column][row] = on;
        if (on) {
            amount[column][row] = FULL;
            falling[column][row] = false;
        } else {
            amount[column][row] = 0;
        }
    }

    /** Removes water from a cell that a block has just filled. */
    public void clear(int column, int row) {
        amount[column][row] = 0;
        falling[column][row] = false;
        source[column][row] = false;
    }

    public int amount(int column, int row) {
        return amount[column][row];
    }

    public boolean falling(int column, int row) {
        return falling[column][row];
    }

    public boolean source(int column, int row) {
        return source[column][row];
    }

    public boolean wet(int column, int row) {
        return amount[column][row] > 0;
    }

    /** One water update; true when any cell changed. */
    public boolean step() {
        int[][] nextAmount = copy(amount);
        boolean[][] nextFalling = copy(falling);
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                if (amount[column][row] == 0 || source[column][row]) continue;
                if (!passable(column, row)) {
                    nextAmount[column][row] = 0;
                    nextFalling[column][row] = false;
                    continue;
                }
                int[] fresh = liquidAt(column, row);
                nextAmount[column][row] = fresh[0];
                nextFalling[column][row] = fresh[1] == 1;
            }
        }
        int[][] spreading = copy(nextAmount);
        boolean[][] spreadingFalls = copy(nextFalling);
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                int here = spreading[column][row];
                if (here == 0 && !source[column][row]) continue;
                spread(column, row, source[column][row] ? FULL : here, !source[column][row] && spreadingFalls[column][row],
                        source[column][row], nextAmount, nextFalling);
            }
        }
        boolean changed = !Arrays.deepEquals(nextAmount, amount) || !Arrays.deepEquals(nextFalling, falling);
        amount = nextAmount;
        falling = nextFalling;
        return changed;
    }

    private void spread(int column, int row, int strength, boolean isFalling, boolean isSource, int[][] nextAmount, boolean[][] nextFalling) {
        int below = row + 1;
        if (passable(column, below)) {
            if (amount[column][below] == 0 && !source[column][below]) {
                fill(column, below, FULL, true, nextAmount, nextFalling);
                return;
            }
            if (!isSource) return;
        }
        int sideways = isFalling ? FULL - 1 : strength - 1;
        if (sideways <= 0) return;
        int best = NO_SLOPE;
        int[] slopes = new int[2];
        int[] directions = {-1, 1};
        for (int index = 0; index < 2; index++) {
            int next = column + directions[index];
            slopes[index] = NO_SLOPE + 1;
            if (!passable(next, row) || source[next][row]) continue;
            slopes[index] = passable(next, row + 1) ? 0 : slope(next, row, 1, -directions[index]);
            best = Math.min(best, slopes[index]);
        }
        for (int index = 0; index < 2; index++) {
            if (slopes[index] != best) continue;
            int next = column + directions[index];
            if (amount[next][row] > 0 || source[next][row]) continue;
            fill(next, row, sideways, false, nextAmount, nextFalling);
        }
    }

    private int slope(int column, int row, int depth, int cameFrom) {
        int best = NO_SLOPE;
        for (int direction : new int[]{-1, 1}) {
            if (direction == cameFrom) continue;
            int next = column + direction;
            if (!passable(next, row) || source[next][row]) continue;
            if (passable(next, row + 1)) return depth;
            if (depth < SLOPE_FIND_DISTANCE) best = Math.min(best, slope(next, row, depth + 1, -direction));
        }
        return best;
    }

    private static void fill(int column, int row, int strength, boolean isFalling, int[][] nextAmount, boolean[][] nextFalling) {
        if (isFalling) {
            nextAmount[column][row] = FULL;
            nextFalling[column][row] = true;
        } else if (!nextFalling[column][row] && strength > nextAmount[column][row]) {
            nextAmount[column][row] = strength;
        }
    }

    private int[] liquidAt(int column, int row) {
        if (row > 0 && amount[column][row - 1] > 0 && passable(column, row - 1)) return new int[]{FULL, 1};
        int strongest = 0;
        for (int direction : new int[]{-1, 1}) {
            int next = column + direction;
            if (!passable(next, row)) continue;
            strongest = Math.max(strongest, amount[next][row]);
        }
        int left = strongest - 1;
        return left <= 0 ? new int[]{0, 0} : new int[]{left, 0};
    }

    private boolean passable(int column, int row) {
        return column >= 0 && column < width && row >= 0 && row < height && open.test(column, row);
    }

    private static int[][] copy(int[][] grid) {
        int[][] copy = new int[grid.length][];
        for (int index = 0; index < grid.length; index++) copy[index] = grid[index].clone();
        return copy;
    }

    private static boolean[][] copy(boolean[][] grid) {
        boolean[][] copy = new boolean[grid.length][];
        for (int index = 0; index < grid.length; index++) copy[index] = grid[index].clone();
        return copy;
    }
}
