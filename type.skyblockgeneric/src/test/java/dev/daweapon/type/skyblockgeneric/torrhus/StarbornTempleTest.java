package dev.daweapon.type.skyblockgeneric.torrhus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarbornTempleTest {
    private static final StarbornTemple DESERT = StarbornTemple.DESERT;

    @Test
    void willFollowsTheWikiTiers() {
        assertEquals(0, DESERT.will(0), 1e-9);
        assertEquals(0.2, DESERT.will(1), 1e-9);
        assertEquals(0.4, DESERT.will(10), 1e-9);
        assertEquals(1.0, DESERT.will(100), 1e-9);
        assertEquals(2.0, DESERT.will(5_000), 1e-9);
        assertEquals(3.4, DESERT.will(1_000_000), 1e-9);
        assertEquals(3.4, DESERT.will(5_000_000), 1e-9);
    }

    @Test
    void willInterpolatesBetweenTiers() {
        assertEquals(0.2 + 4D / 9 * 0.2, DESERT.will(5), 1e-9);
        assertEquals(2.14, DESERT.will(8_411), 0.02);
    }

    @Test
    void donationCostsMatchTheWiki() {
        assertEquals(70_000, StarbornTemple.coinCost(1));
        assertEquals(350_000, StarbornTemple.coinCost(5));
        assertEquals(700_000, StarbornTemple.coinCost(10));
        assertEquals(1_400_000, StarbornTemple.coinCost(20));
        assertEquals(2, StarbornTemple.itemCost(1));
        assertEquals(10, StarbornTemple.itemCost(5));
        assertEquals(20, StarbornTemple.itemCost(10));
        assertEquals(40, StarbornTemple.itemCost(20));
    }

    @Test
    void donationsStackOntoRemainingTime() {
        long now = 1_000_000;
        assertEquals(now + StarbornTemple.DAY_MILLIS, StarbornTemple.extend(now, 0, 1));
        assertEquals(now + 6 * StarbornTemple.DAY_MILLIS, StarbornTemple.extend(now, now + StarbornTemple.DAY_MILLIS, 5));
        assertEquals(now + 5 * StarbornTemple.DAY_MILLIS, StarbornTemple.extend(now, now - 10, 5));
        assertTrue(StarbornTemple.benefactorActive(now + 1, now));
        assertFalse(StarbornTemple.benefactorActive(now, now));
    }
}
