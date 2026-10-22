package dev.daweapon.type.skyblockgeneric.hotf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotfBeekeeperTest {
    @Test
    void fasterEffectsRemoveTwentyPercentOfTheWait() {
        assertEquals(0.8, HotfBeekeeper.speedFactor(true), 1e-9);
        assertEquals(1.0, HotfBeekeeper.speedFactor(false), 1e-9);
    }

    @Test
    void wikiListsFourEqualWeightEffects() {
        assertEquals(4, HotfBeekeeper.values().length);
        assertEquals(25, HotfBeekeeper.SECOND_CRITTER_PERCENT, 1e-9);
        assertEquals(2, HotfBeekeeper.EXTRA_HONEYCOMB_AMOUNT);
    }
}
