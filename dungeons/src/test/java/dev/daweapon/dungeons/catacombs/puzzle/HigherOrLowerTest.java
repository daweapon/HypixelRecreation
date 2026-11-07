package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.higherorlower.HigherOrLower;
import dev.daweapon.dungeons.catacombs.puzzle.higherorlower.HigherOrLower.Kill;
import dev.daweapon.dungeons.catacombs.puzzle.higherorlower.HigherOrLower.Order;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HigherOrLowerTest {
    private static final List<Integer> CAPTURED = List.of(1366, 1534, 1776, 2039, 2291, 2672, 2843, 3138, 3543, 3633);
    private static final int[] CAPTURED_CHEST_HEIGHTS = {7, 12, 17, 22, 27, 31, 36, 41, 46, 51};

    @Test
    void theCapturedRunKilledTheBlazesWeakestFirstAndTheChestClimbedToTheTop() {
        HigherOrLower puzzle = new HigherOrLower(CAPTURED, Order.LOWEST_FIRST);
        for (int blaze = 0; blaze < CAPTURED.size(); blaze++) {
            assertEquals(blaze == CAPTURED.size() - 1 ? Kill.LAST : Kill.IN_ORDER, puzzle.kill(blaze));
            assertEquals(CAPTURED_CHEST_HEIGHTS[blaze], 2 + puzzle.chestOffset(49), "after kill " + (blaze + 1));
        }
        assertTrue(puzzle.done());
    }

    @Test
    void killingABlazeOutOfTurnFails() {
        HigherOrLower puzzle = new HigherOrLower(CAPTURED, Order.LOWEST_FIRST);
        assertEquals(Kill.IN_ORDER, puzzle.kill(0));
        assertEquals(Kill.OUT_OF_ORDER, puzzle.kill(5));
        assertTrue(puzzle.failed());
        assertEquals(Kill.IGNORED, puzzle.kill(1));
    }

    @Test
    void aChestAtTheTopMeansStrongestFirst() {
        HigherOrLower puzzle = new HigherOrLower(CAPTURED, Order.HIGHEST_FIRST);
        assertEquals(9, puzzle.next());
        assertEquals(Kill.OUT_OF_ORDER, puzzle.kill(0));
    }

    @Test
    void dealtHealthsAreDistinctAndInRange() {
        Random random = new Random(4);
        for (int deal = 0; deal < 100; deal++) {
            List<Integer> healths = HigherOrLower.dealHealths(10, 1300, 3700, random);
            assertEquals(10, new HashSet<>(healths).size());
            healths.forEach(health -> assertTrue(health >= 1300 && health <= 3700, String.valueOf(health)));
        }
        assertThrows(IllegalArgumentException.class, () -> new HigherOrLower(List.of(5, 5), Order.LOWEST_FIRST));
    }

    @Test
    void theCatalogueHasTenBlazeSpotsAlongTheColumn() {
        PuzzleCatalogue.HigherOrLowerRoom room = PuzzleCatalogueFixture.load().higherOrLower("blaze").orElseThrow();
        assertEquals(10, room.blazes().size());
        assertEquals(Order.LOWEST_FIRST, room.order());
        assertEquals(49, room.top() - room.bottom());
    }
}
