package dev.daweapon.type.theend.dragon;

import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

public enum DragonVariant {
    PROTECTOR("Protector", "PROTECTOR", 333, 9_000_000, 85, 1100, 140, 2497, 999, 832, 40, 1, 1, 1),
    OLD("Old", "OLD", 358, 15_000_000, 18, 1100, 130, 2685, 1074, 895, 30, 1, 1, 1),
    WISE("Wise", "WISE", 367, 9_000_000, 25, 2200, 140, 2752, 1101, 917, 40, 1, 1.2, 1),
    UNSTABLE("Unstable", "UNSTABLE", 333, 9_000_000, 25, 1100, 140, 2497, 999, 832, 40, 1, 1, 1),
    YOUNG("Young", "YOUNG", 325, 7_500_000, 18, 1100, 225, 2437, 975, 812, 100, 1, 1, 1),
    STRONG("Strong", "STRONG", 333, 9_000_000, 25, 1100, 140, 2497, 999, 832, 40, 1.2, 1, 1.2),
    SUPERIOR("Superior", "SUPERIOR", 367, 12_000_000, 30, 1650, 180, 2752, 1101, 917, 65, 1.15, 1.15, 1.15);

    private static final double BASE_SUPERIOR_CHANCE = 4;
    private static final double SUPERIOR_CHANCE_PER_EYE = 12.5;

    private final String displayName;
    private final String itemPrefix;
    private final int level;
    private final double health;
    private final double defense;
    private final double damage;
    private final double speed;
    private final long combatXp;
    private final int orbs;
    private final int coins;
    private final int flyHeight;
    private final double rushMultiplier;
    private final double projectileMultiplier;
    private final double contactMultiplier;

    DragonVariant(String displayName, String itemPrefix, int level, double health, double defense, double damage,
                  double speed, long combatXp, int orbs, int coins, int flyHeight,
                  double rushMultiplier, double projectileMultiplier, double contactMultiplier) {
        this.displayName = displayName;
        this.itemPrefix = itemPrefix;
        this.level = level;
        this.health = health;
        this.defense = defense;
        this.damage = damage;
        this.speed = speed;
        this.combatXp = combatXp;
        this.orbs = orbs;
        this.coins = coins;
        this.flyHeight = flyHeight;
        this.rushMultiplier = rushMultiplier;
        this.projectileMultiplier = projectileMultiplier;
        this.contactMultiplier = contactMultiplier;
    }

    public String displayName() { return displayName + " Dragon"; }
    public String itemPrefix() { return itemPrefix; }
    public int level() { return level; }
    public double health() { return health; }
    public double defense() { return defense; }
    public double damage() { return damage; }
    public double speed() { return speed; }
    public long combatXp() { return combatXp; }
    public int orbs() { return orbs; }
    public int coins() { return coins; }
    public int flyHeight() { return flyHeight; }
    public double rushMultiplier() { return rushMultiplier; }
    public double projectileMultiplier() { return projectileMultiplier; }
    public double contactMultiplier() { return contactMultiplier; }
    public boolean superior() { return this == SUPERIOR; }
    public int essenceReward() { return superior() ? 10 : 5; }
    public int firstKillXp() { return superior() ? 50 : 25; }
    public String fragmentId() { return itemPrefix + "_FRAGMENT"; }
    public String armorId(String piece) { return itemPrefix + "_DRAGON_" + piece; }

    public static double superiorChance(int recombobulatedEyes) {
        if (recombobulatedEyes <= 0) return BASE_SUPERIOR_CHANCE;
        return Math.min(100, recombobulatedEyes * SUPERIOR_CHANCE_PER_EYE);
    }

    public static DragonVariant roll(int recombobulatedEyes, RandomGenerator random) {
        double superior = superiorChance(recombobulatedEyes);
        if (random.nextDouble() * 100 < superior) return SUPERIOR;
        List<DragonVariant> others = Arrays.stream(values()).filter(variant -> !variant.superior()).toList();
        return others.get(random.nextInt(others.size()));
    }
}
