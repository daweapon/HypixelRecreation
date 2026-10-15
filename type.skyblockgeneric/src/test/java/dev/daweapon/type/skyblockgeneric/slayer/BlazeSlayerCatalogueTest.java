package dev.daweapon.type.skyblockgeneric.slayer;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlazeSlayerCatalogueTest {
    private static SlayerDefinition blaze;

    @BeforeAll
    static void load() {
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
        blaze = SlayerRegistry.get(SlayerType.INFERNO_DEMONLORD);
    }

    @Test
    void tiersMatchTheWiki() {
        assertNotNull(blaze);
        assertEquals(2_500_000, blaze.tiers().get(SlayerTier.I).bossHealth());
        assertEquals(10_000_000, blaze.tiers().get(SlayerTier.II).bossHealth());
        assertEquals(45_000_000, blaze.tiers().get(SlayerTier.III).bossHealth());
        assertEquals(150_000_000, blaze.tiers().get(SlayerTier.IV).bossHealth());
        assertEquals(36_000, blaze.tiers().get(SlayerTier.IV).requiredCombatXp());
    }

    @Test
    void onlyBlazeFamilyMobsCount() {
        assertTrue(blaze.targetMobIds().contains("BLAZE"));
        assertTrue(blaze.targetMobIds().contains("MILLENNIA_AGED_BLAZE"));
        assertFalse(blaze.targetMobIds().contains("FLARE"));
        assertTrue(blaze.targetMobTypes().isEmpty());
    }

    @Test
    void everyAvailableDropIsARealItem() {
        assertEquals(25, blaze.drops().size());
        for (SlayerDrop drop : blaze.drops()) {
            if (drop.unavailableReason() == null) assertNotNull(drop.item(), drop.key());
            assertFalse(drop.tiers().isEmpty(), drop.key());
        }
        SlayerDrop dye = blaze.drops().stream().filter(drop -> drop.key().equals("DYE_FLAME")).findFirst().orElseThrow();
        assertEquals(75_000_000L, dye.rngMeterXp());
    }

    @Test
    void levelsCarryTheirRewards() {
        assertEquals(9, blaze.levels().size());
        assertEquals(1_000_000, blaze.maxXp());
        assertEquals(24, blaze.levels().stream().mapToInt(level -> level.unlocks().size()).sum());
        int skyBlockXp = blaze.levels().stream().mapToInt(SlayerLevelReward::skyBlockXp).sum();
        assertEquals(725, skyBlockXp);
        assertEquals(3, blaze.minibosses().size());
    }

    @Test
    void rngMeterFillsAndResets() {
        SlayerDrop powder = blaze.drops().stream()
            .filter(drop -> drop.key().equals("ENCHANTED_BLAZE_POWDER")).findFirst().orElseThrow();
        DatapointSlayer.SlayerProgress progress = new DatapointSlayer.SlayerProgress();
        assertNull(SlayerRngMeter.selected(blaze, progress));
        progress.setRngItem(powder.key());
        SlayerRngMeter.addXp(progress, SlayerTier.II, 500);
        assertEquals(0, progress.getRngXp());
        SlayerRngMeter.addXp(progress, SlayerTier.IV, 500);
        assertEquals(500, progress.getRngXp());
        assertEquals(1 + 2D * 500 / 2351, SlayerRngMeter.weightMultiplier(progress, powder), 1e-9);
        progress.setRngXp(3_000);
        assertTrue(SlayerRngMeter.full(progress, powder));
        SlayerRngMeter.onObtained(progress, powder);
        assertEquals(3_000 - 2_351, progress.getRngXp());
        SlayerRngMeter.onObtained(progress, powder);
        assertEquals(0, progress.getRngXp());
    }
}
