package net.swofty.type.skyblockgeneric.garden.greenhouse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Footprint {
    private Footprint() {
    }

    public static int sizeOf(String size) {
        if (size == null) return 1;
        int split = size.indexOf('x');
        if (split <= 0) return 1;
        try {
            return Math.max(1, Integer.parseInt(size.substring(0, split).trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static int anchorFor(int clicked, int size) {
        int offset = (size - 1) / 2;
        int column = clicked % GreenhouseData.SIZE - offset;
        int row = clicked / GreenhouseData.SIZE - offset;
        return inBounds(column, row, size) ? GreenhouseData.slot(column, row) : -1;
    }

    public static boolean inBounds(int column, int row, int size) {
        return column >= 0 && row >= 0 && column + size <= GreenhouseData.SIZE && row + size <= GreenhouseData.SIZE;
    }

    public static List<Integer> cells(int anchor, int size) {
        int column = anchor % GreenhouseData.SIZE;
        int row = anchor / GreenhouseData.SIZE;
        List<Integer> cells = new ArrayList<>();
        if (anchor < 0 || !inBounds(column, row, size)) return cells;
        for (int dz = 0; dz < size; dz++) {
            for (int dx = 0; dx < size; dx++) cells.add(GreenhouseData.slot(column + dx, row + dz));
        }
        return cells;
    }

    public static List<Integer> ring(int anchor, int size) {
        return surround(anchor, size, true);
    }

    public static List<Integer> edge(int anchor, int size) {
        return surround(anchor, size, false);
    }

    private static List<Integer> surround(int anchor, int size, boolean diagonals) {
        Set<Integer> own = new LinkedHashSet<>(cells(anchor, size));
        Set<Integer> result = new LinkedHashSet<>();
        for (int cell : own) {
            int column = cell % GreenhouseData.SIZE;
            int row = cell / GreenhouseData.SIZE;
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if ((dx == 0 && dz == 0) || (!diagonals && dx != 0 && dz != 0)) continue;
                    int c = column + dx;
                    int r = row + dz;
                    if (c < 0 || r < 0 || c >= GreenhouseData.SIZE || r >= GreenhouseData.SIZE) continue;
                    int slot = GreenhouseData.slot(c, r);
                    if (!own.contains(slot)) result.add(slot);
                }
            }
        }
        return new ArrayList<>(result);
    }
}
