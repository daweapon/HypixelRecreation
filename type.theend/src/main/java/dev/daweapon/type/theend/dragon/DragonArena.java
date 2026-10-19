package dev.daweapon.type.theend.dragon;

import net.minestom.server.coordinate.Pos;

import java.util.List;

public final class DragonArena {
    public static final Pos EGG = new Pos(-672.5, 77, -275.5);
    public static final Pos CENTER = new Pos(-672.5, 40, -275.5);
    public static final double ORBIT_RADIUS = 22;
    public static final double CEILING = 104;
    public static final int GATE_BOUNCE_RADIUS = 6;

    public static final List<Pos> ALTAR_FRAMES = List.of(
            new Pos(-600, 28, -328),
            new Pos(-599, 28, -326),
            new Pos(-598, 28, -322),
            new Pos(-599, 29, -327),
            new Pos(-598, 29, -324),
            new Pos(-602, 30, -327),
            new Pos(-599, 30, -323),
            new Pos(-600, 31, -325));

    public static final List<Pos> CRYSTAL_SITES = List.of(
            new Pos(-645.5, 5, -275.5),
            new Pos(-653.5, 5, -262.5),
            new Pos(-678.5, 5, -254.5),
            new Pos(-689.5, 5, -262.5),
            new Pos(-697.5, 5, -275.5),
            new Pos(-679.5, 5, -300.5),
            new Pos(-641.5, 5, -275.5),
            new Pos(-695.5, 5, -257.5));

    public static final Pos ALTAR_CENTER = new Pos(-599, 29, -325);
    public static final Pos ALTAR_STANDING = new Pos(-595.5, 27, -325.5);

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
