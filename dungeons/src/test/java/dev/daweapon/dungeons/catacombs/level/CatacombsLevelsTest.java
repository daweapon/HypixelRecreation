package dev.daweapon.dungeons.catacombs.level;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatacombsLevelsTest {
    @Test
    void cumulativeSpotValues() {
        assertEquals(0, CatacombsLevels.xpForLevel(0));
        assertEquals(50, CatacombsLevels.xpForLevel(1));
        assertEquals(625, CatacombsLevels.xpForLevel(5));
        assertEquals(4_385, CatacombsLevels.xpForLevel(10));
        assertEquals(135_640, CatacombsLevels.xpForLevel(20));
        assertEquals(668_640, CatacombsLevels.xpForLevel(25));
        assertEquals(3_084_640, CatacombsLevels.xpForLevel(30));
        assertEquals(51_559_640, CatacombsLevels.xpForLevel(40));
        assertEquals(569_809_640L, CatacombsLevels.xpForLevel(50));
    }

    @Test
    void perLevelValues() {
        assertEquals(50, CatacombsLevels.xpToReachLevel(1));
        assertEquals(230, CatacombsLevels.xpToReachLevel(5));
        assertEquals(116_250_000, CatacombsLevels.xpToReachLevel(50));
    }

    @Test
    void levelForXpBoundaries() {
        assertEquals(0, CatacombsLevels.levelForXp(0));
        assertEquals(0, CatacombsLevels.levelForXp(49));
        assertEquals(1, CatacombsLevels.levelForXp(50));
        assertEquals(4, CatacombsLevels.levelForXp(624));
        assertEquals(5, CatacombsLevels.levelForXp(625));
        assertEquals(49, CatacombsLevels.levelForXp(569_809_639L));
        assertEquals(50, CatacombsLevels.levelForXp(569_809_640L));
        assertEquals(50, CatacombsLevels.levelForXp(Long.MAX_VALUE));
    }

    @Test
    void healthBonusIsTwoPerLevelCappedAtFifty() {
        assertEquals(0, CatacombsLevels.healthBonus(0));
        assertEquals(20, CatacombsLevels.healthBonus(10));
        assertEquals(100, CatacombsLevels.healthBonus(50));
        assertEquals(100, CatacombsLevels.healthBonus(60));
    }

    @Test
    void outOfRangeLevelsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> CatacombsLevels.xpForLevel(51));
        assertThrows(IllegalArgumentException.class, () -> CatacombsLevels.xpToReachLevel(0));
    }
}
