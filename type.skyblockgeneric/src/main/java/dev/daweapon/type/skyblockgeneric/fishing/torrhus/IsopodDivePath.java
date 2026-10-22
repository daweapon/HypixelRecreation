package dev.daweapon.type.skyblockgeneric.fishing.torrhus;

import net.minestom.server.coordinate.Pos;

import java.util.List;

public final class IsopodDivePath {
    public static final double BLOCKS_PER_TICK = 0.6;
    public static final List<Pos> WAYPOINTS = List.of(
            new Pos(-640.5, 100, 192.5),
            new Pos(-628.5, 58, 212.5),
            new Pos(-611.5, 44.5, 244.5));

    private IsopodDivePath() {
    }

    public static boolean shouldDive(double health, double maxHealth, boolean dived) {
        return !dived && maxHealth > 0 && health <= maxHealth / 2;
    }

    public static Pos step(Pos from, Pos to, double distance) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= distance) return to;
        double scale = distance / length;
        return new Pos(from.x() + dx * scale, from.y() + dy * scale, from.z() + dz * scale,
                (float) Math.toDegrees(Math.atan2(-dx, dz)), 0);
    }

    public static int nextWaypoint(Pos position, int index) {
        return position.distanceSquared(WAYPOINTS.get(index)) < 0.01 ? index + 1 : index;
    }

    public static boolean finished(int index) {
        return index >= WAYPOINTS.size();
    }
}
