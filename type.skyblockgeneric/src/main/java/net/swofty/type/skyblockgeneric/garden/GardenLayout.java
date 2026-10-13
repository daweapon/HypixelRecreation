package net.swofty.type.skyblockgeneric.garden;

import java.util.ArrayList;
import java.util.List;
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

    public static final int NONE = -1;
    public static final int PLOT_SIZE = 96;
    public static final int ORIGIN = -48;
    private static final int GRID_LOW = ORIGIN - PLOT_SIZE * 2;

    public record PlotSign(int plot, double x, double z) {
    }

    private GardenLayout() {
    }

    public static int plotAt(double x, double z) {
        int column = Math.floorDiv((int) Math.floor(x) - GRID_LOW, PLOT_SIZE);
        int row = Math.floorDiv((int) Math.floor(z) - GRID_LOW, PLOT_SIZE);
        if (row < 0 || row >= PLOT_GRID.length || column < 0 || column >= PLOT_GRID[row].length) {
            return NONE;
        }
        return PLOT_GRID[row][column];
    }

    public static List<PlotSign> signs() {
        List<PlotSign> signs = new ArrayList<>();
        int size = PLOT_GRID.length;
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (column + 1 < size) {
                    double x = GRID_LOW + PLOT_SIZE * (column + 1);
                    double z = centre(row);
                    signs.add(new PlotSign(outer(row, column, row, column + 1), x, z));
                }
                if (row + 1 < size) {
                    double x = centre(column);
                    double z = GRID_LOW + PLOT_SIZE * (row + 1);
                    signs.add(new PlotSign(outer(row, column, row + 1, column), x, z));
                }
            }
        }
        return signs;
    }

    private static double centre(int index) {
        return PLOT_SIZE * (index - PLOT_GRID.length / 2);
    }

    private static int outer(int rowA, int columnA, int rowB, int columnB) {
        int centreIndex = PLOT_GRID.length / 2;
        int distanceA = Math.abs(rowA - centreIndex) + Math.abs(columnA - centreIndex);
        int distanceB = Math.abs(rowB - centreIndex) + Math.abs(columnB - centreIndex);
        return distanceA > distanceB ? PLOT_GRID[rowA][columnA] : PLOT_GRID[rowB][columnB];
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
