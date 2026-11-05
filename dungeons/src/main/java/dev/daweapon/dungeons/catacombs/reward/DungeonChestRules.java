package dev.daweapon.dungeons.catacombs.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
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
    public static final double S_PLUS_QUALITY_MULTIPLIER = 1.05;

    public static int claimsPerRun(boolean usingDungeonChestKey) {
        return usingDungeonChestKey ? CLAIMS_WITH_DUNGEON_CHEST_KEY : BASE_CLAIMS_PER_RUN;
    }

    public static List<DungeonChestTier> availableTiers(DungeonScoreRank rank, CatacombsFloor floor) {
        return availableTiers(rank, floor, CatacombsMode.NORMAL);
    }

    /**
     * Entrance has no boss chest. Floor VII completion grants Emerald and Obsidian whatever the score, and a
     * completed Master Mode Floor VII grants every chest.
     */
    public static List<DungeonChestTier> availableTiers(DungeonScoreRank rank, CatacombsFloor floor, CatacombsMode mode) {
        if (floor == CatacombsFloor.ENTRANCE) return List.of();
        boolean floorSeven = floor == CatacombsFloor.FLOOR_SEVEN;
        return Arrays.stream(DungeonChestTier.values())
                .filter(tier -> tier.unlockedBy(rank, floor)
                        || (floorSeven && (tier == DungeonChestTier.EMERALD || tier == DungeonChestTier.OBSIDIAN))
                        || (floorSeven && mode == CatacombsMode.MASTER))
                .toList();
    }

    /**
     * Wiki Quality Formula, including Hypixel's bug where the treasure multiplier and flat bonuses are applied twice.
     *
     * @param treasureMultiplier 1.0, or 1.01/1.02/1.03 for the highest Treasure accessory owned
     * @param flatBonus          Boss Luck plus the Catacombs Box attribute
     */
    public static int quality(int baseQuality, boolean sPlus, double treasureMultiplier, double flatBonus) {
        int initial = (int) Math.floor(baseQuality * (sPlus ? S_PLUS_QUALITY_MULTIPLIER : 1.0));
        long intended = roundHalfUp(initial * treasureMultiplier + flatBonus);
        return (int) roundHalfUp(intended * treasureMultiplier + flatBonus);
    }

    private static long roundHalfUp(double value) {
        return (long) Math.floor(value + 0.5);
    }
}
