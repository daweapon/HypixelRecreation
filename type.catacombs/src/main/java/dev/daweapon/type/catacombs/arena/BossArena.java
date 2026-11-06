package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicReader;
import net.minestom.server.instance.block.Block;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A floor's real boss room, copied block for block from a captured Hypixel run and pasted at the same world position
 * Hypixel uses, just past the dungeon grid. Floor I's arena is Bonzo's circus tent (x -72..-14, y 55..145, z -40..49),
 * cut from both captured runs (identical) without the marker blocks Hypixel keeps at the bottom and top of the world.
 */
public final class BossArena {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/dungeons/bosses");
    private static final Map<CatacombsFloor, Placement> PLACEMENTS = Map.of(
            CatacombsFloor.FLOOR_ONE, new Placement("floor_1.schem", -72, 55, -40));
    private static final Map<String, Schematic> LOADED = new ConcurrentHashMap<>();

    private record Placement(String file, int x, int y, int z) {
    }

    private final Schematic schematic;
    private final int minX;
    private final int minY;
    private final int minZ;

    BossArena(Schematic schematic, int minX, int minY, int minZ) {
        this.schematic = schematic;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
    }

    public static boolean hasRealArena(CatacombsFloor floor) {
        return PLACEMENTS.containsKey(floor);
    }

    /** The floor's captured arena, when it has one and it clears the dungeon grid of this layout. */
    public static Optional<BossArena> of(CatacombsFloor floor, CatacombsBlockPlan plan) {
        Placement placement = PLACEMENTS.get(floor);
        if (placement == null) return Optional.empty();
        Optional<Schematic> schematic = load(placement.file());
        if (schematic.isEmpty()) return Optional.empty();
        BossArena arena = new BossArena(schematic.get(), placement.x(), placement.y(), placement.z());
        if (arena.overlaps(plan)) {
            Logger.warn("The {} boss arena overlaps the dungeon grid; using the placeholder arena", floor);
            return Optional.empty();
        }
        return Optional.of(arena);
    }

    public static void preload() {
        PLACEMENTS.values().forEach(placement -> load(placement.file()));
    }

    private static Optional<Schematic> load(String file) {
        try {
            return Optional.of(LOADED.computeIfAbsent(file, name -> {
                try {
                    return SchematicReader.read(DIRECTORY.resolve(name));
                } catch (IOException exception) {
                    throw new IllegalStateException("Could not read boss arena " + name, exception);
                }
            }));
        } catch (RuntimeException exception) {
            Logger.error(exception, "The boss arena {} could not be loaded", file);
            return Optional.empty();
        }
    }

    boolean overlaps(CatacombsBlockPlan plan) {
        boolean apartX = maxX() < plan.minX() || minX > plan.maxX();
        boolean apartZ = maxZ() < plan.minZ() || minZ > plan.maxZ();
        return !apartX && !apartZ;
    }

    public boolean covers(int x, int z) {
        return x >= minX && x <= maxX() && z >= minZ && z <= maxZ();
    }

    /** The arena's block at a world position; null outside it or for air. */
    public Block blockAt(int x, int y, int z) {
        if (!covers(x, z) || y < minY || y > maxY()) return null;
        Block block = schematic.blockAt(x - minX, y - minY, z - minZ);
        return block == null || block.air() ? null : block;
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
        return minX + schematic.width() - 1;
    }

    public int maxY() {
        return minY + schematic.height() - 1;
    }

    public int maxZ() {
        return minZ + schematic.length() - 1;
    }
}
