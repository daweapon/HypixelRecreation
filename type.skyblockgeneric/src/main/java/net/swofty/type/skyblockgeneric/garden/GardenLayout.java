package net.swofty.type.skyblockgeneric.garden;

import java.util.Set;
import java.util.TreeSet;

public final class GardenLayout {
    public static final int BARN = 0;
    public static final int[][] PLOT_GRID = {
            {21, 13, 9, 14, 22},
            {15, 5, 1, 6, 16},
            {10, 2, BARN, 3, 11},
            {17, 7, 4, 8, 18},
            {23, 19, 12, 20, 24}
    };

    private GardenLayout() {
    }

    public static Set<Integer> neighbours(int plot) {
        Set<Integer> neighbours = new TreeSet<>();
        for (int row = 0; row < PLOT_GRID.length; row++) {
            for (int column = 0; column < PLOT_GRID[row].length; column++) {
                if (PLOT_GRID[row][column] != plot) {
                    continue;
                }
                add(neighbours, row - 1, column);
                add(neighbours, row + 1, column);
                add(neighbours, row, column - 1);
                add(neighbours, row, column + 1);
            }
        }
        return neighbours;
    }

    public static boolean touchesBarnOrOwnedPlot(int plot, Set<Integer> owned) {
        for (int neighbour : neighbours(plot)) {
            if (neighbour == BARN || owned.contains(neighbour)) {
                return true;
            }
        }
        return false;
    }

    private static void add(Set<Integer> neighbours, int row, int column) {
        if (row >= 0 && row < PLOT_GRID.length && column >= 0 && column < PLOT_GRID[row].length) {
            neighbours.add(PLOT_GRID[row][column]);
        }
    }
}
