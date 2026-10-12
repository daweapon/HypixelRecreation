package net.swofty.type.skyblockgeneric.item.armor;

import net.swofty.type.skyblockgeneric.item.set.sets.StrongDragonSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorAbilitiesTest {
    @Test
    void frozenBlazeCapGrowsPerFiveThousandRodsUpToFiveThousand() {
        assertEquals(500, ArmorAbilities.frozenBlazeCap(0), 1e-9);
        assertEquals(500, ArmorAbilities.frozenBlazeCap(4_999), 1e-9);
        assertEquals(600, ArmorAbilities.frozenBlazeCap(5_000), 1e-9);
        assertEquals(5_000, ArmorAbilities.frozenBlazeCap(225_000), 1e-9);
        assertEquals(5_000, ArmorAbilities.frozenBlazeCap(10_000_000), 1e-9);
    }

    @Test
    void frozenBlazeDamageIsBasePlusPercentOfMaxHealthUnderTheCap() {
        assertEquals(330, ArmorAbilities.frozenBlazeDamage(1_000, 0), 1e-9);
        assertEquals(500, ArmorAbilities.frozenBlazeDamage(100_000, 0), 1e-9);
        assertEquals(600, ArmorAbilities.frozenBlazeDamage(100_000, 5_000), 1e-9);
    }

    @Test
    void mistAuraNeedsTwoSorrowPiecesAndAGhost() {
        assertEquals(60, ArmorAbilities.mistAura(100, true, 2), 1e-9);
        assertEquals(100, ArmorAbilities.mistAura(100, true, 1), 1e-9);
        assertEquals(100, ArmorAbilities.mistAura(100, false, 4), 1e-9);
    }

    @Test
    void reaperDoublesUndeadDamageAndNearlyNullifiesTheRest() {
        assertEquals(200, ArmorAbilities.reaperDealt(100, true), 1e-9);
        assertEquals(1, ArmorAbilities.reaperDealt(100, false), 1e-9);
    }

    @Test
    void extraDefenseScalesDamageByTheDefenseFormula() {
        assertEquals(50, ArmorAbilities.extraDefenseTaken(100, 0, 100), 1e-9);
        assertEquals(100.0 * 300 / 400, ArmorAbilities.extraDefenseTaken(100, 200, 100), 1e-9);
        assertEquals(100, ArmorAbilities.extraDefenseTaken(100, 50, 0), 1e-9);
    }

    @Test
    void chickenHeadTrimsFiveFallPercent() {
        assertEquals(19, ArmorAbilities.fallDamage(20), 1e-9);
    }

    @Test
    void spikyDealsTenPlusTwentyPercentOfStrength() {
        assertEquals(10, ArmorAbilities.spikyDamage(0), 1e-9);
        assertEquals(110, ArmorAbilities.spikyDamage(500), 1e-9);
    }

    @Test
    void packBonusCountsAtMostThreePlayers() {
        assertEquals(0, ArmorAbilities.packMembers(-1));
        assertEquals(2, ArmorAbilities.packMembers(2));
        assertEquals(3, ArmorAbilities.packMembers(7));
    }

    @Test
    void strongBloodAddsSeventyFiveDamageOnlyWithTheAspectOfTheEnd() {
        assertEquals(75, StrongDragonSet.aoteDamage(true), 1e-9);
        assertEquals(0, StrongDragonSet.aoteDamage(false), 1e-9);
    }

    @Test
    void negativeEffectsAreRecognised() {
        assertTrue(ArmorAbilities.isNegative("slowness"));
        assertTrue(ArmorAbilities.isNegative("poison"));
        assertFalse(ArmorAbilities.isNegative("speed"));
    }
}
