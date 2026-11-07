package dev.daweapon.type.catacombs.breaker;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import net.minestom.server.instance.block.Block;

import java.util.Optional;
import java.util.function.IntPredicate;

/**
 * Where the Dungeonbreaker may break a block (wiki "Dungeonbreaker", 0.23.5 patch notes): only inside rooms the party
 * has explored, never in puzzle rooms, never around room boundaries (so not through walls into another room or through
 * doors), never crypts or other run mechanisms, and in boss rooms only during the Wither Lords fight of Floor VII.
 */
public final class BreakerRules {
    public static final int BOUNDARY_MARGIN = 2;

    public enum Area {ROOM, ROOM_EDGE, UNEXPLORED_ROOM, PUZZLE_ROOM, BOSS_ARENA, OUTSIDE}

    private BreakerRules() {
    }

    public static boolean allows(Block block, Area area, boolean mechanism, CatacombsFloor floor) {
        if (mechanism || !breakable(block)) return false;
        return switch (area) {
            case ROOM -> true;
            case BOSS_ARENA -> bossRoomAllows(floor);
            case ROOM_EDGE, UNEXPLORED_ROOM, PUZZLE_ROOM, OUTSIDE -> false;
        };
    }

    public static boolean bossRoomAllows(CatacombsFloor floor) {
        return floor == CatacombsFloor.FLOOR_SEVEN;
    }

    /** Air, liquids, unbreakable blocks (bedrock, barriers) and anything with block data (chests, skulls, signs). */
    public static boolean breakable(Block block) {
        if (block.air() || block.liquid()) return false;
        if (block.hardness() < 0) return false;
        if (block.blockEntityType() != null) return false;
        return !block.compare(Block.LEVER) && !block.compare(Block.BARRIER) && !block.compare(Block.LIGHT);
    }

    /** Where a block column sits in the room grid; the boss arena lies outside it and is checked by the caller. */
    public static Area area(FloorLayout layout, IntPredicate explored, int x, int z) {
        int localX = x - CellGeometry.ORIGIN_X;
        int localZ = z - CellGeometry.ORIGIN_Z;
        if (localX < 0 || localZ < 0) return Area.OUTSIDE;
        int col = localX / CellGeometry.CELL_PITCH;
        int row = localZ / CellGeometry.CELL_PITCH;
        Optional<PlacedRoom> found = roomAt(layout, col, row);
        if (found.isEmpty()) return Area.OUTSIDE;
        PlacedRoom room = found.get();
        if (room.kind() == RoomKind.PUZZLE) return Area.PUZZLE_ROOM;
        if (!explored.test(room.index())) return Area.UNEXPLORED_ROOM;
        int offsetX = localX % CellGeometry.CELL_PITCH;
        int offsetZ = localZ % CellGeometry.CELL_PITCH;
        return nearForeignEdge(layout, room, col, row, offsetX, offsetZ) ? Area.ROOM_EDGE : Area.ROOM;
    }

    private static boolean nearForeignEdge(FloorLayout layout, PlacedRoom room, int col, int row, int offsetX, int offsetZ) {
        int fromX = offsetX < BOUNDARY_MARGIN ? -1 : 0;
        int toX = offsetX >= CellGeometry.ROOM_SIZE - BOUNDARY_MARGIN ? 1 : 0;
        int fromZ = offsetZ < BOUNDARY_MARGIN ? -1 : 0;
        int toZ = offsetZ >= CellGeometry.ROOM_SIZE - BOUNDARY_MARGIN ? 1 : 0;
        for (int dx = fromX; dx <= toX; dx++) {
            for (int dz = fromZ; dz <= toZ; dz++) {
                if (dx == 0 && dz == 0) continue;
                Optional<PlacedRoom> neighbour = roomAt(layout, col + dx, row + dz);
                if (neighbour.isEmpty() || neighbour.get().index() != room.index()) return true;
            }
        }
        return false;
    }

    private static Optional<PlacedRoom> roomAt(FloorLayout layout, int col, int row) {
        if (col < 0 || row < 0 || col >= layout.width() || row >= layout.height()) return Optional.empty();
        return layout.roomAt(new Cell(col, row));
    }
}
