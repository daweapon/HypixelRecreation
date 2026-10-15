package dev.daweapon.dungeons.catacombs.boss;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WatcherSpawns {
    private static final int BASE_MOBS = 9;

    public static int mobCount(CatacombsFloor floor) {
        return BASE_MOBS + floor.ordinal();
    }

    public static boolean completesRunWhenCleared(CatacombsFloor floor) {
        return floor == CatacombsFloor.ENTRANCE;
    }

    public static boolean cleared(CatacombsFloor floor, int mobsKilled) {
        return mobsKilled >= mobCount(floor);
    }
}
