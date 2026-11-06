package dev.daweapon.dungeons.catacombs.boss;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WatcherSpawns {
    private static final int BASE_MOBS = 9;
    private static final int CAPTURED_FLOOR_ONE_MOBS = 9;

    /** The wiki's 9 plus the floor number, except Floor I where both captured runs had exactly 9 summons. */
    public static int mobCount(CatacombsFloor floor) {
        if (floor == CatacombsFloor.FLOOR_ONE) return CAPTURED_FLOOR_ONE_MOBS;
        return BASE_MOBS + floor.ordinal();
    }

    public static boolean completesRunWhenCleared(CatacombsFloor floor) {
        return floor == CatacombsFloor.ENTRANCE;
    }

    public static boolean cleared(CatacombsFloor floor, int mobsKilled) {
        return mobsKilled >= mobCount(floor);
    }
}
