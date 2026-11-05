package dev.daweapon.type.catacombs.crypt;

import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.MobSpawnPoint;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.SecretAnchor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Where the dungeon's crypts go: up to two tombs per standard room, seeded by the layout, and at least {@link #MINIMUM}
 * in the dungeon so the full crypt bonus is always reachable. A real room offers the crypt spots listed in its template;
 * a placeholder room offers its free corners.
 */
public final class CryptPlan {
    public static final int MINIMUM = 5;
    public static final int MAX_PER_ROOM = 2;
    public static final int TOMB_LENGTH = 2;
    private static final int[][] CORNERS = {{5, 5}, {24, 5}, {5, 24}, {24, 24}};
    private static final double CLEARANCE = 4;

    public record Crypt(int id, int room, int x, int y, int z) {
        public boolean covers(int blockX, int blockY, int blockZ) {
            return blockY == y && blockZ == z && blockX >= x && blockX < x + TOMB_LENGTH;
        }
    }

    private CryptPlan() {
    }

    public static List<Crypt> of(FloorLayout layout) {
        Map<PlacedRoom, List<int[]>> free = new LinkedHashMap<>();
        for (PlacedRoom room : layout.rooms()) {
            if (room.kind() == RoomKind.NORMAL || room.kind() == RoomKind.RARE) free.put(room, freeCorners(room));
        }
        Map<PlacedRoom, Integer> counts = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<PlacedRoom, List<int[]>> entry : free.entrySet()) {
            Random random = new Random(layout.seed() * 31 + entry.getKey().index());
            int count = Math.min(random.nextInt(MAX_PER_ROOM + 1), entry.getValue().size());
            counts.put(entry.getKey(), count);
            total += count;
        }
        for (PlacedRoom room : free.keySet()) {
            if (total >= MINIMUM) break;
            if (counts.get(room) < Math.min(MAX_PER_ROOM, free.get(room).size())) {
                counts.merge(room, 1, Integer::sum);
                total++;
            }
        }
        List<Crypt> crypts = new ArrayList<>();
        counts.forEach((room, count) -> {
            for (int index = 0; index < count; index++) crypts.add(place(crypts.size(), room, free.get(room).get(index)));
        });
        return List.copyOf(crypts);
    }

    private static List<int[]> freeCorners(PlacedRoom room) {
        if (!room.template().crypts().isEmpty()) {
            return room.template().crypts().stream().map(spot -> new int[]{spot.x(), spot.z(), spot.y()}).toList();
        }
        List<int[]> corners = new ArrayList<>();
        for (Cell cell : room.shape().baseCells()) {
            for (int[] corner : CORNERS) {
                int x = cell.col() * CellGeometry.CELL_PITCH + corner[0];
                int z = cell.row() * CellGeometry.CELL_PITCH + corner[1];
                if (clear(room, x, z)) corners.add(new int[]{x, z, 1});
            }
        }
        return corners;
    }

    private static boolean clear(PlacedRoom room, int x, int z) {
        for (SecretAnchor anchor : room.template().secretAnchors()) {
            if (near(anchor.x(), anchor.z(), x, z)) return false;
        }
        for (MobSpawnPoint spawn : room.template().mobSpawns()) {
            if (near(spawn.x(), spawn.z(), x, z)) return false;
        }
        return true;
    }

    private static boolean near(double ax, double az, int x, int z) {
        double dx = ax - (x + 1);
        double dz = az - (z + 0.5);
        return dx * dx + dz * dz < CLEARANCE * CLEARANCE;
    }

    private static Crypt place(int id, PlacedRoom room, int[] local) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        CellGeometry.PlanarPoint rotated = CellGeometry.rotateLocal(local[0] + 0.5, local[1] + 0.5, room.shape(), room.rotation());
        return new Crypt(id, room.index(), origin.x() + (int) Math.floor(rotated.x()), CellGeometry.FLOOR_Y + local[2],
                origin.z() + (int) Math.floor(rotated.z()));
    }
}
