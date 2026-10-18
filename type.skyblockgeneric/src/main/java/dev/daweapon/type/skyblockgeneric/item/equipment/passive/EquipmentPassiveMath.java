package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.Set;
import java.util.random.RandomGenerator;

public final class EquipmentPassiveMath {
    public static final String BLAZE_BELT = "BLAZE_BELT";
    public static final String VANQUISHED_BLAZE_BELT = "VANQUISHED_BLAZE_BELT";
    public static final String MAGMA_NECKLACE = "MAGMA_NECKLACE";
    public static final String VANQUISHED_MAGMA_NECKLACE = "VANQUISHED_MAGMA_NECKLACE";
    public static final String VANQUISHED_GHAST_CLOAK = "VANQUISHED_GHAST_CLOAK";
    public static final String VANQUISHED_GLOWSTONE_GAUNTLET = "VANQUISHED_GLOWSTONE_GAUNTLET";
    public static final String SCOVILLE_BELT = "SCOVILLE_BELT";
    public static final String DELIRIUM_NECKLACE = "DELIRIUM_NECKLACE";
    public static final String LAVA_SHELL_NECKLACE = "LAVA_SHELL_NECKLACE";
    public static final String TERA_SHELL_NECKLACE = "TERA_SHELL_NECKLACE";
    public static final String FLAMING_FIST = "FLAMING_FIST";
    public static final String IMPLOSION_BELT = "IMPLOSION_BELT";
    public static final String GAUNTLET_OF_CONTAGION = "GAUNTLET_OF_CONTAGION";
    public static final String ANCIENT_CLOAK = "ANCIENT_CLOAK";
    public static final String SCOURGE_CLOAK = "SCOURGE_CLOAK";

    public static final double BLAZE_BELT_REFLECT_PERCENT = 10;
    public static final double VANQUISHED_BLAZE_BELT_REFLECT_PERCENT = 20;
    public static final double MAGMA_NECKLACE_CREAM_PERCENT = 10;
    public static final double VANQUISHED_MAGMA_NECKLACE_CREAM_PERCENT = 20;
    public static final int COUNTER_STEP = 1_000;
    public static final int COUNTER_MAX_BONUS = 10;
    public static final double SCOVILLE_DEFENSE_PER_ENEMY = 50;
    public static final double SCOVILLE_MAX_DEFENSE = 1_000;
    public static final double SCOVILLE_RADIUS = 10;
    public static final double DELIRIUM_FIRE_PERCENT_PER_STRENGTH = 8;
    public static final double FLAMING_FIST_DAMAGE = 10_000;
    public static final int FLAMING_FIST_COUNT = 3;
    public static final int FLAMING_FIST_SPACING_TICKS = 4;
    public static final double IMPLOSION_BELT_MULTIPLIER = 1.25;
    public static final double CONTAGION_HEALTH_FRACTION = 0.10;
    public static final double CONTAGION_RADIUS = 2;
    public static final double ANCIENT_CLOAK_MULTIPLIER = 1.10;
    public static final double SCOURGE_FIRE_INCREASE_PERCENT = 10_000;
    public static final double SCOURGE_NEXT_HIT_FRACTION = 0.20;

    private EquipmentPassiveMath() {
    }

    public static double reflectChancePercent(Set<String> worn) {
        if (worn.contains(VANQUISHED_BLAZE_BELT)) return VANQUISHED_BLAZE_BELT_REFLECT_PERCENT;
        return worn.contains(BLAZE_BELT) ? BLAZE_BELT_REFLECT_PERCENT : 0;
    }

    public static double magmaCreamPercent(Set<String> worn) {
        if (worn.contains(VANQUISHED_MAGMA_NECKLACE)) return VANQUISHED_MAGMA_NECKLACE_CREAM_PERCENT;
        return worn.contains(MAGMA_NECKLACE) ? MAGMA_NECKLACE_CREAM_PERCENT : 0;
    }

    public static int scaledAmount(int amount, double bonusPercent, RandomGenerator random) {
        if (amount <= 0 || bonusPercent <= 0) return amount;
        double exact = amount * (1 + bonusPercent / 100);
        int whole = (int) Math.floor(exact);
        double fraction = exact - whole;
        return fraction > 0 && LootRoll.roll(null, null, fraction * 100, random) ? whole + 1 : whole;
    }

    public static int counterBonus(long counter) {
        return (int) Math.max(0, Math.min(COUNTER_MAX_BONUS, counter / COUNTER_STEP));
    }

    public static double scovilleDefense(int ignitedEnemies) {
        return Math.min(SCOVILLE_MAX_DEFENSE, Math.max(0, ignitedEnemies) * SCOVILLE_DEFENSE_PER_ENEMY);
    }

    public static double deliriumMultiplier(double strength) {
        return 1 + Math.max(0, strength) * DELIRIUM_FIRE_PERCENT_PER_STRENGTH / 100;
    }

    public static double scourgeFireMultiplier() {
        return 1 + SCOURGE_FIRE_INCREASE_PERCENT / 100;
    }

    public static double scourgeNextHitBonus(double lastFireDamage) {
        return Math.max(0, lastFireDamage) * SCOURGE_NEXT_HIT_FRACTION;
    }

    public static double contagionDamage(double killedMaxHealth) {
        return Math.max(0, killedMaxHealth) * CONTAGION_HEALTH_FRACTION;
    }
}
