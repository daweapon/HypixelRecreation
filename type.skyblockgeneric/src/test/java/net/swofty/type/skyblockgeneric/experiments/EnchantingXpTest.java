package net.swofty.type.skyblockgeneric.experiments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantingXpTest {
    @Test
    void thirtyLevelsGive575Xp() {
        assertEquals(575.1, EnchantingXp.forLevelsSpent(30), 0.1);
    }

    @Test
    void dailyCapLimitsManualXp() {
        assertEquals(100, EnchantingXp.withinDailyCap(575, 499_900));
        assertEquals(0, EnchantingXp.withinDailyCap(575, 500_000));
    }

    @Test
    void grandBottleAtEnchanting20Gives3000Orbs() {
        assertEquals(3_000, ExperienceBottle.GRAND.orbsFor(20));
        assertEquals(6_000, ExperienceBottle.GRAND.orbsFor(60));
        assertEquals(32, ExperienceBottle.EXPERIENCE_BOTTLE.orbsFor(60));
    }

    @Test
    void vanillaPointsRoundTrip() {
        for (int level : new int[]{0, 5, 14, 15, 29, 30, 100}) {
            long points = VanillaExperience.totalPoints(level, 0f);
            assertEquals(level, VanillaExperience.levelForPoints(points));
        }
    }
}
