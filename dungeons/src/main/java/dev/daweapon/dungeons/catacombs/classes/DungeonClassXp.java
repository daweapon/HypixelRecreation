package dev.daweapon.dungeons.catacombs.classes;

import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Class XP from a run. The played class earns the full amount; every other class a teammate played earns a team bonus.
 * The wiki only says teammates' classes give "some" XP as a team bonus, so the 25% share is an estimate. Class levels
 * use the Catacombs XP table and each class level pays 4 SkyBlock XP (Healer Class Perks menu on the wiki).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DungeonClassXp {
    public static final double TEAM_BONUS_SHARE = 0.25;
    public static final int MAXIMUM_LEVEL = CatacombsLevels.MAXIMUM_LEVEL;
    public static final int SKYBLOCK_XP_PER_LEVEL = 4;

    public static Map<DungeonClassType, Long> earned(DungeonClassType played, Collection<DungeonClassType> teammates, long base) {
        Map<DungeonClassType, Long> earned = new EnumMap<>(DungeonClassType.class);
        if (base <= 0) return earned;
        earned.put(played, base);
        long bonus = Math.round(base * TEAM_BONUS_SHARE);
        for (DungeonClassType teammate : teammates) {
            if (teammate != played && bonus > 0) earned.putIfAbsent(teammate, bonus);
        }
        return earned;
    }

    public static int level(long xp) {
        return CatacombsLevels.levelForXp(xp);
    }

    public static long maxXp() {
        return CatacombsLevels.xpForLevel(MAXIMUM_LEVEL);
    }

    public static long capped(long xp) {
        return Math.max(0, Math.min(xp, maxXp()));
    }

    public static int skyBlockXp(int fromLevel, int toLevel) {
        return Math.max(0, toLevel - fromLevel) * SKYBLOCK_XP_PER_LEVEL;
    }
}
