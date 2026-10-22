package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeaconRulesTest {
    @Test
    void untunedBeaconGivesFiveStacksOfFiveFortune() {
        assertEquals(5, BeaconRules.stackLimit(0));
        assertEquals(5, BeaconRules.fortunePerStack(0));
        assertEquals(25, BeaconRules.fortune(5, 0, 0));
        assertEquals(25, BeaconRules.fortune(9, 0, 0));
    }

    @Test
    void everyOtherEnhancerRaisesTheStackLimit() {
        assertEquals(5, BeaconRules.stackLimit(1));
        assertEquals(6, BeaconRules.stackLimit(2));
        assertEquals(7, BeaconRules.stackLimit(4));
        assertEquals(10, BeaconRules.stackLimit(10));
        assertEquals(10, BeaconRules.stackLimit(99));
    }

    @Test
    void maxedBeaconWithBeaconZealotReachesTwoHundred() {
        assertEquals(10, BeaconRules.fortunePerStack(10));
        assertEquals(100, BeaconRules.fortune(10, 10, 0));
        assertEquals(200, BeaconRules.fortune(20, 10, 100));
    }

    @Test
    void zealotMultipliesTheRawBonus() {
        assertEquals(27.5, BeaconRules.fortune(5, 0, 10), 1e-9);
        assertEquals(0, BeaconRules.fortune(0, 5, 100));
    }

    @Test
    void beaconmiteChanceGrowsWithSignalStrength() {
        assertEquals(10, BeaconRules.beaconmitePercent(0));
        assertEquals(20, BeaconRules.beaconmitePercent(10));
        assertEquals(20, BeaconRules.beaconmitePercent(40));
    }

    @Test
    void formatsWholeAndHalfValues() {
        assertEquals("25", BeaconRules.format(25));
        assertEquals("5.5", BeaconRules.format(5.5));
        assertEquals(1, BeaconRules.panels(false));
        assertEquals(2, BeaconRules.panels(true));
    }
}
