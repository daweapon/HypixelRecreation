package dev.daweapon.type.skyblockgeneric.region.mining;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.random.RandomGenerator;

public final class PondIce {
    private PondIce() {
    }

    public static boolean isBreakable(Block block, Block below) {
        return block.compare(Block.ICE) && below.compare(Block.WATER);
    }

    public static long regrowDelayMillis(long minMillis, long maxMillis, RandomGenerator random) {
        if (maxMillis <= minMillis) return minMillis;
        return minMillis + random.nextLong(maxMillis - minMillis + 1);
    }

    public static boolean occupies(Point min, Point max, Point block, double headroom) {
        return overlaps(min.x(), max.x(), block.blockX(), block.blockX() + 1)
                && overlaps(min.y(), max.y(), block.blockY(), block.blockY() + 1 + headroom)
                && overlaps(min.z(), max.z(), block.blockZ(), block.blockZ() + 1);
    }

    private static boolean overlaps(double min, double max, double low, double high) {
        if (min >= high) return false;
        return min == max ? max >= low : max > low;
    }
}
