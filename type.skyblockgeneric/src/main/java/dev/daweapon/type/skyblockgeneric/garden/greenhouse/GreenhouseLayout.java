package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.garden.GardenLayout;
import org.jetbrains.annotations.Nullable;

public final class GreenhouseLayout {
    public static final int BED_X = 43;
    public static final int BED_Z = 43;
    public static final int FLOOR_Y = 73;
    public static final int CROP_Y = 74;
    public static final int CARPENTER_X = 49;
    public static final int CARPENTER_Z = 41;
    public static final float CARPENTER_YAW = 11;

    public record Spot(int plot, int slot) {
        public int column() {
            return slot % GreenhouseData.SIZE;
        }

        public int row() {
            return slot / GreenhouseData.SIZE;
        }
    }

    private GreenhouseLayout() {
    }

    public static int worldX(int plot, int column) {
        return GardenLayout.plotOrigin(plot)[0] + BED_X + column;
    }

    public static int worldZ(int plot, int row) {
        return GardenLayout.plotOrigin(plot)[1] + BED_Z + row;
    }

    public static Pos carpenter(int plot) {
        int[] origin = GardenLayout.plotOrigin(plot);
        return new Pos(origin[0] + CARPENTER_X + 0.5, FLOOR_Y, origin[1] + CARPENTER_Z + 0.5, CARPENTER_YAW, 0);
    }

    public static Pos jake(int plot) {
        int[] origin = GardenLayout.plotOrigin(plot);
        return new Pos(origin[0] + CARPENTER_X + 3.5, FLOOR_Y, origin[1] + CARPENTER_Z + 0.5, -20, 0);
    }

    public static Pos analyzer(int plot) {
        int[] origin = GardenLayout.plotOrigin(plot);
        return new Pos(origin[0] + CARPENTER_X - 3 + 0.5, FLOOR_Y, origin[1] + CARPENTER_Z + 0.5, 0, 0);
    }

    public static @Nullable Spot spotAt(Point point, int plot) {
        int[] origin = GardenLayout.plotOrigin(plot);
        if (origin == null) return null;
        int column = point.blockX() - origin[0] - BED_X;
        int row = point.blockZ() - origin[1] - BED_Z;
        if (column < 0 || row < 0 || column >= GreenhouseData.SIZE || row >= GreenhouseData.SIZE) return null;
        return new Spot(plot, GreenhouseData.slot(column, row));
    }

    public static int[][] neighbours(int slot) {
        int column = slot % GreenhouseData.SIZE;
        int row = slot / GreenhouseData.SIZE;
        int[][] all = {{column - 1, row}, {column + 1, row}, {column, row - 1}, {column, row + 1}};
        return java.util.Arrays.stream(all)
                .filter(cell -> cell[0] >= 0 && cell[1] >= 0 && cell[0] < GreenhouseData.SIZE && cell[1] < GreenhouseData.SIZE)
                .toArray(int[][]::new);
    }

    public static int[][] around(int slot) {
        int column = slot % GreenhouseData.SIZE;
        int row = slot / GreenhouseData.SIZE;
        java.util.List<int[]> cells = new java.util.ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int c = column + dx;
                int r = row + dz;
                if (c >= 0 && r >= 0 && c < GreenhouseData.SIZE && r < GreenhouseData.SIZE) cells.add(new int[]{c, r});
            }
        }
        return cells.toArray(int[][]::new);
    }
}
