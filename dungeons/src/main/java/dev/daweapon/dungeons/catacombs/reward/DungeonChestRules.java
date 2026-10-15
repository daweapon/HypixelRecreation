package dev.daweapon.dungeons.catacombs.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DungeonChestRules {
    public static final int CHESTS_OFFERED = 3;
    public static final int MAXIMUM_CHESTS = 6;
    public static final int BASE_CLAIMS_PER_RUN = 1;
    public static final int CLAIMS_WITH_DUNGEON_CHEST_KEY = 2;
    public static final int KISMET_FEATHER_REROLLS = 1;
    public static final int CROESUS_RETAINED_RUNS = 60;
    public static final int CROESUS_RETENTION_HOURS = 72;

    public static int claimsPerRun(boolean usingDungeonChestKey) {
        return usingDungeonChestKey ? CLAIMS_WITH_DUNGEON_CHEST_KEY : BASE_CLAIMS_PER_RUN;
    }

    public static List<DungeonChestTier> availableTiers(DungeonScoreRank rank, CatacombsFloor floor) {
        return Arrays.stream(DungeonChestTier.values())
                .filter(tier -> tier.unlockedBy(rank, floor))
                .toList();
    }
}
