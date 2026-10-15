package dev.daweapon.type.skyblockgeneric.garden.pest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PestRulesTest {
    @Test
    void fortuneLossFollowsTheWikiTable() {
        assertEquals(0, PestRules.fortuneLossPercent(3, 0));
        assertEquals(5, PestRules.fortuneLossPercent(4, 0));
        assertEquals(0, PestRules.fortuneLossPercent(4, 100));
        assertEquals(30, PestRules.fortuneLossPercent(6, 0));
        assertEquals(15, PestRules.fortuneLossPercent(7, 200));
        assertEquals(75, PestRules.fortuneLossPercent(8, 99));
        assertEquals(0, PestRules.fortuneLossPercent(8, 500));
    }

    @Test
    void bonusChanceAddsPests() {
        assertEquals(1, PestRules.pestsPerSpawn(0, 0));
        assertEquals(2, PestRules.pestsPerSpawn(100, 0.99));
        assertEquals(3, PestRules.pestsPerSpawn(150, 0.1));
        assertEquals(2, PestRules.pestsPerSpawn(150, 0.9));
    }

    @Test
    void cooldownScalesWithRepellentAndGear() {
        assertEquals(300_000, PestRules.cooldownMillis(false, false, 0));
        assertEquals(600_000, PestRules.cooldownMillis(true, false, 0));
        assertEquals(1_200_000, PestRules.cooldownMillis(false, true, 0));
        assertEquals(270_000, PestRules.cooldownMillis(false, false, 10));
        assertEquals(130_000, PestRules.cooldownMillis(false, false, 90));
        assertEquals(70_000, PestRules.cooldownMillis(false, false, 90, true));
    }

    @Test
    void catalogueLoadsEveryPest() {
        PestCatalogue catalogue = PestCatalogue.get();
        assertEquals(13, catalogue.natural().size());
        assertNotNull(catalogue.pest("FIELD_MOUSE"));
        assertEquals(49, PestRewards.guaranteedAmount(catalogue.pest("FLY"), 2520));
        assertEquals(143, PestRewards.guaranteedAmount(catalogue.pest("BEETLE"), 2520));
        assertTrue(catalogue.natural().stream().allMatch(pest -> pest.level() >= 5));
    }
}
