package net.swofty.type.skyblockgeneric.mythological.gear;

import net.swofty.type.skyblockgeneric.enchantment.debuff.VenomousDebuff;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MythologicalGearTest {

    @Test
    void unearthedScalesFromTwoToEightPieces() {
        assertEquals(0, MythologicalGear.unearthedReduction(1));
        assertEquals(10, MythologicalGear.unearthedReduction(2));
        assertEquals(17.5, MythologicalGear.unearthedReduction(5));
        assertEquals(25, MythologicalGear.unearthedReduction(8));
    }

    @Test
    void familiarityAndGreedScaleFromTwoToFourPieces() {
        assertEquals(0, MythologicalGear.familiarityMagicFind(1));
        assertEquals(10, MythologicalGear.familiarityMagicFind(2));
        assertEquals(20, MythologicalGear.familiarityMagicFind(4));
        assertEquals(15, MythologicalGear.greedPercent(3));
    }

    @Test
    void venomousGoesUpToSevenWithTheWikiValues() {
        assertEquals(7, VenomousDebuff.SPEED_REDUCTION_PERCENTAGES.length);
        assertEquals(0.20, VenomousDebuff.SPEED_REDUCTION_PERCENTAGES[6], 1e-9);
        assertEquals(2.0, VenomousDebuff.DAMAGE_PERCENT_PER_SECOND[6], 1e-9);
    }
}
