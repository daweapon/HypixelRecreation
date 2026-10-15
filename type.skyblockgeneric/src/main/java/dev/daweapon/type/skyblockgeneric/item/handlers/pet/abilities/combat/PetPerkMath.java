package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.random.RandomGenerator;

public final class PetPerkMath {
    public static final double LONG_NECK_DISTANCE = 3;
    public static final double APEX_PREDATOR_RADIUS = 15;
    public static final int TRAMPLE_MIN_FALL = 20;
    public static final double TRAMPLE_RADIUS = 3;
    public static final double SKELETAL_DEFENSE_RANGE = 4;
    public static final double SKELETAL_DEFENSE_CRIT_MULTIPLIER = 30;
    public static final long SKELETAL_DEFENSE_COOLDOWN_MS = 5_000;
    public static final long HEMORRHAGE_MS = 10_000;
    public static final long STUN_MS = 10_000;
    public static final long SPLASH_SURPRISE_STUN_MS = 5_000;
    public static final long SLOW_START_COOLDOWN_MS = 60_000;
    public static final double HOTSPOT_CHANCE_CAP = 85;
    public static final long STUN_COOLDOWN_MS = 10_000;
    public static final double AURA_RANGE = 50;
    public static final int AURA_TARGETS = 4;
    public static final double BASE_JUMP_STRENGTH = 0.42;
    public static final double JUMP_STRENGTH_PER_LEVEL = 0.1;
    public static final double BIRD_DISCOURSE_RANGE = 20;
    public static final long OVERHEAL_QUIET_MS = 10_000;
    public static final long EXTINGUISH_INTERVAL_MS = 8_000;
    public static final long EXTINGUISH_COMBAT_MS = 10_000;
    public static final double EXTINGUISH_RADIUS = 2.5;
    public static final int WITHER_SECONDS = 3;
    public static final long TRAINING_REFUND_STEP_COINS = 1_000_000;
    public static final double TRAINING_REFUND_MAX_PERCENT = 5;
    private static final int[] BULWARK_KILLS = {100, 200, 300, 500, 800, 1200, 1750, 2500, 3500, 5000, 10000, 25000,
            50000, 100000, 125000, 150000, 200000};
    private static final int[] BULWARK_DEFENSE = {30, 60, 90, 135, 180, 225, 270, 315, 360, 405, 465, 500, 535, 570,
            585, 595, 600};
    private static final int[] BULWARK_TRUE_DEFENSE = {3, 6, 9, 14, 18, 23, 27, 32, 36, 41, 47, 50, 53, 57, 58, 59, 60};

    private PetPerkMath() {
    }

    public static double missingHealthPercent(double maxPercent, double health, double maxHealth) {
        if (maxHealth <= 0) return 0;
        double missing = Math.clamp(1 - health / maxHealth, 0, 1);
        return maxPercent * missing;
    }

    public static double longNeckDamage(double flatDamage, double distance) {
        return distance > LONG_NECK_DISTANCE ? flatDamage : 0;
    }

    public static double boostChance(double chancePercent, double boostPercent) {
        return chancePercent * (1 + boostPercent / 100D);
    }

    public static int scaledAmount(int amount, double extraPercent, RandomGenerator random) {
        if (amount <= 0 || extraPercent <= 0) return amount;
        double scaled = amount * (1 + extraPercent / 100D);
        int whole = (int) scaled;
        double fraction = scaled - whole;
        return whole + (LootRoll.roll(null, null, fraction * 100, random) ? 1 : 0);
    }

    public static long scaledAmount(long amount, double extraPercent, RandomGenerator random) {
        if (amount <= 0 || extraPercent <= 0) return amount;
        double scaled = amount * (1 + extraPercent / 100D);
        long whole = (long) scaled;
        double fraction = scaled - whole;
        return whole + (LootRoll.roll(null, null, fraction * 100, random) ? 1 : 0);
    }

    public static int reducedCooldown(int ticks, double reductionPercent) {
        if (ticks <= 0 || reductionPercent <= 0) return ticks;
        return (int) Math.round(ticks * (1 - Math.min(reductionPercent, 100) / 100D));
    }

    public static boolean tramples(int blocksFallen) {
        return blocksFallen >= TRAMPLE_MIN_FALL;
    }

    public static double trampleDamage(double weaponDamage, double percentPerBlock, int blocksFallen) {
        if (!tramples(blocksFallen)) return 0;
        return weaponDamage * percentPerBlock / 100D * blocksFallen;
    }

    public static double skeletalDefenseDamage(double critDamage) {
        return Math.max(0, critDamage) * SKELETAL_DEFENSE_CRIT_MULTIPLIER;
    }

