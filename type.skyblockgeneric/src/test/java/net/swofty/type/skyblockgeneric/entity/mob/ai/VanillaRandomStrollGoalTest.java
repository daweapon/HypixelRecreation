package net.swofty.type.skyblockgeneric.entity.mob.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaRandomStrollGoalTest {
    @Test
    void onlyAFewStrollsPathInOneTick() {
        long tick = 1_000_000_000L;
        for (int i = 0; i < VanillaRandomStrollGoal.STROLLS_PER_TICK; i++) {
            assertTrue(VanillaRandomStrollGoal.takeBudget(tick));
        }
        assertFalse(VanillaRandomStrollGoal.takeBudget(tick + 10));
        assertTrue(VanillaRandomStrollGoal.takeBudget(tick + 50));
    }

    @Test
    void aStrollSearchesAFewHundredNodesAtMost() {
        assertTrue(VanillaRandomStrollGoal.nodeBudget(6) <= 200);
        assertTrue(VanillaRandomStrollGoal.ATTEMPTS * VanillaRandomStrollGoal.nodeBudget(6) <= 600);
    }
}
