package dev.daweapon.dungeons.catacombs.layout;

import java.util.Optional;

public final class CellGeometry {
    public static final int ROOM_SIZE = 31;
    public static final int CELL_PITCH = 32;
    public static final int ORIGIN_X = -200;
    public static final int ORIGIN_Z = -200;
    public static final int FLOOR_Y = 68;

    private static final int DOOR_OFFSET = ROOM_SIZE;
    private static final int ROOM_CENTER = ROOM_SIZE / 2;

    private CellGeometry() {
    }

    public record BlockPosition(int x, int y, int z) {
    }

    public record PlanarPoint(double x, double z) {
    }

    public static BlockPosition blockOrigin(Cell cell) {
        return new BlockPosition(ORIGIN_X + cell.col() * CELL_PITCH, FLOOR_Y, ORIGIN_Z + cell.row() * CELL_PITCH);
    }

    public static BlockPosition roomCenter(Cell cell) {
        BlockPosition origin = blockOrigin(cell);
        return new BlockPosition(origin.x() + ROOM_CENTER, origin.y(), origin.z() + ROOM_CENTER);
    }

    public static BlockPosition doorPosition(Cell from, Cell to) {
        if (!from.adjacentTo(to)) {
            throw new IllegalArgumentException("Cells " + from + " and " + to + " are not adjacent");
        }
        BlockPosition origin = blockOrigin(new Cell(Math.min(from.col(), to.col()), Math.min(from.row(), to.row())));
        if (from.row() == to.row()) {
            return new BlockPosition(origin.x() + DOOR_OFFSET, origin.y(), origin.z() + ROOM_CENTER);
        }
        return new BlockPosition(origin.x() + ROOM_CENTER, origin.y(), origin.z() + DOOR_OFFSET);
    }

    public static Optional<Cell> cellAt(int x, int z) {
        int localX = x - ORIGIN_X;
        int localZ = z - ORIGIN_Z;
        if (localX < 0 || localZ < 0) {
            return Optional.empty();
        }
        if (localX % CELL_PITCH == DOOR_OFFSET || localZ % CELL_PITCH == DOOR_OFFSET) {
            return Optional.empty();
        }
        return Optional.of(new Cell(localX / CELL_PITCH, localZ / CELL_PITCH));
    }

    public static int footprintBlocks(int cells) {
        return cells * CELL_PITCH - 1;
    }

    public static int widthBlocks(RoomShape shape, Rotation rotation) {
        return footprintBlocks(shape.widthCells(rotation));
    }

    public static int depthBlocks(RoomShape shape, Rotation rotation) {
        return footprintBlocks(shape.heightCells(rotation));
    }

    public static PlanarPoint rotateLocal(double x, double z, RoomShape shape, Rotation rotation) {
        double width = widthBlocks(shape, Rotation.DEG_0);
        double depth = depthBlocks(shape, Rotation.DEG_0);
        double currentX = x;
        double currentZ = z;
        for (int turn = 0; turn < rotation.quarterTurns(); turn++) {
            double nextX = depth - currentZ;
            double nextZ = currentX;
            currentX = nextX;
            currentZ = nextZ;
            double swap = width;
            width = depth;
            depth = swap;
        }
        return new PlanarPoint(currentX, currentZ);
    }
}
