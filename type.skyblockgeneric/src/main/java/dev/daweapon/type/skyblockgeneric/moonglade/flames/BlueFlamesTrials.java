package dev.daweapon.type.skyblockgeneric.moonglade.flames;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class BlueFlamesTrials {
    public static final int TRIAL_COUNT = 30;
    public static final int SURVIVE_SECONDS = 10;
    public static final int BADGE_COUNT = 29;
    public static final String BADGE_PREFIX = "SOUL_CAMPFIRE_TALISMAN_";
    public static final String TIER_TEN_TRIGGER = "brian.tier10";

    private static final long[] DPS = {1, 6, 16, 36, 76, 146, 246, 346, 546, 746, 946, 1446, 1946, 2446, 2946, 3946,
            4946, 5946, 6946, 7946, 9196, 10446, 11946, 13446, 15446, 17446, 19946, 22946, 26446, 30446};
    private static final String[] RARITIES = {"Initiate", "Adept", "Cultist", "Scion", "God"};
    private static final int[] RARITY_STARTS = {1, 4, 8, 13, 21};
    private static final Map<Integer, Upgrade> UPGRADES = Map.of(
            4, new Upgrade("ENCHANTED_FIG_LOG", 64),
            8, new Upgrade("ENCHANTED_FIG_LOG", 256),
            13, new Upgrade("ENCHANTED_MANGROVE_LOG", 256),
            21, new Upgrade("ENCHANTED_MANGROVE_LOG", 512));

    public record Upgrade(String itemId, int amount) {
    }

    private BlueFlamesTrials() {
    }

    public static long damagePerSecond(int trial) {
        return DPS[trial - 1];
    }

    public static long totalDamage(int trial) {
        return damagePerSecond(trial) * SURVIVE_SECONDS;
    }

    public static int badgeLevelAfter(int completedTrials) {
        return Math.max(0, Math.min(BADGE_COUNT, completedTrials - 1));
    }

    public static String badgeId(int level) {
        return BADGE_PREFIX + level;
    }

    public static int levelOfBadge(@Nullable String itemId) {
        if (itemId == null || !itemId.startsWith(BADGE_PREFIX)) return 0;
        try {
            return Integer.parseInt(itemId.substring(BADGE_PREFIX.length()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public static @Nullable Upgrade upgradeInto(int level) {
        return UPGRADES.get(level);
    }

    public static String rarityOf(int level) {
        int index = 0;
        for (int i = 0; i < RARITY_STARTS.length; i++) if (level >= RARITY_STARTS[i]) index = i;
        return RARITIES[index];
    }

    public static int trialsUntilRarityUp(int completedTrials) {
        int level = badgeLevelAfter(completedTrials);
        for (int start : RARITY_STARTS) if (start > level) return start - level;
        return 0;
    }

    public static double damageAfterDefense(double damagePerSecond, double defense) {
        return damagePerSecond * 100D / (100D + Math.max(0, defense));
    }

    public static boolean instantFail(double damagePerSecond, double maxHealth) {
        return damagePerSecond > maxHealth;
    }
}
