package dev.daweapon.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public final class GardenGround {
    private static final int SCAN_TOP = 74;
    private static final int SCAN_BOTTOM = 60;

    private GardenGround() {
    }

    public static Pos onGround(Instance instance, Pos wanted) {
        if (instance == null || instance.getChunkAt(wanted.x(), wanted.z()) == null) return wanted;
        for (int y = SCAN_TOP; y > SCAN_BOTTOM; y--) {
            if (instance.getBlock(wanted.blockX(), y, wanted.blockZ()).isSolid()) {
                return wanted.withY(y + 1);
            }
        }
        return wanted;
    }
}
