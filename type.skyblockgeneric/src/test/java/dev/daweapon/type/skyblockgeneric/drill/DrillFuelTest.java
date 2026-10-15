package dev.daweapon.type.skyblockgeneric.drill;

import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeDrillData;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeDrillData.DrillData;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrillFuelTest {
    private static DrillData with(String tank, String engine, String module) {
        return new DrillData(DrillData.BASE_FUEL, tank, engine, module);
    }

    @Test
    void drillsStartWithThreeThousandFuelAndNoTank() {
        assertEquals(3_000, DrillFuel.capacity(DrillData.EMPTY));
        assertEquals(3_000, DrillData.EMPTY.fuel());
    }

    @Test
    void tanksSetTheCapacityFromTheWiki() {
        assertEquals(10_000, DrillFuel.capacity(with("MITHRIL_FUEL_TANK", null, null)));
        assertEquals(25_000, DrillFuel.capacity(with("TITANIUM_FUEL_TANK", null, null)));
        assertEquals(50_000, DrillFuel.capacity(with("GEMSTONE_FUEL_TANK", null, null)));
        assertEquals(100_000, DrillFuel.capacity(with("PERFECTLY_CUT_FUEL_TANK", null, null)));
    }

    @Test
    void consumingNeverGoesBelowZero() {
        DrillData data = new DrillData(2, null, null, null);
        assertEquals(1, DrillFuel.consume(data, 1).fuel());
        assertEquals(0, DrillFuel.consume(data, 3).fuel());
    }

    @Test
    void refuellingIsCappedAtTheTankCapacity() {
        DrillData data = new DrillData(2_500, null, null, null);
        assertEquals(3_000, DrillFuel.refuel(data, 3_000, 1).fuel());
        DrillData roomy = new DrillData(0, "TITANIUM_FUEL_TANK", null, null);
        assertEquals(20_000, DrillFuel.refuel(roomy, 10_000, 2).fuel());
        assertEquals(25_000, DrillFuel.refuel(roomy, 10_000, 5).fuel());
    }

    @Test
    void onlyAsManyItemsAreConsumedAsFitInTheTank() {
        DrillData data = new DrillData(0, null, null, null);
        assertEquals(1, DrillFuel.itemsToFill(data, 3_000, 64));
        DrillData big = new DrillData(0, "PERFECTLY_CUT_FUEL_TANK", null, null);
        assertEquals(10, DrillFuel.itemsToFill(big, 10_000, 64));
        assertEquals(4, DrillFuel.itemsToFill(big, 10_000, 4));
        assertEquals(0, DrillFuel.itemsToFill(DrillFuel.fill(big), 10_000, 4));
        assertEquals(0, DrillFuel.itemsToFill(big, 10_000, 0));
    }

    @Test
    void sunnySideOmeletteQuadruplesTheFuelCost() {
        assertEquals(1, DrillFuel.blockCost(DrillFuel.DEFAULT_BLOCK_COST, DrillData.EMPTY));
        DrillData sunny = with(null, null, "GOBLIN_OMELETTE_SUNNY_SIDE");
        assertEquals(4, DrillFuel.blockCost(DrillFuel.DEFAULT_BLOCK_COST, sunny));
        assertEquals(12, DrillFuel.blockCost(DrillFuel.GLACITE_BLOCK_COST, sunny));
    }

    @Test
    void fuelSaveChanceFollowsTheOmelette() {
        RandomGenerator alwaysLow = () -> 0L;
        RandomGenerator alwaysHigh = () -> -1L;
        assertFalse(DrillFuel.savesFuel(DrillData.EMPTY, alwaysLow));
        DrillData goblin = with(null, null, "GOBLIN_OMELETTE");
        DrillData pesto = with(null, null, "GOBLIN_OMELETTE_PESTO");
        assertTrue(DrillFuel.savesFuel(goblin, alwaysLow));
        assertFalse(DrillFuel.savesFuel(goblin, alwaysHigh));
        assertFalse(DrillFuel.savesFuel(pesto, alwaysHigh));
        assertEquals(25, DrillParts.fuelSaveChance(goblin));
        assertEquals(75, DrillParts.fuelSaveChance(pesto));
    }

    @Test
    void tungstenRegulatorStepsEveryFiveHundredFuel() {
        assertEquals(0, DrillFuel.regulationSteps(1, 498, 500));
        assertEquals(1, DrillFuel.regulationSteps(2, 498, 500));
        assertEquals(2, DrillFuel.regulationSteps(1_000, 0, 500));
        assertEquals(0, DrillFuel.regulationSteps(10, 0, 0));
    }

    @Test
    void enginesAndModulesAddMiningStatistics() {
        DrillData data = with(null, "TITANIUM_DRILL_ENGINE", "STARFALL_SEASONING");
        var stats = DrillParts.statistics(data);
        assertEquals(175.0, stats.getOverall(ItemStatistic.MINING_SPEED));
        assertEquals(25.0, stats.getOverall(ItemStatistic.MINING_FORTUNE));
        assertEquals(0.0, DrillParts.statistics(DrillData.EMPTY).getOverall(ItemStatistic.MINING_SPEED));
    }

    @Test
    void sunnySideAddsFiftyMiningFortune() {
        var stats = DrillParts.statistics(with(null, null, "GOBLIN_OMELETTE_SUNNY_SIDE"));
        assertEquals(50.0, stats.getOverall(ItemStatistic.MINING_FORTUNE));
    }

    @Test
    void tanksReduceTheAbilityCooldown() {
        assertEquals(10, DrillParts.cooldownReductionPercent(with("PERFECTLY_CUT_FUEL_TANK", null, null)));
        assertEquals(0, DrillParts.cooldownReductionPercent(DrillData.EMPTY));
    }

    @Test
    void installingAndRemovingClampsFuelToTheNewCapacity() {
        DrillPart tank = DrillParts.get("MITHRIL_FUEL_TANK");
        DrillData installed = DrillParts.install(DrillData.EMPTY, tank);
        assertEquals("MITHRIL_FUEL_TANK", DrillParts.installed(installed, DrillSlot.TANK));
        DrillData filled = DrillFuel.fill(installed);
        assertEquals(10_000, filled.fuel());
        DrillData removed = DrillParts.remove(filled, DrillSlot.TANK);
        assertNull(removed.tank());
        assertEquals(3_000, removed.fuel());
    }

    @Test
    void everyPartSitsInTheSlotItsItemNamesAndMachineFuelsMatchTheWiki() {
        assertEquals(DrillSlot.ENGINE, DrillParts.get("AMBER_POLISHED_DRILL_ENGINE").slot());
        assertEquals(DrillSlot.MODULE, DrillParts.get("TUNGSTEN_KEYCHAIN").slot());
        assertEquals(16, DrillParts.all().size());
        assertEquals(3_000, MachineFuel.amount("BIOFUEL"));
        assertEquals(10_000, MachineFuel.amount("VOLTA"));
        assertEquals(10_000, MachineFuel.amount("OIL_BARREL"));
        assertEquals(0, MachineFuel.amount("DIAMOND"));
    }

    @Test
    void drillDataSurvivesTheAttributeRoundTrip() {
        ItemAttributeDrillData attribute = new ItemAttributeDrillData();
        DrillData data = new DrillData(1_234, "GEMSTONE_FUEL_TANK", null, "GOBLIN_OMELETTE_PESTO");
        attribute.setValue(data);
        ItemAttributeDrillData loaded = new ItemAttributeDrillData();
        loaded.setValue(loaded.loadFromString(attribute.saveIntoString()));
        assertEquals(data, loaded.getValue());
    }

    @Test
    void anUntouchedDrillStoresNothingAndBadDataFallsBackToDefaults() {
        ItemAttributeDrillData attribute = new ItemAttributeDrillData();
        assertNull(attribute.saveIntoString());
        assertEquals(DrillData.EMPTY, attribute.loadFromString("garbage"));
        assertEquals(DrillData.EMPTY, attribute.loadFromString("x|-|-|-"));
    }
}
