package dev.daweapon.type.skyblockgeneric.trapper;

import lombok.Getter;

@Getter
public enum TrapperRarity {
    TRACKABLE("Trackable", "<f>", 100, 2, 50, 1, 240, 0, 0, false, false, 250_000),
    UNTRACKABLE("Untrackable", "<a>", 500, 4, 100, 10, 180, 100, 14, false, false, 200_000),
    UNDETECTED("Undetected", "<9>", 1_000, 6, 250, 100, 120, 180, 16, false, false, 150_000),
    ENDANGERED("Endangered", "<5>", 5_000, 8, 500, 1_000, 60, 260, 18, true, false, 100_000),
    ELUSIVE("Elusive", "<6>", 10_000, 16, 3_000, 10_000, 6, 340, 20, true, true, 10_000);

    private final String displayName;
    private final String colour;
    private final int health;
    private final int pelts;
    private final int huntingXp;
    private final int coins;
    private final int weight;
    private final int fleeSpeed;
    private final int fleeRadius;
    private final boolean blinds;
    private final boolean aware;
    private final int dyeOdds;

    TrapperRarity(String displayName, String colour, int health, int pelts, int huntingXp, int coins, int weight,
                  int fleeSpeed, int fleeRadius, boolean blinds, boolean aware, int dyeOdds) {
        this.displayName = displayName;
        this.colour = colour;
        this.health = health;
        this.pelts = pelts;
        this.huntingXp = huntingXp;
        this.coins = coins;
        this.weight = weight;
        this.fleeSpeed = fleeSpeed;
        this.fleeRadius = fleeRadius;
        this.blinds = blinds;
        this.aware = aware;
        this.dyeOdds = dyeOdds;
    }

    public String coloured() {
        return colour + displayName;
    }

    public double dyeChancePercent() {
        return 100D / dyeOdds;
    }
}