    public static boolean offCooldown(long lastProc, long now, long cooldownMs) {
        return lastProc == 0 || now - lastProc >= cooldownMs;
    }

    public static double healingAfterReduction(double amount, double reductionPercent) {
        return amount * (1 - Math.clamp(reductionPercent, 0, 100) / 100D);
    }

    public static double boosted(double value, double percent) {
        return value * (1 + percent / 100D);
    }

    public static double chancePerUnit(double chancePercent, int units) {
        return Math.min(100, chancePercent * Math.max(0, units));
    }

    public static double jumpStrength(int jumpBoostLevel) {
        return BASE_JUMP_STRENGTH + JUMP_STRENGTH_PER_LEVEL * Math.max(0, jumpBoostLevel);
    }

    public static double hotspotChance(double baseChance, double petBonus, double cap) {
        return Math.min(cap, baseChance + Math.max(0, petBonus));
    }

    public static boolean doublesSquidDrops(String seaCreatureId) {
        return "SQUID".equals(seaCreatureId) || "NIGHT_SQUID".equals(seaCreatureId);
    }

    public static double speedWithRaisedCap(double rawSpeed, double baseCap, double capBonus) {
        return Math.min(rawSpeed, baseCap + Math.max(0, capBonus));
    }

    public static int bulwarkTier(int kills) {
        int tier = 0;
        while (tier < BULWARK_KILLS.length && kills >= BULWARK_KILLS[tier]) tier++;
        return tier;
    }

    public static int bulwarkDefense(int kills) {
        int tier = bulwarkTier(kills);
        return tier == 0 ? 0 : BULWARK_DEFENSE[tier - 1];
    }

    public static int bulwarkTrueDefense(int kills) {
        int tier = bulwarkTier(kills);
        return tier == 0 ? 0 : BULWARK_TRUE_DEFENSE[tier - 1];
    }

    public static int bulwarkNextKills(int kills) {
        int tier = bulwarkTier(kills);
        return tier >= BULWARK_KILLS.length ? -1 : BULWARK_KILLS[tier];
    }

    public static int bulwarkNextDefense(int kills) {
        int tier = bulwarkTier(kills);
        return tier >= BULWARK_KILLS.length ? -1 : BULWARK_DEFENSE[tier];
    }

    public static int bulwarkNextTrueDefense(int kills) {
        int tier = bulwarkTier(kills);
        return tier >= BULWARK_KILLS.length ? -1 : BULWARK_TRUE_DEFENSE[tier];
    }

    public static double bulwarkDamage(double damage, double playerDefense, double bonusDefense, double bonusTrueDefense) {
        double defense = Math.max(0, playerDefense);
        double afterDefense = damage * (100 + defense) / (100 + defense + Math.max(0, bonusDefense));
        return afterDefense * 100 / (100 + Math.max(0, bonusTrueDefense));
    }

    public static double coinsPerMobLevel(double perLevel, @org.jetbrains.annotations.Nullable Integer mobLevel) {
        if (mobLevel == null || mobLevel <= 0 || perLevel <= 0) return 0;
        return Math.floor(perLevel * mobLevel);
    }

    public static float overhealShield(double maxHealth, double percent) {
        return (float) (Math.max(0, maxHealth) * Math.max(0, percent) / 100D);
    }

    public static boolean overheals(long lastDamageAt, long now, boolean shieldActive) {
        return !shieldActive && now - lastDamageAt >= OVERHEAL_QUIET_MS;
    }

    public static double witherDamagePerSecond(double hitDamage, double percent) {
        return Math.max(0, hitDamage) * Math.max(0, percent) / 100D / WITHER_SECONDS;
    }

    public static double reducedBobberSeconds(double seconds, double lessTimePercent) {
        return seconds * (1 - Math.clamp(lessTimePercent, 0, 100) / 100D);
    }

    public static double manaCostFactor(double lessPercent) {
        return 1 - Math.clamp(lessPercent, 0, 100) / 100D;
    }

    public static double trainingRefundPercent(double coinCost) {
        double steps = Math.floor(Math.max(0, coinCost) / TRAINING_REFUND_STEP_COINS);
        return Math.min(TRAINING_REFUND_MAX_PERCENT, steps);
    }

    public static long discountedTrainingCost(double coinCost, double percent) {
        return Math.round(coinCost * (1 - Math.clamp(percent, 0, 100) / 100D));
    }

    public static double chocolateMultiplier(double baseMultiplier, double petBonus) {
        return baseMultiplier + Math.max(0, petBonus);
    }

    public static double bestAura(double current, double candidate) {
        return Math.max(current, candidate);
    }

    public static boolean inAuraRange(double distance) {
        return distance <= BIRD_DISCOURSE_RANGE;
    }
}
