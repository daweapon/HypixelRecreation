package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

public record DungeonRunRules(
        int speedTimeLimitSeconds,
        int speedTimeOffsetSeconds,
        int secretRequirementPercent,
        int maximumDurationSeconds,
        int minimumPartySize,
        int maximumPartySize,
        boolean automaticGhostRevive,
        boolean trapRoomsEnabled
) {
    public static final int MAXIMUM_DURATION_SECONDS = 3600;
    public static final int MINIMUM_PARTY_SIZE = 2;
    public static final int MAXIMUM_PARTY_SIZE = 5;
    public static final int AUTOMATIC_REVIVE_SECONDS = 50;

    public static DungeonRunRules of(CatacombsFloor floor, CatacombsMode mode) {
        return new DungeonRunRules(
                DungeonScoreTables.timeLimitSeconds(floor, mode),
                DungeonScoreTables.timeOffsetSeconds(floor, mode),
                DungeonScoreTables.secretRequirementPercent(floor, mode),
                MAXIMUM_DURATION_SECONDS,
                MINIMUM_PARTY_SIZE,
                MAXIMUM_PARTY_SIZE,
                mode == CatacombsMode.NORMAL,
                floor.ordinal() >= CatacombsFloor.FLOOR_THREE.ordinal());
    }

    public boolean allowsPartySize(int size) {
        return allowsPartySize(size, false);
    }

    public boolean allowsPartySize(int size, boolean soloAllowed) {
        int minimum = soloAllowed ? 1 : minimumPartySize;
        return size >= minimum && size <= maximumPartySize;
    }
}
