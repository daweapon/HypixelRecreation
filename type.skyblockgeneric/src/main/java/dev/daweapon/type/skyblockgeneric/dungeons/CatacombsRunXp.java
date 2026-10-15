package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;

import java.util.EnumMap;
import java.util.Map;

/**
 * PLACEHOLDER: the wiki has no per-floor Catacombs XP formula for a completed run. Floor I's 80 is on its wiki
 * page; Entrance's 40 is an estimate. Floors without an entry pay nothing until they are playable. The score does
 * not scale the XP because no source for that exists. The daily bonus (first
 * {@link CatacombsLevels#DAILY_BONUS_RUNS} runs each day, +{@link CatacombsLevels#DAILY_BONUS_PERCENT}%) is on the wiki.
 */
public final class CatacombsRunXp {
    private static final Map<CatacombsFloor, Long> BASE = new EnumMap<>(CatacombsFloor.class);

    static {
        BASE.put(CatacombsFloor.ENTRANCE, 40L);
        BASE.put(CatacombsFloor.FLOOR_ONE, 80L);
    }

    private CatacombsRunXp() {
    }

    public static long base(CatacombsFloor floor) {
        return BASE.getOrDefault(floor, 0L);
    }

    public static long withBonus(long base, int bonusPercent) {
        return base + Math.round(base * bonusPercent / 100.0);
    }
}
