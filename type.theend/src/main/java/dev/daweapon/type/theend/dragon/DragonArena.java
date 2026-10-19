package dev.daweapon.type.theend.dragon;

import net.minestom.server.coordinate.Pos;

import java.util.List;

public final class DragonArena {
    public static final Pos EGG = new Pos(-671.5, 62, -275.5);
    public static final Pos EGG_MIN = new Pos(-680, 46, -285);
    public static final Pos EGG_MAX = new Pos(-662, 79, -267);
    public static final Pos CENTER = new Pos(-672.5, 40, -275.5);
    public static final double ORBIT_RADIUS = 22;
    public static final double CEILING = 104;
    public static final int GATE_BOUNCE_RADIUS = 6;

    public static final Pos ALTAR_CENTER = new Pos(-671, 9, -276);
    public static final List<Pos> ALTAR_FRAMES = List.of(
            new Pos(-672, 9, -278),
            new Pos(-670, 9, -278),
            new Pos(-669, 9, -277),
            new Pos(-669, 9, -275),
            new Pos(-670, 9, -274),
            new Pos(-672, 9, -274),
            new Pos(-673, 9, -275),
            new Pos(-673, 9, -277));

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

    public static double flightHeight(DragonVariant variant) {
        return Math.min(CEILING, CENTER.y() + variant.flyHeight());
    }
}
