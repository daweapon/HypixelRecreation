package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.loottable.LootRoll;

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
    public static final long STUN_COOLDOWN_MS = 10_000;
    public static final double AURA_RANGE = 50;
    public static final int AURA_TARGETS = 4;
    public static final double BASE_JUMP_STRENGTH = 0.42;
    public static final double JUMP_STRENGTH_PER_LEVEL = 0.1;

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

    public static double speedWithRaisedCap(double rawSpeed, double baseCap, double capBonus) {
        return Math.min(rawSpeed, baseCap + Math.max(0, capBonus));
    }
}
