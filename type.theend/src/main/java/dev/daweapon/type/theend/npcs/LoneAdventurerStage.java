package dev.daweapon.type.theend.npcs;

public enum LoneAdventurerStage {
    NOT_STARTED,
    KILLING_ENDERMEN,
    ENDER_ARMOR,
    SHORTBOW,
    PEARL_DEALER_REWARD,
    GUIDE_WAIT,
    GUIDE_READY;

    public static final int ENDERMEN_REQUIRED = 5;
    public static final int GUIDE_COMBAT_LEVEL = 18;
    public static final long SKYBLOCK_DAY_MILLIS = 20 * 60 * 1000L;

    public static LoneAdventurerStage fromOrdinal(long ordinal) {
        LoneAdventurerStage[] stages = values();
        return stages[(int) Math.max(0, Math.min(ordinal, stages.length - 1))];
    }

    public boolean atFirstStand() {
        return compareTo(ENDER_ARMOR) < 0;
    }

    public boolean guideUnlocked(int combatLevel, long millisSinceShortbow) {
        return this == GUIDE_WAIT && combatLevel >= GUIDE_COMBAT_LEVEL && millisSinceShortbow >= SKYBLOCK_DAY_MILLIS;
    }
}
