package dev.daweapon.type.skyblockgeneric.item.armor;

import java.util.Set;

public final class ArmorAbilities {
    public static final double FROZEN_BLAZE_BASE_DAMAGE = 300;
    public static final double FROZEN_BLAZE_HEALTH_PERCENT = 3;
    public static final double FROZEN_BLAZE_BASE_CAP = 500;
    public static final double FROZEN_BLAZE_CAP_PER_STEP = 100;
    public static final long FROZEN_BLAZE_ROD_STEP = 5_000;
    public static final double FROZEN_BLAZE_MAX_CAP = 5_000;
    public static final double FROZEN_BLAZE_RANGE = 5;
    public static final double UNSTABLE_DAMAGE = 3_000;
    public static final double UNSTABLE_RANGE = 8;
    public static final int UNSTABLE_INTERVAL_TICKS = 300;
    public static final double MIST_AURA_MULTIPLIER = 0.6;
    public static final double REAPER_UNDEAD_MULTIPLIER = 2;
    public static final double REAPER_OTHER_MULTIPLIER = 0.01;
    public static final double UNDEAD_DEFENSE = 100;
    public static final double ANIMAL_DEFENSE = 50;
    public static final double PACK_TRUE_DEFENSE = 5;
    public static final double PACK_STRENGTH = 35;
    public static final double PACK_DEFENSE = 80;
    public static final int PACK_MAX_PLAYERS = 3;
    public static final double PACK_RANGE = 30;
    public static final double CHICKEN_FALL_MULTIPLIER = 0.95;
    public static final int BONE_SHIELD_MAX = 3;
    public static final int BONE_SHIELD_REGEN_TICKS = 600;
    public static final double SKELETON_HAT_CHANCE = 20;
    public static final double SKELETON_HAT_DAMAGE = 50;
    public static final double SKELETON_HAT_RANGE = 8;
    public static final double SPIKY_CHANCE = 20;
    public static final double SPIKY_BASE_DAMAGE = 10;
    public static final double SPIKY_STRENGTH_PERCENT = 20;
    public static final double SPIKY_RANGE = 5;
    public static final double WATER_BURST_SPEED = 25;
    public static final int WATER_BURST_MANA = 20;
    public static final int EGG_COOLDOWN_TICKS = 100;
    public static final double END_STAT_MULTIPLIER = 2;
    public static final double STRONG_BLOOD_DAMAGE = 75;
    public static final double STRONG_BLOOD_STRENGTH = 5;
    public static final double STRONG_BLOOD_RANGE = 2;
    public static final int STRONG_BLOOD_SPEED_TICKS = 60;
    public static final double ENRAGE_STAT = 100;
    public static final long ENRAGE_DURATION_MS = 6_000;
    public static final long ENRAGE_COOLDOWN_MS = 25_000;
    public static final int MIST_AURA_PIECES = 2;
    public static final int TROLLING_PIECES = 3;

    public static final Set<String> NEGATIVE_EFFECTS = Set.of("slowness", "mining_fatigue", "nausea", "blindness",
            "hunger", "weakness", "poison", "wither", "levitation", "darkness", "unluck", "bad_omen");

    private ArmorAbilities() {
    }

    public static double frozenBlazeCap(long blazeRods) {
        double cap = FROZEN_BLAZE_BASE_CAP + FROZEN_BLAZE_CAP_PER_STEP * (Math.max(0, blazeRods) / FROZEN_BLAZE_ROD_STEP);
        return Math.min(FROZEN_BLAZE_MAX_CAP, cap);
    }

    public static double frozenBlazeDamage(double mobMaxHealth, long blazeRods) {
        double raw = FROZEN_BLAZE_BASE_DAMAGE + mobMaxHealth * FROZEN_BLAZE_HEALTH_PERCENT / 100D;
        return Math.min(frozenBlazeCap(blazeRods), raw);
    }

    public static double mistAura(double damage, boolean ghost, int sorrowPieces) {
        return ghost && sorrowPieces >= MIST_AURA_PIECES ? damage * MIST_AURA_MULTIPLIER : damage;
    }

    public static double reaperDealt(double damage, boolean undead) {
        return damage * (undead ? REAPER_UNDEAD_MULTIPLIER : REAPER_OTHER_MULTIPLIER);
    }

    public static double extraDefenseTaken(double damage, double currentDefense, double extraDefense) {
        double base = 100 + Math.max(0, currentDefense);
        return damage * base / (base + Math.max(0, extraDefense));
    }

    public static double fallDamage(double damage) {
        return damage * CHICKEN_FALL_MULTIPLIER;
    }

    public static double spikyDamage(double strength) {
        return SPIKY_BASE_DAMAGE + strength * SPIKY_STRENGTH_PERCENT / 100D;
    }

    public static int packMembers(int nearbyWearers) {
        return Math.max(0, Math.min(PACK_MAX_PLAYERS, nearbyWearers));
    }

    public static boolean isNegative(String effectKey) {
        return NEGATIVE_EFFECTS.contains(effectKey);
    }
}
