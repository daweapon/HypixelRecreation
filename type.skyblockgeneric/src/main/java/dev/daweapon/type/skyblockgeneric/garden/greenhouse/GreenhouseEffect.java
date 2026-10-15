package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import lombok.Getter;

@Getter
public enum GreenhouseEffect {
    HARVEST_BOOST("Harvest Boost", "Increases Yield of adjacent crops by +20%.", false, 0.20),
    IMPROVED_HARVEST_BOOST("Improved Harvest Boost", "Increases Yield of adjacent crops by +30%.", false, 0.30),
    XP_BOOST("XP Boost", "Increases Farming XP of adjacent crops by +20%.", false, 0.20),
    IMPROVED_XP_BOOST("Improved XP Boost", "Increases Farming XP of adjacent crops by +30%.", false, 0.30),
    WATER_RETAIN("Water Retain", "Retains watering status of adjacent crops by +50%.", false, 0.50),
    IMPROVED_WATER_RETAIN("Improved Water Retain", "Retains watering status of adjacent crops by +100%.", false, 1.00),
    IMMUNITY("Immunity", "Provides immunity to negative effects.", false, 0),
    BONUS_DROPS("Bonus Drops", "Harvested items additionally roll from an extra loot pool.", false, 0),
    EFFECT_SPREAD("Effect Spread", "Spreads other buffs and debuffs on the current crop to adjacent crops.", false, 0),
    HARVEST_LOSS("Harvest Loss", "Decreases Yield of adjacent crops by -20%.", true, 0.20),
    XP_LOSS("XP Loss", "Decreases Farming XP of adjacent crops by -20%.", true, 0.20),
    WATER_DRAIN("Water Drain", "Decreases watering status of adjacent crops by -30%.", true, 0.30);

    private final String displayName;
    private final String description;
    private final boolean negative;
    private final double magnitude;

    GreenhouseEffect(String displayName, String description, boolean negative, double magnitude) {
        this.displayName = displayName;
        this.description = description;
        this.negative = negative;
        this.magnitude = magnitude;
    }

    public boolean spreadable() {
        return this != IMMUNITY && this != EFFECT_SPREAD && this != BONUS_DROPS;
    }
}
