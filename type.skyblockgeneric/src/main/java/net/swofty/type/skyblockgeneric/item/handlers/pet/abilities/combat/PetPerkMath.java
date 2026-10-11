package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.loottable.LootRoll;

import java.util.random.RandomGenerator;

public final class PetPerkMath {
    public static final double LONG_NECK_DISTANCE = 3;
    public static final double APEX_PREDATOR_RADIUS = 15;

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
}
