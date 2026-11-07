package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.levels.DiminishingXp;

public final class WhisperExperience {
    public static final long WHISPERS_PER_XP = 2_400;
    public static final long LINEAR_CAP = 600_000;
    public static final long FULL_AT = 12_500_000;
    public static final double MAX_XP = 620;
    private static final DiminishingXp RULE = new DiminishingXp(WHISPERS_PER_XP, LINEAR_CAP, FULL_AT, MAX_XP);

    private WhisperExperience() {
    }

    public static double experience(long whispers) {
        return RULE.experience(whispers);
    }
}
