package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VialOfVenomTest {
    @Test
    void eachVialGivesOneCritDamageUpToFive() {
        assertEquals(0, VialOfVenom.criticalDamage(0), 1e-9);
        assertEquals(3, VialOfVenom.criticalDamage(3), 1e-9);
        assertEquals(5, VialOfVenom.criticalDamage(5), 1e-9);
        assertEquals(5, VialOfVenom.criticalDamage(99), 1e-9);
        assertEquals(0, VialOfVenom.criticalDamage(-2), 1e-9);
    }

    @Test
    void statisticsCarryTheCritDamage() {
        assertEquals(4, VialOfVenom.statistics(4).getOverall(ItemStatistic.CRITICAL_DAMAGE), 1e-9);
        assertEquals(0, VialOfVenom.statistics(0).getOverall(ItemStatistic.CRITICAL_DAMAGE), 1e-9);
    }

    @Test
    void experienceMatchesTheXpSourceCap() {
        assertEquals(2, VialOfVenom.experience(1), 1e-9);
        assertEquals(SkyBlockXPSource.VIAL_OF_VENOM.getMaxXP(), VialOfVenom.experience(VialOfVenom.LIMIT), 1e-9);
        assertEquals(SkyBlockXPSource.VIAL_OF_VENOM.getMaxXP(), VialOfVenom.experience(50), 1e-9);
    }

    @Test
    void everyVialHasItsOwnMessageAndTheSixthIsRefused() {
        for (int before = 0; before < VialOfVenom.LIMIT; before++) {
            assertTrue(VialOfVenom.messageFor(before).contains("!"), "vial " + before);
        }
        assertTrue(VialOfVenom.messageFor(0).contains("YIPPEE"));
        assertTrue(VialOfVenom.messageFor(4).contains("NOPE"));
        assertEquals(VialOfVenom.MAXED_MESSAGE, VialOfVenom.messageFor(5));
    }
}
