package dev.daweapon.type.skyblockgeneric.drill;

public record DrillPart(String itemId, DrillSlot slot, int maxFuel, double cooldownReduction, double miningSpeed,
                        double miningFortune, double fuelSaveChance, int fuelMultiplier, double powderPercent,
                        int perkLevels, int fuelPerRegulation) {

    public static DrillPart tank(String itemId, int maxFuel, double cooldownReduction) {
        return new DrillPart(itemId, DrillSlot.TANK, maxFuel, cooldownReduction, 0, 0, 0, 1, 0, 0, 0);
    }

    public static DrillPart engine(String itemId, double miningSpeed, double miningFortune) {
        return new DrillPart(itemId, DrillSlot.ENGINE, 0, 0, miningSpeed, miningFortune, 0, 1, 0, 0, 0);
    }

    public static DrillPart module(String itemId) {
        return new DrillPart(itemId, DrillSlot.MODULE, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    }

    public DrillPart withStats(double miningSpeed, double miningFortune) {
        return new DrillPart(itemId, slot, maxFuel, cooldownReduction, miningSpeed, miningFortune, fuelSaveChance,
                fuelMultiplier, powderPercent, perkLevels, fuelPerRegulation);
    }

    public DrillPart withFuelSave(double fuelSaveChance) {
        return new DrillPart(itemId, slot, maxFuel, cooldownReduction, miningSpeed, miningFortune, fuelSaveChance,
                fuelMultiplier, powderPercent, perkLevels, fuelPerRegulation);
    }

    public DrillPart withFuelMultiplier(int fuelMultiplier) {
        return new DrillPart(itemId, slot, maxFuel, cooldownReduction, miningSpeed, miningFortune, fuelSaveChance,
                fuelMultiplier, powderPercent, perkLevels, fuelPerRegulation);
    }

    public DrillPart withPowder(double powderPercent) {
        return new DrillPart(itemId, slot, maxFuel, cooldownReduction, miningSpeed, miningFortune, fuelSaveChance,
                fuelMultiplier, powderPercent, perkLevels, fuelPerRegulation);
    }

    public DrillPart withPerkLevels(int perkLevels) {
        return new DrillPart(itemId, slot, maxFuel, cooldownReduction, miningSpeed, miningFortune, fuelSaveChance,
                fuelMultiplier, powderPercent, perkLevels, fuelPerRegulation);
    }

    public DrillPart withRegulation(int fuelPerRegulation) {
        return new DrillPart(itemId, slot, maxFuel, cooldownReduction, miningSpeed, miningFortune, fuelSaveChance,
                fuelMultiplier, powderPercent, perkLevels, fuelPerRegulation);
    }
}
