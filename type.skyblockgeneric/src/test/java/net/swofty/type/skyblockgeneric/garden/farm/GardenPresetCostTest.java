package net.swofty.type.skyblockgeneric.garden.farm;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenPresetCostTest {
    private static final ItemType SEEDS = ItemType.valueOf("SEEDS");
    private static final ItemType ENCHANTED_SEEDS = ItemType.valueOf("ENCHANTED_SEEDS");
    private static final List<GardenPresetCost.Source> SOURCES = List.of(
            new GardenPresetCost.Source(SEEDS, 1),
            new GardenPresetCost.Source(ENCHANTED_SEEDS, 160));

    @Test
    void everyCropPresetPlantsAndCostsSomething() {
        for (GardenCrop crop : GardenCrop.values()) {
            assertTrue(GardenPresetCost.required(crop) > 0, crop.name());
            assertNotNull(GardenPresetCost.seed(crop), crop.name());
            assertEquals(GardenPresetCost.seed(crop), GardenPresetCost.sources(crop).getFirst().item(), crop.name());
        }
    }

    @Test
    void wheatCostsOneSeedPerPlantedBlock() {
        assertEquals(80 * 90, GardenPresetCost.required(GardenCrop.WHEAT));
    }

    @Test
    void baseSeedsArePaidBeforeEnchantedOnes() {
        GardenPresetCost.Payment payment = GardenPresetCost.plan(100, SOURCES, item -> item.equals(SEEDS) ? 500 : 5);
        assertEquals(Map.of(SEEDS, 100), payment.take());
        assertEquals(0, payment.refund());
    }

    @Test
    void enchantedSeedsCoverTheRestAndTheDifferenceIsRefunded() {
        GardenPresetCost.Payment payment = GardenPresetCost.plan(200, SOURCES, item -> item.equals(SEEDS) ? 50 : 3);
        assertEquals(Map.of(SEEDS, 50, ENCHANTED_SEEDS, 1), payment.take());
        assertEquals(10, payment.refund());
        assertEquals(SEEDS, payment.refundItem());
    }

    @Test
    void shortfallsAreRejected() {
        assertNull(GardenPresetCost.plan(1_000, SOURCES, item -> item.equals(SEEDS) ? 10 : 2));
    }
}
