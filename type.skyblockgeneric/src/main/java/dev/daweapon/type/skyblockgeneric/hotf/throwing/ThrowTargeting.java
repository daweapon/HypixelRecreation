package dev.daweapon.type.skyblockgeneric.hotf.throwing;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;

import java.util.function.Predicate;

public final class ThrowTargeting {
    private ThrowTargeting() {
    }

    public static boolean bounces(double chancePercent, double roll) {
        return chancePercent > 0 && roll * 100 < chancePercent;
    }

    public static Point nearestLog(Point center, int radius, int minimumDistance, Predicate<Point> isLog, Predicate<Point> excluded) {
        BlockVec origin = new BlockVec(center);
        BlockVec best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance > radius || distance < minimumDistance || distance >= bestDistance) continue;
                    BlockVec candidate = origin.add(x, y, z);
                    if (excluded.test(candidate) || !isLog.test(candidate)) continue;
                    best = candidate;
                    bestDistance = distance;
                }
            }
        }
        return best;
    }
}
