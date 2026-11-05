package dev.daweapon.dungeons.catacombs.classes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Per-level numbers for every class. The base values and per-level steps are from the wiki's class pages and the
 * class menus' tooltips; {@code strength} is {@link DungeonClassParty#LONE_CLASS_MULTIPLIER} when only one member plays
 * the class, which doubles positive bonuses. Values the wiki gives only as a range (ability damage) are interpolated
 * linearly across levels 1 to 50; values it does not give at all (Healing Circle radius and duration at level 0, Buff
 * Potion duration, stun duration) are estimates.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DungeonClassScaling {
    public static final int MAXIMUM_LEVEL = DungeonClassXp.MAXIMUM_LEVEL;

    public static final int HEALING_AURA_RADIUS = 8;
    public static final int GHOST_POTION_RADIUS = 10;
    public static final int THUNDERSTORM_RADIUS = 10;
    public static final int THUNDERSTORM_SECONDS = 15;
    public static final int MAGE_STAFF_RANGE = 10;
    public static final double MAGE_STAFF_BASE_FACTOR = 0.30;
    public static final int EXPLOSIVE_SHOT_RADIUS = 4;
    public static final int EXPLOSIVE_SHOT_ARROWS = 3;
    public static final int RAPID_FIRE_ARROWS_PER_SECOND = 5;
    public static final double RAPID_FIRE_DAMAGE_SHARE = 0.75;
    public static final int RAGNAROK_SECONDS = 15;
    public static final int RAGNAROK_ATTACK_SPEED = 100;
    public static final int RAGNAROK_SPEED = 400;
    public static final double RAGNAROK_MELEE_MULTIPLIER = 1.5;
    public static final double BLOODLUST_MISSING_HEALTH_PERCENT = 3;
    public static final int CASTLE_OF_STONE_REDUCTION_PERCENT = 70;
    public static final int CASTLE_OF_STONE_SECONDS = 20;
    public static final int CASTLE_OF_STONE_AGGRO_RADIUS = 10;
    public static final int DIVERSION_PERCENT = 80;
    public static final int DIVERSION_RADIUS = 30;
    public static final int ABSORPTION_POTION_SECONDS = 3;
    public static final int HEALING_BOW_PERCENT = 10;
    public static final int HEALING_BOW_STRENGTH = 50;
    public static final int HEALING_BOW_SECONDS = 10;
    public static final int INSTANT_WALL_SECONDS = 10;
    public static final int BUFF_POTION_SECONDS = 10;
    public static final int STUN_SECONDS = 3;
    public static final int ARCHER_MELEE_PENALTY_PERCENT = 25;

    public static int clampLevel(int level) {
        return Math.max(0, Math.min(MAXIMUM_LEVEL, level));
    }

    private static int fives(int level) {
        return clampLevel(level) / 5;
    }

    static double interpolatedTotal(int level, double first, double last) {
        int capped = clampLevel(level);
        double total = first;
        for (int step = 1; step <= capped; step++) {
            total += first + (last - first) * (step - 1) / (MAXIMUM_LEVEL - 1);
        }
        return total;
    }

    public static double renewPercent(int level, int strength) {
        return (50 + clampLevel(level)) * strength;
    }

    public static double healingAuraPercent(int level, int strength) {
        return (1 + 0.1 * fives(level)) * strength;
    }

    public static double healingCirclePercent(int level, int strength) {
        return (1 + 0.5 * fives(level)) * strength;
    }

    public static double healingCircleRadius(int level) {
        return 3 + 0.5 * fives(level);
    }

    public static int healingCircleSeconds(int level) {
        return 5 + fives(level);
    }

    public static double wishShieldPercent(int level, int strength) {
        return (20 + clampLevel(level)) * strength;
    }

    public static double ghostHealingPotionPercent(int level, int strength) {
        return (10 + clampLevel(level) / 2) * strength;
    }

    public static double ghostHealingPotionFlat(int level, int strength) {
        return (100 + 2 * clampLevel(level)) * strength;
    }

    public static int selfReviveSeconds(int level) {
        return 100 - clampLevel(level);
    }

    public static double mageCooldownReductionPercent(int level) {
        return 25 + clampLevel(level) / 2;
    }

    public static double mageIntelligence(int level, int strength) {
        return (250 + 5 * clampLevel(level)) * strength;
    }

    public static double mageAbilityDamage(int level, int strength) {
        return (5 + fives(level)) * strength;
    }

    public static double mageStaffFactor(double intelligence) {
        return MAGE_STAFF_BASE_FACTOR + 0.9 * Math.max(0, intelligence) / 1000;
    }

    public static double guidedSheepDamage(int level, int strength) {
        return interpolatedTotal(level, 100, 2000) * strength;
    }

    public static double fireballDamage(int level, int strength) {
        return interpolatedTotal(level, 80, 1600) * strength;
    }

    public static double thunderstormDamage(int level, int strength) {
        return guidedSheepDamage(level, strength) / 2;
    }

    public static double berserkMeleePercent(int level, int strength) {
        return (40 + 0.75 * clampLevel(level)) * strength;
    }

    public static double berserkSpeed(int level, int strength) {
        return (30 + 0.4 * fives(level)) * strength;
    }

    public static double throwingAxeMultiplier(int level, int strength) {
        return (1 + clampLevel(level) / 100.0) * strength;
    }

    public static double ghostAxeDamage(int level, int strength) {
        return interpolatedTotal(level, 50, 1000) * strength;
    }

    public static double buffPotionStrength(int level, int strength) {
        return (30 + 2 * fives(level)) * strength;
    }

    public static double archerArrowPercent(int level, int strength) {
        return (120 + 1.6 * clampLevel(level)) * strength;
    }

    public static double doubleshotChancePercent(int level, int strength) {
        return Math.min(100, (50 + clampLevel(level)) * strength);
    }

    public static int explosiveShotCooldownSeconds(int level) {
        return 40 - 2 * fives(level);
    }

    public static int rapidFireSeconds(int level) {
        return 4 + clampLevel(level) / 10;
    }

    public static double tankHealth(int strength) {
        return 100.0 * strength;
    }

    public static double tankDefense(int level, int strength) {
        int capped = clampLevel(level);
        return (50 + capped + (capped == MAXIMUM_LEVEL ? 1 : 0)) * strength;
    }

    public static double tankVitality(int level, int strength) {
        int capped = clampLevel(level);
        return (10 + 0.1 * capped + (capped == MAXIMUM_LEVEL ? 0.1 : 0)) * strength;
    }

    public static double tankDefensePercent(int level, int strength) {
        return (25 + 0.2 * clampLevel(level)) * strength;
    }

    public static double seismicWaveDamage(int level, double defense, int strength) {
        double base = 20_000 + 28_000.0 * clampLevel(level);
        return base * (1 + 0.1 * Math.floor(Math.max(0, defense) / 50)) * strength;
    }

    public static double absorptionPotion(int level, int strength) {
        return (200 + 10 * clampLevel(level)) * strength;
    }

    public static double healerVitality(int level, int strength) {
        return 0.2 * clampLevel(level) * strength;
    }

    public static double healerMending(int level, int strength) {
        return 0.5 * clampLevel(level) * strength;
    }

    /** An ability's cooldown for this class and level, after Explosive Shot's per-level cut and Mage's reduction. */
    public static double cooldownSeconds(DungeonClassAbility ability, DungeonClassType type, int level) {
        double seconds = "EXPLOSIVE_SHOT".equals(ability.id()) ? explosiveShotCooldownSeconds(level) : ability.cooldownSeconds();
        if (type == DungeonClassType.MAGE && ability.type() != DungeonClassAbilityType.GHOST) {
            seconds *= 1 - mageCooldownReductionPercent(level) / 100;
        }
        return seconds;
    }
}
