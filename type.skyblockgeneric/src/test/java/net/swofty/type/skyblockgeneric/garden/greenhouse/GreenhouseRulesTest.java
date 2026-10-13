package net.swofty.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseRulesTest {
    private final GreenhouseConfig config = GreenhouseConfig.get();

    private GreenhouseData.Bed bed(Map<Integer, String> plants) {
        GreenhouseData.Bed bed = new GreenhouseData.Bed();
        plants.forEach((slot, type) -> bed.getPlants().put(slot, new GreenhouseData.Plant(type, 0)));
        return bed;
    }

    @Test
    void stageTimeFollowsUpgradesAndUniqueCrops() {
        assertEquals(4 * 3_600_000L, GreenhouseRules.stageMillis(config, 0, 0));
        assertEquals(Math.round(4 * 3_600_000L * 0.85), GreenhouseRules.stageMillis(config, 3, 0));
        assertEquals(Math.round(4 * 3_600_000L * 0.5), GreenhouseRules.stageMillis(config, 9, 0));
        assertEquals(15.0, config.growthSpeedPercent(3));
        assertEquals(6.0, config.plantYieldPercent(3));
        assertEquals(20.0, config.plantYieldPercent(9));
    }

    @Test
    void orthogonalNeighboursApplyEffects() {
        GreenhouseData.Bed bed = bed(Map.of(GreenhouseData.slot(1, 1), "CARROT", GreenhouseData.slot(2, 1), "WHEAT",
                GreenhouseData.slot(2, 2), "WHEAT"));
        Map<Integer, List<GreenhouseEffect>> effects = GreenhouseRules.appliedEffects(bed, config);
        assertEquals(List.of(GreenhouseEffect.HARVEST_BOOST), effects.get(GreenhouseData.slot(1, 1)));
        assertEquals(List.of(GreenhouseEffect.XP_BOOST, GreenhouseEffect.HARVEST_BOOST), effects.get(GreenhouseData.slot(2, 1)).stream().sorted().toList().reversed());
        assertTrue(effects.get(GreenhouseData.slot(2, 2)).contains(GreenhouseEffect.HARVEST_BOOST));
    }

    @Test
    void immunityRemovesNegativeEffects() {
        GreenhouseData.Bed bed = bed(Map.of(GreenhouseData.slot(1, 1), "WHEAT", GreenhouseData.slot(0, 1), "CACTUS",
                GreenhouseData.slot(2, 1), "POTATO"));
        List<GreenhouseEffect> effects = GreenhouseRules.appliedEffects(bed, config).get(GreenhouseData.slot(1, 1));
        assertFalse(effects.contains(GreenhouseEffect.HARVEST_LOSS));
        assertTrue(effects.contains(GreenhouseEffect.IMMUNITY));
    }

    @Test
    void effectSpreadPassesEffectsOn() {
        GreenhouseData.Bed bed = bed(Map.of(GreenhouseData.slot(1, 1), "WILD_ROSE", GreenhouseData.slot(0, 1), "WHEAT",
                GreenhouseData.slot(2, 1), "CARROT"));
        List<GreenhouseEffect> carrot = GreenhouseRules.appliedEffects(bed, config).get(GreenhouseData.slot(2, 1));
        assertTrue(carrot.contains(GreenhouseEffect.HARVEST_BOOST));
    }

    @Test
    void harvestMathUsesFortuneAndMultipliers() {
        PlantType wheat = config.plant("WHEAT");
        assertNotNull(wheat);
        assertEquals(72, GreenhouseRules.harvestAmount(wheat, 0, 1));
        assertEquals(72 * 11, GreenhouseRules.harvestAmount(wheat, 1000, 1));
        assertEquals(Math.round(72 * 1.2), GreenhouseRules.harvestAmount(wheat, 0, GreenhouseRules.yieldMultiplier(List.of(GreenhouseEffect.HARVEST_BOOST), 0, 0)));
        assertEquals(0.8, GreenhouseRules.xpMultiplier(List.of(GreenhouseEffect.XP_LOSS)), 1e-9);
        assertEquals(0, GreenhouseRules.waterLossFactor(List.of(GreenhouseEffect.IMPROVED_WATER_RETAIN)), 1e-9);
    }

    @Test
    void thirstyPlantsOnlySometimesAdvance() {
        Random random = new Random(7);
        int advanced = 0;
        for (int i = 0; i < 1000; i++) if (GreenhouseRules.advances(config, -10, random)) advanced++;
        assertTrue(advanced > 400 && advanced < 600);
        assertTrue(GreenhouseRules.advances(config, 5, random));
    }

    @Test
    void presetLoadsWithTheBedAtTheCentre() {
        GreenhousePreset preset = GreenhousePreset.get();
        assertEquals(96, preset.size());
        assertEquals("minecraft:farmland", preset.blockAt(GreenhouseLayout.BED_X + 3, GreenhouseLayout.BED_Z + 3, GreenhouseLayout.FLOOR_Y).name());
    }

    @Test
    void dataSurvivesJson() {
        GreenhouseData data = new GreenhouseData();
        GreenhouseData.Bed bed = new GreenhouseData.Bed();
        bed.getUnlocked()[5] = true;
        GreenhouseData.Plant plant = new GreenhouseData.Plant("WHEAT", 1000);
        plant.setStage(3);
        plant.setWater(12.5);
        bed.getPlants().put(5, plant);
        data.getBeds().put(24, bed);
        data.getTanks().put("HYDRO_CAN_1000", 100.0);
        data.setGrowthSpeedTier(2);
        GreenhouseData read = GreenhouseData.fromJson(data.toJson());
        assertEquals(3, read.bed(24).getPlants().get(5).getStage());
        assertTrue(read.bed(24).getUnlocked()[5]);
        assertEquals(2, read.getGrowthSpeedTier());
        assertEquals(100.0, read.getTanks().get("HYDRO_CAN_1000"));
    }
}
