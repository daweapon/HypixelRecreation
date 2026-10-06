package net.swofty.type.skyblockgeneric.entity.mob.island;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateIslandMobSpawnerTest {

    @Test
    void mobLevelsFollowTheWikiCombatLadder() {
        int[][] documented = {{0, 1}, {4, 1}, {5, 2}, {7, 2}, {8, 3}, {9, 3}, {10, 4}, {12, 5}, {14, 6},
                {16, 7}, {18, 8}, {19, 8}, {20, 9}, {32, 15}, {60, 15}};
        for (int[] pair : documented) {
            assertEquals(pair[1], IslandMobSpecies.levelForCombat(pair[0]), "Combat " + pair[0]);
        }
    }

    @Test
    void undocumentedCombatLevelsStayBetweenTheDocumentedEnds() {
        int previous = IslandMobSpecies.levelForCombat(20);
        for (int combat = 21; combat <= 32; combat++) {
            int level = IslandMobSpecies.levelForCombat(combat);
            assertTrue(level >= previous && level <= 15, "Combat " + combat + " gave " + level);
            previous = level;
        }
    }

    @Test
    void creepersNeverExceedLevelOne() {
        assertEquals(1, IslandMobSpecies.CREEPER.clamp(9));
        assertEquals(80, IslandMobSpecies.CREEPER.healthAt(9));
    }

    @Test
    void nightIsTheVanillaDarkWindow() {
        assertFalse(PrivateIslandMobSpawner.isNight(6000));
        assertTrue(PrivateIslandMobSpawner.isNight(13000));
        assertTrue(PrivateIslandMobSpawner.isNight(18000 + 24000L * 5));
        assertFalse(PrivateIslandMobSpawner.isNight(23000));
    }
}
