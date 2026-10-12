package net.swofty.type.skyblockgeneric.slayer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimordialEyeDropTest {
    @BeforeAll
    static void load() {
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
    }

    @Test
    void theEyeDropsOnlyFromTierFiveTarantulaAfterSlayerSeven() {
        SlayerDrop eye = SlayerRegistry.get(SlayerType.TARANTULA_BROODFATHER).drops().stream()
                .filter(drop -> drop.requiredLevel() == 7 && drop.dropsAt(SlayerTier.V) && drop.tiers().size() == 1)
                .findFirst().orElseThrow();
        assertEquals(SlayerDrop.Pool.MAIN, eye.pool());
        assertEquals(0.014, eye.odds(SlayerTier.V).chancePercent(), 1e-9);
        assertFalse(eye.dropsAt(SlayerTier.IV));
        assertTrue(eye.odds(SlayerTier.V).minAmount() == 1 && eye.odds(SlayerTier.V).maxAmount() == 1);
    }
}
