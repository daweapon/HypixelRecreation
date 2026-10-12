package net.swofty.type.skyblockgeneric.item.armor;

public final class ArmorAbilityMath {
    public static final double[] DEPTH_CHAMPION_REDUCTION = {3, 5, 8, 10};
    public static final double[] DEEPNESS_WITHIN_HEALTH_PER_LEVEL = {6, 7, 9, 10};
    public static final double[] SWAMP_SOLDIER_BONUS = {5, 17, 28, 40};
    public static final double[] ARACHNES_FAITHFUL = {5, 10, 20, 35, 50, 70, 100};
    public static final double NUTCRACKER_GIFT_PERCENT_PER_PIECE = 7.5;
    public static final double[] LONG_TUBA_RESPIRATION = {2, 5, 10};
    public static final double MITHRIL_PROTECTION_MAX_HEALTH_PERCENT = 40;
    public static final double MASTIFF_ANIMAL_REDUCTION = 20;
    public static final double MASTIFF_DEFENSE_CAP = 300;
    public static final double MASTIFF_MAX_HEALTH_PERCENT = 40;
    public static final double PACK_PIECE_ANIMAL_DEFENSE = 50;
    public static final double PACK_CHESTPLATE_ANIMAL_DEFENSE = 75;
    public static final double PACK_TRUE_DEFENSE = 5;
    public static final int OCTODEXTERITY_STRIKES = 4;
    public static final double OCTODEXTERITY_DAMAGE_BONUS = 100;
    public static final double VENOM_HEALING_REDUCTION = 40;
    public static final long VENOM_MILLIS = 4_000;
    public static final double RADIOACTIVE_STRENGTH_PER_CRIT_DAMAGE = 10;
    public static final double RADIOACTIVE_MAX_STRENGTH = 1000;
    public static final double CLOVER_OTHER_STATS_PERCENT = -5;
    public static final double RACING_SPEED_CAP_BONUS = 100;
    public static final int OBSIDIAN_PER_SPEED = 20;
    public static final double HEAT_COATING_MULTIPLIER = 3;
    public static final double BAT_PERSON_NIGHT_MULTIPLIER = 2;
    public static final double BAT_PERSON_FESTIVAL_MULTIPLIER = 3;
    public static final int HOWL_MAX_STACKS = 10;
    public static final double HOWL_DEFENSE_PER_STACK = 50;
    public static final long HOWL_MILLIS = 5_000;
    public static final double DARKNESS_STRENGTH = 30;
    public static final double DARKNESS_ATTACK_SPEED = 20;
    public static final double DARKNESS_SPEED = 10;
    public static final double DARKNESS_INTELLIGENCE_MULTIPLIER = 1.25;
    public static final double DARKNESS_ENDERMAN_DAMAGE_BONUS = 100;
    public static final double DARKNESS_ENDERMAN_FEROCITY = 200;
    public static final int DARKNESS_SOULFLOW_COST = 2;
    public static final long DARKNESS_INTERVAL_MILLIS = 5_000;
    public static final long COMBAT_WINDOW_MILLIS = 5_000;
    public static final int BATTALION_MAX_STACKS = 5;
    public static final double BATTALION_MAGIC_FIND = 1;
    public static final double BATTALION_DEFENSE = 50;
    public static final double TIKI_FISHING_SPEED = 10;
    public static final double TIKI_SEA_CREATURE_CHANCE = 4;
    public static final double TIKI_DOUBLE_HOOK_CHANCE = 2;
    public static final double TIKI_TREASURE_CHANCE = 2;

    private ArmorAbilityMath() {
    }

    public static double tier(double[] table, int wornPieces) {
        return wornPieces <= 0 ? 0 : table[Math.min(wornPieces, table.length) - 1];
    }

    public static double arachnesFaithful(int arachnePieces) {
        if (arachnePieces < 2) return 0;
        return ARACHNES_FAITHFUL[Math.min(arachnePieces, ARACHNES_FAITHFUL.length + 1) - 2];
    }

    public static double radioactiveCritDamage(double strength) {
        return Math.floor(Math.min(Math.max(0, strength), RADIOACTIVE_MAX_STRENGTH) / RADIOACTIVE_STRENGTH_PER_CRIT_DAMAGE);
    }

    public static double reducedByPercent(double damage, double percent) {
        return damage * Math.max(0, 1 - percent / 100D);
    }

    public static double boostedByPercent(double damage, double percent) {
        return damage * (1 + percent / 100D);
    }

    public static double mithrilProtection(double damage, double maxHealth) {
        return Math.min(damage, maxHealth * MITHRIL_PROTECTION_MAX_HEALTH_PERCENT / 100D);
    }

    public static boolean mithrilProtectionTriggers(double damage, double maxHealth) {
        return damage > maxHealth * MITHRIL_PROTECTION_MAX_HEALTH_PERCENT / 100D;
    }

    public static double extraDefenseMultiplier(double defense, double extraDefense) {
        double base = 100D + Math.max(0, defense);
        return base / (base + Math.max(0, extraDefense));
    }

    public static boolean isOctodexterityStrike(int strikeNumber) {
        return strikeNumber > 0 && strikeNumber % OCTODEXTERITY_STRIKES == 0;
    }

    public static double batPersonMultiplier(boolean festival, boolean night) {
        if (festival) return BAT_PERSON_FESTIVAL_MULTIPLIER;
        return night ? BAT_PERSON_NIGHT_MULTIPLIER : 1;
    }

    public static boolean isNight(long worldTime) {
        long dayTime = Math.floorMod(worldTime, 24000L);
        return dayTime >= 13000 && dayTime < 23000;
    }

    public static double obsidianSpeed(int obsidianCount) {
        return Math.max(0, obsidianCount) / OBSIDIAN_PER_SPEED;
    }

    public static double mastiffCritDamage(double cappedDefense) {
        return Math.floor(Math.max(0, cappedDefense) / 2D);
    }

    public static double mastiffHealthPercent(double critDamage) {
        return Math.min(MASTIFF_MAX_HEALTH_PERCENT, Math.floor(Math.max(0, critDamage) / 25D));
    }

    public static int battalionStacks(int otherWearers) {
        return Math.max(0, Math.min(BATTALION_MAX_STACKS, otherWearers));
    }

    public static int howlStacks(int activeStacks) {
        return Math.max(0, Math.min(HOWL_MAX_STACKS, activeStacks));
    }

    public static double raisedSpeedCap(double rawSpeed, double baseCap, double capBonus) {
        return Math.min(rawSpeed, baseCap + Math.max(0, capBonus));
    }
}
