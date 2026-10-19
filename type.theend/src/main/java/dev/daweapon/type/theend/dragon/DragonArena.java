package dev.daweapon.type.theend.dragon;

import net.minestom.server.coordinate.Pos;

import java.util.List;

public final class DragonArena {
    public static final Pos EGG = new Pos(-672.5, 77, -275.5);
    public static final Pos CENTER = new Pos(-672.5, 40, -275.5);
    public static final double ORBIT_RADIUS = 22;
    public static final double CEILING = 104;
    public static final int GATE_BOUNCE_RADIUS = 6;

    public static final Pos ALTAR_CENTER = new Pos(-606, 22, -276);
    public static final List<Pos> ALTAR_FRAMES = List.of(
            new Pos(-602, 22, -276),
            new Pos(-603, 22, -273),
            new Pos(-606, 22, -272),
            new Pos(-609, 22, -273),
            new Pos(-610, 22, -276),
            new Pos(-609, 22, -279),
            new Pos(-606, 22, -280),
            new Pos(-603, 22, -279));

    public static final List<Pos> CRYSTAL_SITES = List.of(
            new Pos(-645.5, 5, -275.5),
            new Pos(-653.5, 5, -262.5),
            new Pos(-678.5, 5, -254.5),
            new Pos(-689.5, 5, -262.5),
            new Pos(-697.5, 5, -275.5),
            new Pos(-679.5, 5, -300.5),
            new Pos(-641.5, 5, -275.5),
            new Pos(-695.5, 5, -257.5));


    private DragonArena() {
    }

    public static int frameIndex(int x, int y, int z) {
        for (int i = 0; i < ALTAR_FRAMES.size(); i++) {
            Pos frame = ALTAR_FRAMES.get(i);
            if (frame.blockX() == x && frame.blockY() == y && frame.blockZ() == z) return i;
        }
        return -1;
    }

    public static String frameFacing(Pos frame) {
        double dx = ALTAR_CENTER.x() - frame.x();
        double dz = ALTAR_CENTER.z() - frame.z();
        if (Math.abs(dx) >= Math.abs(dz)) return dx > 0 ? "east" : "west";
        return dz > 0 ? "south" : "north";
    }

    public static double flightHeight(DragonVariant variant) {
        return Math.min(CEILING, CENTER.y() + variant.flyHeight());
    }
}
