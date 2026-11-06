package dev.daweapon.type.catacombs.engine.bonzo;

import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;

/**
 * Where things are in the captured Floor I arena (world coordinates, identical in both runs): the party lands at
 * (-42.5, 71.5, 34.5) facing north, Bonzo spawns at (-42.5, 71, 20.5) facing them, the two exit gates are 3 wide, 5
 * tall and 3 deep in the corridor north of the tent, and the exit portal back to the Dungeon Hub is the 5 by 8 nether
 * portal at the end of that corridor.
 */
public final class BonzoArenaSpots {
    public static final Pos ARRIVAL = new Pos(-42.5, 71.5, 34.5, 180, 0);
    public static final Pos BONZO_SPAWN = new Pos(-42.5, 71, 20.5, 0, 0);
    public static final Pos GATE_SOUND = new Pos(-43.5, 70.5, 4.5);
    public static final List<int[]> FIRST_GATE = box(-44, -42, 71, 75, 4, 6);
    public static final List<int[]> SECOND_GATE = box(-44, -42, 71, 75, -6, -4);
    public static final List<int[]> EXIT_PORTAL = box(-45, -41, 59, 67, -39, -39);

    private BonzoArenaSpots() {
    }

    static List<int[]> box(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        List<int[]> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) blocks.add(new int[]{x, y, z});
            }
        }
        return List.copyOf(blocks);
    }

    public static boolean inExitPortal(int x, int y, int z) {
        return x >= -45 && x <= -41 && z == -39 && y >= 59 && y <= 67;
    }
}
