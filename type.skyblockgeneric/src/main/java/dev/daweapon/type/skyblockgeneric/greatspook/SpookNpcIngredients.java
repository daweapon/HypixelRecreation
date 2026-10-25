package dev.daweapon.type.skyblockgeneric.greatspook;

public final class SpookNpcIngredients {
    public static final long SIRIUS_COINS = 7_871_265L;
    public static final long GRATITUDE_WINDOW_MILLIS = 2_000;
    public static final long FREE_SPIDER_COOLDOWN_MILLIS = 5_000;
    public static final int CAGE_MIN_X = -277;
    public static final int CAGE_MAX_X = -274;
    public static final int CAGE_MIN_Y = 115;
    public static final int CAGE_MAX_Y = 117;
    public static final int CAGE_MIN_Z = -186;
    public static final int CAGE_MAX_Z = -183;
    public static final double COW_HAT_RANGE = 8;

    private SpookNpcIngredients() {
    }

    public static boolean isSoup(String id) {
        return id != null && id.endsWith("_SOUP");
    }

    public static boolean exactSiriusPurse(double coins) {
        return coins == SIRIUS_COINS;
    }

    public static boolean acceptedQuickly(long now, long arrivedAt) {
        return now - arrivedAt <= GRATITUDE_WINDOW_MILLIS;
    }

    public static boolean inCage(int x, int y, int z) {
        return x >= CAGE_MIN_X && x <= CAGE_MAX_X && y >= CAGE_MIN_Y && y <= CAGE_MAX_Y
                && z >= CAGE_MIN_Z && z <= CAGE_MAX_Z;
    }

    public static long cooldownLeft(long now, long lastFreed) {
        return Math.max(0, lastFreed + FREE_SPIDER_COOLDOWN_MILLIS - now);
    }
}
