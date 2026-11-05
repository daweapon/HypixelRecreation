package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomStructure;
import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicRotation;
import net.minestom.server.instance.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A real room's blocks turned with the layout's rotation and moved onto the room's cells. */
public final class RealRoom {
    private final PlacedRoom room;
    private final Schematic schematic;
    private final int turns;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final Map<Integer, Block> turnedStates = new ConcurrentHashMap<>();

    private RealRoom(PlacedRoom room, Schematic schematic, int minX, int minY, int minZ) {
        this.room = room;
        this.schematic = schematic;
        this.turns = room.rotation().quarterTurns();
        boolean swap = turns % 2 == 1;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = minX + (swap ? schematic.length() : schematic.width()) - 1;
        this.maxY = minY + schematic.height() - 1;
        this.maxZ = minZ + (swap ? schematic.width() : schematic.length()) - 1;
    }

    public static RealRoom place(PlacedRoom room, Schematic schematic) {
        RoomStructure structure = room.template().blocks().orElseThrow();
        int[] first = turnLocal(room, structure.x(), structure.z());
        int[] last = turnLocal(room, structure.x() + schematic.width() - 1, structure.z() + schematic.length() - 1);
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        return new RealRoom(room, schematic, origin.x() + Math.min(first[0], last[0]), CellGeometry.FLOOR_Y + structure.y(),
                origin.z() + Math.min(first[1], last[1]));
    }

    private static int[] turnLocal(PlacedRoom room, int x, int z) {
        CellGeometry.PlanarPoint turned = CellGeometry.rotateLocal(x + 0.5, z + 0.5, room.shape(), room.rotation());
        return new int[]{(int) Math.floor(turned.x()), (int) Math.floor(turned.z())};
    }

    public PlacedRoom room() {
        return room;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    public boolean covers(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    /** The block at a world position, or null outside the room's blocks or where the file had an unknown block. */
    public Block blockAt(int x, int y, int z) {
        if (!covers(x, z) || y < minY || y > maxY) return null;
        int turnedX = x - minX;
        int turnedZ = z - minZ;
        int width = schematic.width();
        int length = schematic.length();
        int baseX = switch (turns) {
            case 1 -> turnedZ;
            case 2 -> width - 1 - turnedX;
            case 3 -> width - 1 - turnedZ;
            default -> turnedX;
        };
        int baseZ = switch (turns) {
            case 1 -> length - 1 - turnedX;
            case 2 -> length - 1 - turnedZ;
            case 3 -> turnedX;
            default -> turnedZ;
        };
        Block base = schematic.blockAt(baseX, y - minY, baseZ);
        if (base == null || turns == 0) return base;
        Block turned = turnedStates.computeIfAbsent(base.stateId(), _ -> SchematicRotation.rotate(base.withNbt(null), turns));
        return base.hasNbt() ? turned.withNbt(base.nbt()) : turned;
    }
}
