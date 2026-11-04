package dev.daweapon.type.skyblockgeneric.slayer;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlayerShardDropsTest {
    @BeforeAll
    static void load() {
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
    }

    private static SlayerDrop drop(SlayerType type, String item) {
        return SlayerRegistry.get(type).drops().stream()
                .filter(drop -> drop.item() != null && drop.item().name().equals(item)).findFirst().orElse(null);
    }

    @Test
    void paragonDropsFromTierFourAndFiveOfEverySlayerExceptTheBloodfiend() {
        for (SlayerType type : SlayerType.values()) {
            SlayerDrop paragon = drop(type, "SHARD_PARAGON");
            if (type == SlayerType.RIFTSTALKER_BLOODFIEND) {
                assertEquals(null, paragon);
                continue;
            }
            assertNotNull(paragon, type.name());
            assertEquals(SlayerDrop.Pool.SEPARATE, paragon.pool());
            assertEquals(5, paragon.requiredLevel());
            assertEquals(0.25, paragon.odds(SlayerTier.IV).chancePercent(), 1e-9);
            if (type != SlayerType.VOIDGLOOM_SERAPH) assertEquals(0.5, paragon.odds(SlayerTier.V).chancePercent(), 1e-9);
            assertFalse(paragon.dropsAt(SlayerTier.III));
            assertFalse(paragon.hasRngMeter());
            assertTrue(paragon.available());
        }
    }

    @Test
    void primordialDropsOnlyFromTierFiveTarantula() {
        SlayerDrop primordial = drop(SlayerType.TARANTULA_BROODFATHER, "SHARD_PRIMORDIAL");
        assertNotNull(primordial);
        assertEquals(0.71, primordial.odds(SlayerTier.V).chancePercent(), 1e-9);
        assertFalse(primordial.dropsAt(SlayerTier.IV));
        assertEquals(null, drop(SlayerType.SVEN_PACKMASTER, "SHARD_PRIMORDIAL"));
        assertNotNull(ItemType.get("SHARD_PRIMORDIAL"));
    }
}
