package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DungeonScoreTables {
    private static final int[] NORMAL_TIME_LIMIT_MINUTES = {10, 10, 10, 10, 12, 10, 12, 14};
    private static final int[] MASTER_TIME_LIMIT_MINUTES = {0, 8, 8, 8, 8, 8, 10, 14};
    private static final int[] NORMAL_TIME_OFFSET_SECONDS = {120, 120, 120, 120, 240, 120, 240, 360};
    private static final int[] MASTER_TIME_OFFSET_SECONDS = {0, 0, 0, 0, 0, 0, 120, 360};
    private static final int[] NORMAL_SECRET_REQUIREMENT_PERCENT = {30, 30, 40, 50, 60, 70, 85, 100};

    public static int timeLimitSeconds(CatacombsFloor floor, CatacombsMode mode) {
        requireSupported(floor, mode);
        int[] table = mode == CatacombsMode.MASTER ? MASTER_TIME_LIMIT_MINUTES : NORMAL_TIME_LIMIT_MINUTES;
        return table[floor.ordinal()] * 60;
    }

    public static int timeOffsetSeconds(CatacombsFloor floor, CatacombsMode mode) {
        requireSupported(floor, mode);
        int[] table = mode == CatacombsMode.MASTER ? MASTER_TIME_OFFSET_SECONDS : NORMAL_TIME_OFFSET_SECONDS;
        return table[floor.ordinal()];
    }

    public static int secretRequirementPercent(CatacombsFloor floor, CatacombsMode mode) {
        requireSupported(floor, mode);
        return mode == CatacombsMode.MASTER ? 100 : NORMAL_SECRET_REQUIREMENT_PERCENT[floor.ordinal()];
    }

    private static void requireSupported(CatacombsFloor floor, CatacombsMode mode) {
        if (!floor.supports(mode)) {
            throw new IllegalArgumentException(floor.displayName() + " does not support " + mode);
        }
    }
}
