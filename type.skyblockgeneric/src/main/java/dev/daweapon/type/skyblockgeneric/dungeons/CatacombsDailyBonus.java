package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;

/** How many Catacombs runs a player finished on a day, which decides the daily XP bonus. */
public record CatacombsDailyBonus(long day, int runs) {
    public static final CatacombsDailyBonus NONE = new CatacombsDailyBonus(-1, 0);

    public static CatacombsDailyBonus parse(String stored) {
        if (stored == null || !stored.contains(":")) return NONE;
        try {
            String[] parts = stored.split(":", 2);
            return new CatacombsDailyBonus(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException exception) {
            return NONE;
        }
    }

    public String encode() {
        return day + ":" + runs;
    }

    public int runsOn(long today) {
        return day == today ? runs : 0;
    }

    public int bonusPercent(long today) {
        return runsOn(today) < CatacombsLevels.DAILY_BONUS_RUNS ? CatacombsLevels.DAILY_BONUS_PERCENT : 0;
    }

    public CatacombsDailyBonus afterRun(long today) {
        return new CatacombsDailyBonus(today, runsOn(today) + 1);
    }
}
