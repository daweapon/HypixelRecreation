package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectionPerksTest {

    @Test
    void boostedCountsKeepTheExpectedValue() {
        assertEquals(13, ElectionPerks.scaleCount(10, 1.3D, 0.99D));
        assertEquals(1, ElectionPerks.scaleCount(1, 1.3D, 0.5D));
        assertEquals(2, ElectionPerks.scaleCount(1, 1.3D, 0.1D));
        assertEquals(0, ElectionPerks.scaleCount(0, 1.3D, 0D));
        assertEquals(5, ElectionPerks.scaleCount(5, 1D, 0D));
    }

    @Test
    void astralNegotiatorTakesFifteenPercentOffExperienceLevels() {
        assertEquals(26, ElectionPerks.discountedLevels(30, 15D));
        assertEquals(9, ElectionPerks.discountedLevels(10, 15D));
        assertEquals(30, ElectionPerks.discountedLevels(30, 0D));
        assertEquals(0, ElectionPerks.discountedLevels(0, 15D));
    }

    @Test
    void perksLeftToOtherWorkstreamsStayAvailable() {
        for (Perk perk : new Perk[]{Perk.MYTHOLOGICAL_RITUAL, Perk.CHIVALROUS_CARNIVAL, Perk.FISHING_FESTIVAL, Perk.GOATED,
                Perk.STOCK_EXCHANGE, Perk.VOLUME_TRADING, Perk.MOLTEN_FORGE, Perk.BLOOMING_BUSINESS}) {
            assertTrue(PerkAvailability.isFullyAvailable(perk), perk.name());
        }
    }

    @Test
    void perksWithoutTheirSystemCarryAReason() {
        for (Perk perk : new Perk[]{Perk.MARAUDER, Perk.EZPZ, Perk.BENEDICTION}) {
            assertTrue(PerkAvailability.isMissing(perk), perk.name());
            assertFalse(PerkAvailability.gap(perk).orElseThrow().reason().isBlank(), perk.name());
        }
        assertFalse(PerkAvailability.isMissing(Perk.SWEET_BENEVOLENCE));
        assertFalse(PerkAvailability.isFullyAvailable(Perk.SWEET_BENEVOLENCE));
    }
}
