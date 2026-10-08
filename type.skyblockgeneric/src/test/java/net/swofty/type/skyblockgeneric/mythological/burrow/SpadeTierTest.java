package net.swofty.type.skyblockgeneric.mythological.burrow;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpadeTierTest {

    @Test
    void chainLengthsMatchTheWiki() {
        assertEquals(4, SpadeTier.ANCESTRAL.getChainLength());
        assertEquals(6, SpadeTier.ARCHAIC.getChainLength());
        assertEquals(8, SpadeTier.DEIFIC.getChainLength());
        assertEquals(10, SpadeTier.DEIFIC.getChainLength() + SpadeTier.ERUDITE_BONUS_LENGTH);
    }

    @Test
    void treasureTablesHaveTheWikiTotalWeights() {
        assertEquals(96, total(SpadeTier.ANCESTRAL));
        assertEquals(592, total(SpadeTier.ARCHAIC));
        assertEquals(3_221, total(SpadeTier.DEIFIC));
    }

    @Test
    void onlyHigherSpadesDigUpMythosFragmentsAndBraidedFeathers() {
        assertTrue(SpadeTier.ANCESTRAL.getTreasures().stream().noneMatch(t -> t.item() == ItemType.MYTHOS_FRAGMENT));
        assertTrue(SpadeTier.ARCHAIC.getTreasures().stream().anyMatch(t -> t.item() == ItemType.MYTHOS_FRAGMENT));
        assertTrue(SpadeTier.DEIFIC.getTreasures().stream().anyMatch(t -> t.item() == ItemType.BRAIDED_GRIFFIN_FEATHER));
    }

    @Test
    void rollingAlwaysReturnsATreasure() {
        Random random = new Random(7);
        for (int i = 0; i < 10_000; i++) assertNotNull(SpadeTier.DEIFIC.roll(null, random));
    }

    private static int total(SpadeTier tier) {
        return tier.getTreasures().stream().mapToInt(BurrowTreasure::weight).sum();
    }
}
