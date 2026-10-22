package dev.daweapon.type.skyblockgeneric.moonglade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoralFishFamilyTest {
    @Test
    void acceptsFamilyAndRefusesLobbyAndCarnivalFish() {
        assertTrue(CoralFishFamily.accepts("SWAMP_THE_FISH"));
        assertTrue(CoralFishFamily.accepts("FISH_THE_FISH"));
        assertFalse(CoralFishFamily.accepts("CHILL_THE_FISH"));
        assertFalse(CoralFishFamily.accepts("EGG_THE_FISH"));
        assertFalse(CoralFishFamily.accepts("OOPS_THE_FISH"));
        assertFalse(CoralFishFamily.accepts("SPOOK_THE_FISH"));
        assertFalse(CoralFishFamily.accepts("SINGING_FISH"));
        assertFalse(CoralFishFamily.accepts("RAW_FISH"));
    }

    @Test
    void fishingXpFollowsTheWikiTable() {
        assertEquals(0, CoralFishFamily.fishingXp(0));
        assertEquals(10_000, CoralFishFamily.fishingXp(1));
        assertEquals(10_000, CoralFishFamily.fishingXp(5));
        assertEquals(15_000, CoralFishFamily.fishingXp(6));
        assertEquals(20_000, CoralFishFamily.fishingXp(21));
        assertEquals(30_000, CoralFishFamily.fishingXp(22));
        assertEquals(40_000, CoralFishFamily.fishingXp(34));
        assertEquals(0, CoralFishFamily.fishingXp(35));
    }
}
