package dev.daweapon.type.skyblockgeneric.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyBlockPlayerExperienceTest {

    @Test
    void everyTotalLandsInsideItsLevel() {
        for (long total = 0; total < 200_000; total++) {
            int level = SkyBlockPlayer.experienceLevel(total);
            assertTrue(SkyBlockPlayer.experienceAtLevel(level) <= total, "total " + total);
            assertTrue(SkyBlockPlayer.experienceAtLevel(level + 1) > total, "total " + total);
        }
    }

    @Test
    void vanillaLevelBoundaries() {
        assertEquals(0, SkyBlockPlayer.experienceLevel(6));
        assertEquals(1, SkyBlockPlayer.experienceLevel(7));
        assertEquals(16, SkyBlockPlayer.experienceLevel(393));
        assertEquals(17, SkyBlockPlayer.experienceLevel(394));
        assertEquals(30, SkyBlockPlayer.experienceLevel(1506));
        assertEquals(31, SkyBlockPlayer.experienceLevel(1507));
        assertEquals(32, SkyBlockPlayer.experienceLevel(1628));
    }
}
