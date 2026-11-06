package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomColor;
import dev.daweapon.dungeons.catacombs.layout.StandPoint;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every block of one run's dungeon: real rooms come from their block files, turned and moved into place, and
 * everything else (placeholder rooms, the strips between rooms, doorways) comes from {@link CatacombsBlockPlan}. A door
 * into a real room cuts its 3 by 4 opening through that room's outer wall.
 */
public final class CatacombsArenaBlocks {
    private static final int PITCH = CellGeometry.CELL_PITCH;
    private static final int ROOM = CellGeometry.ROOM_SIZE;
    private static final int STANDING_OFFSET = CatacombsBlockPlan.MARKER_RADIUS + 2;

    private final FloorLayout layout;
    private final CatacombsBlockPlan plan;
    private final Map<Integer, RealRoom> realRooms;
    private final Set<Long> openings;
    private final DoorGates gates;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    private CatacombsArenaBlocks(FloorLayout layout, CatacombsBlockPlan plan, Map<Integer, RealRoom> realRooms) {
        this.layout = layout;
        this.plan = plan;
        this.realRooms = realRooms;
        this.openings = openings(layout, realRooms);
        this.gates = DoorGates.of(layout);
        int lowX = plan.minX(), highX = plan.maxX(), lowY = plan.minY(), highY = plan.maxY(), lowZ = plan.minZ(), highZ = plan.maxZ();
        for (RealRoom room : realRooms.values()) {
            lowX = Math.min(lowX, room.minX());
            highX = Math.max(highX, room.maxX());
            lowY = Math.min(lowY, room.minY());
            highY = Math.max(highY, room.maxY());
            lowZ = Math.min(lowZ, room.minZ());
            highZ = Math.max(highZ, room.maxZ());
        }
        this.minX = lowX;
        this.maxX = highX;
        this.minY = lowY;
        this.maxY = highY;
        this.minZ = lowZ;
        this.maxZ = highZ;
    }

    public static CatacombsArenaBlocks of(FloorLayout layout, RoomStructures structures) {
        Map<Integer, RealRoom> realRooms = new HashMap<>();
        for (PlacedRoom room : layout.rooms()) {
            room.template().blocks().ifPresent(structure -> {
                try {
                    realRooms.put(room.index(), RealRoom.place(room, structures.load(structure)));
                } catch (RuntimeException e) {
                    Logger.warn(e, "Room {} falls back to a placeholder: its blocks could not be loaded", room.template().id());
                }
            });
        }
        return new CatacombsArenaBlocks(layout, new CatacombsBlockPlan(layout), Map.copyOf(realRooms));
    }

    public CatacombsBlockPlan plan() {
        return plan;
    }

    public DoorGates gates() {
        return gates;
    }

    public boolean isReal(PlacedRoom room) {
        return realRooms.containsKey(room.index());
    }

    public List<RealRoom> realRooms() {
        return List.copyOf(realRooms.values());
    }

    public int minX() {
        return minX;
    }

    public int maxX() {
        return maxX;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxZ() {
        return maxZ;
    }

    /** Where a player stands in a room: the real room's stand point, or beside a placeholder's centre marker. */
    public Pos standPoint(PlacedRoom room) {
        if (isReal(room) && room.template().stand().isPresent()) {
            StandPoint stand = room.template().stand().get();
            CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
            CellGeometry.PlanarPoint local = CellGeometry.rotateLocal(stand.x(), stand.z(), room.shape(), room.rotation());
            return new Pos(origin.x() + local.x(), origin.y() + stand.y(), origin.z() + local.z());
        }
        CellGeometry.BlockPosition centre = CellGeometry.roomCenter(room.cells().getFirst());
        return new Pos(centre.x() + 0.5, centre.y() + 1, centre.z() + 0.5 + STANDING_OFFSET);
    }

    /** The real room that owns this block column, or null where the placeholder plan decides. */
    public RealRoom ownerAt(int x, int z) {
        int localX = x - CellGeometry.ORIGIN_X;
        int localZ = z - CellGeometry.ORIGIN_Z;
        int col = Math.floorDiv(localX, PITCH);
        int row = Math.floorDiv(localZ, PITCH);
        boolean gapX = Math.floorMod(localX, PITCH) == ROOM;
        boolean gapZ = Math.floorMod(localZ, PITCH) == ROOM;
        List<Cell> touching = new ArrayList<>(4);
        touching.add(new Cell(col, row));
        if (gapX) touching.add(new Cell(col + 1, row));
        if (gapZ) touching.add(new Cell(col, row + 1));
        if (gapX && gapZ) touching.add(new Cell(col + 1, row + 1));
        Integer owner = null;
        for (Cell cell : touching) {
            if (cell.col() < 0 || cell.row() < 0 || cell.col() >= layout.width() || cell.row() >= layout.height()) continue;
            int room = layout.roomAt(cell).orElseThrow().index();
            if (owner != null && owner != room) return null;
            owner = room;
        }
        if (owner != null) return realRooms.get(owner);
        for (RealRoom room : realRooms.values()) {
            if (room.covers(x, z)) return room;
        }
        return null;
    }

    /** The block the arena has at a world position; null means air. */
    public Block blockAt(int x, int y, int z) {
        RealRoom owner = ownerAt(x, z);
        if (owner != null) return blockIn(owner, x, y, z);
        return planBlock(x, y, z);
    }

    Block blockIn(RealRoom owner, int x, int y, int z) {
        Block gate = gates.blockAt(x, y, z);
        if (gate != null) return gate;
        if (openings.contains(pack(x, y, z))) return null;
        Block block = owner.blockAt(x, y, z);
        return block == null || block.air() ? null : block;
    }

    Block planBlock(int x, int y, int z) {
        Block gate = gates.blockAt(x, y, z);
        if (gate != null) return gate;
        CatacombsBlockPlan.Tile tile = plan.tileAt(x, y, z);
        RoomColor colour = tile == CatacombsBlockPlan.Tile.MARKER_FLOOR || tile == CatacombsBlockPlan.Tile.MARKER_PILLAR
                ? plan.markerColourAt(x, z) : RoomColor.BROWN;
        return CatacombsArenaGenerator.block(tile, colour);
    }

    private static Set<Long> openings(FloorLayout layout, Map<Integer, RealRoom> realRooms) {
        Set<Long> openings = new HashSet<>();
        for (LayoutDoor door : layout.doors()) {
            CellGeometry.BlockPosition seam = CellGeometry.doorPosition(door.from(), door.to());
            boolean eastWest = door.from().row() == door.to().row();
            Cell low = eastWest ? (door.from().col() < door.to().col() ? door.from() : door.to())
                    : (door.from().row() < door.to().row() ? door.from() : door.to());
            for (Cell cell : List.of(door.from(), door.to())) {
                int room = layout.roomAt(cell).orElseThrow().index();
                if (!realRooms.containsKey(room)) continue;
                int wall = cell.equals(low) ? -1 : 1;
                for (int along = -DoorFrame.WIDTH / 2; along <= DoorFrame.WIDTH / 2; along++) {
                    for (int dy = 1; dy <= DoorFrame.HEIGHT; dy++) {
                        int x = eastWest ? seam.x() + wall : seam.x() + along;
                        int z = eastWest ? seam.z() + along : seam.z() + wall;
                        openings.add(pack(x, CellGeometry.FLOOR_Y + dy, z));
                    }
                }
            }
        }
        return openings;
    }

    static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
