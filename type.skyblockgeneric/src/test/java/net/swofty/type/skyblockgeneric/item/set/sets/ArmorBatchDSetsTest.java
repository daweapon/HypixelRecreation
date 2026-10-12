package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArmorBatchDSetsTest {
    @Test
    void glaciteGrantsTwoMiningSpeedPerMiningLevel() {
        assertEquals(0, GlaciteArmorSet.miningSpeed(0));
        assertEquals(120, GlaciteArmorSet.miningSpeed(60));
    }

    @Test
    void wiseDragonChargesTwoThirdsOfTheManaCost() {
        assertEquals(2, Math.round(3 * WiseDragonArmorSet.MANA_COST_MULTIPLIER));
        assertEquals(100, Math.round(150 * WiseDragonArmorSet.MANA_COST_MULTIPLIER));
    }

    @Test
    void golemGrantsAbsorptionThreeForTwentySeconds() {
        assertEquals(2, GolemArmorSet.ABSORPTION_AMPLIFIER);
        assertEquals(400, GolemArmorSet.ABSORPTION_TICKS);
    }

    @Test
    void setsAreRegisteredWithAllFourPieces() {
        for (Class<?> set : new Class<?>[]{GlaciteArmorSet.class, WiseDragonArmorSet.class, GolemArmorSet.class}) {
            ArmorSetRegistry registry = ArmorSetRegistry.getArmorSet(set.asSubclass(net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet.class));
            assertNotNull(registry);
            assertEquals(4, ArmorSetRegistry.getPieceCount(registry));
        }
    }
}
