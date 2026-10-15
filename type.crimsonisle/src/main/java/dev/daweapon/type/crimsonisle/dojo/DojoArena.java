package dev.daweapon.type.crimsonisle.dojo;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.HypixelConst;

import java.util.HashMap;
import java.util.Map;

/**
 * The Dojo Arena: the barrier-walled hall under the Dojo, a single layer of lava at y 97 with open air
 * from y 98 to 106 over x -225..-189, z -616..-580. Tests build their platforms into the lava layer and
 * {@link #restoreAll()} puts the lava back afterwards.
 */
public final class DojoArena {
    public static final int MIN_X = -225;
    public static final int MAX_X = -189;
    public static final int MIN_Z = -616;
    public static final int MAX_Z = -580;
    public static final int FLOOR_Y = 97;
    public static final int STAND_Y = 98;
    public static final int CEILING_Y = 106;
    public static final double CENTER_X = -206.5;
    public static final double CENTER_Z = -597.5;
    public static final Pos CENTER = new Pos(CENTER_X, STAND_Y, CENTER_Z, 0, 0);
    public static final Pos EXIT = new Pos(-235.5, 108, -599.5, -135, 0);

    private final Map<Cell, Block> originals = new HashMap<>();

    public static Instance instance() {
        return HypixelConst.getInstanceContainer();
    }

    public static boolean isInLava(Point point) {
        return point.y() < STAND_Y - 0.5;
    }

    public static boolean inside(double x, double z) {
        return x >= MIN_X && x < MAX_X + 1 && z >= MIN_Z && z < MAX_Z + 1;
    }

    public static Pos centered(int x, int y, int z) {
        return new Pos(x + 0.5, y, z + 0.5);
    }

    public void set(int x, int y, int z, Block block) {
        if (!inside(x, z) || y < FLOOR_Y || y > CEILING_Y) return;
        Instance instance = instance();
        originals.putIfAbsent(new Cell(x, y, z), instance.getBlock(x, y, z));
        instance.setBlock(x, y, z, block);
    }

    public void floor(int x, int z, Block block) {
        set(x, FLOOR_Y, z, block);
    }

    public void restore(int x, int y, int z) {
        Block original = originals.remove(new Cell(x, y, z));
        if (original != null) instance().setBlock(x, y, z, original);
    }

    public Block get(int x, int y, int z) {
        return instance().getBlock(x, y, z);
    }

    public boolean isPlaced(int x, int y, int z) {
        return originals.containsKey(new Cell(x, y, z));
    }

    public void disc(double radius, Block block) {
        int r = (int) Math.ceil(radius);
        int cx = (int) Math.floor(CENTER_X);
        int cz = (int) Math.floor(CENTER_Z);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= radius * radius) floor(cx + dx, cz + dz, block);
            }
        }
    }

    public void square(int half, Block block) {
        int cx = (int) Math.floor(CENTER_X);
        int cz = (int) Math.floor(CENTER_Z);
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) floor(cx + dx, cz + dz, block);
        }
    }

    /** Clears anything left standing in the open arena, such as the stray wool baked into the map. */
    public static void clearDebris() {
        Instance instance = instance();
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int y = STAND_Y; y < CEILING_Y; y++) {
                    if (!instance.getBlock(x, y, z).isAir()) instance.setBlock(x, y, z, Block.AIR);
                }
            }
        }
    }

    public void restoreAll() {
        Instance instance = instance();
        originals.forEach((cell, block) -> instance.setBlock(cell.x(), cell.y(), cell.z(), block));
        originals.clear();
    }

    private record Cell(int x, int y, int z) {
    }
}
