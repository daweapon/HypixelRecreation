package dev.daweapon.type.skyblockgeneric.greatspook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpookNpcIngredientsTest {
    @Test
    void anySoupIdIsAccepted() {
        assertTrue(SpookNpcIngredients.isSoup("MUSHROOM_SOUP"));
        assertTrue(SpookNpcIngredients.isSoup("MARSH_SPORE_SOUP"));
        assertTrue(SpookNpcIngredients.isSoup("MAGIC_MUSHROOM_SOUP"));
        assertFalse(SpookNpcIngredients.isSoup("SOUP_BOWL"));
        assertFalse(SpookNpcIngredients.isSoup(""));
        assertFalse(SpookNpcIngredients.isSoup(null));
    }

    @Test
    void siriusNeedsTheExactPurse() {
        assertTrue(SpookNpcIngredients.exactSiriusPurse(7_871_265));
        assertFalse(SpookNpcIngredients.exactSiriusPurse(7_871_264));
        assertFalse(SpookNpcIngredients.exactSiriusPurse(7_871_265.5));
    }

    @Test
    void gratitudeNeedsAnAcceptWithinTwoSeconds() {
        assertTrue(SpookNpcIngredients.acceptedQuickly(2_000, 0));
        assertFalse(SpookNpcIngredients.acceptedQuickly(2_001, 0));
    }

    @Test
    void cageBoxMatchesTheCapturedBars() {
        assertTrue(SpookNpcIngredients.inCage(-277, 116, -185));
        assertTrue(SpookNpcIngredients.inCage(-274, 115, -183));
        assertFalse(SpookNpcIngredients.inCage(-278, 116, -185));
        assertFalse(SpookNpcIngredients.inCage(-275, 118, -184));
    }

    @Test
    void freeSpiderCooldownIsFiveSeconds() {
        assertEquals(5_000, SpookNpcIngredients.cooldownLeft(1_000, 1_000));
        assertEquals(0, SpookNpcIngredients.cooldownLeft(6_000, 1_000));
    }

    @Test
    void hammerPairWorksInEitherOrder() {
        assertTrue(ScavengedIronHammer.isPair("DWARVEN_GOLD_HAMMER", "DWARVEN_EMERALD_HAMMER"));
        assertTrue(ScavengedIronHammer.isPair("DWARVEN_EMERALD_HAMMER", "DWARVEN_GOLD_HAMMER"));
        assertFalse(ScavengedIronHammer.isPair("DWARVEN_GOLD_HAMMER", "DWARVEN_GOLD_HAMMER"));
    }
}
