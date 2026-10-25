package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeStateTest {
    private static TreeState.Dice dice(boolean generates, boolean purple) {
        return new TreeState.Dice() {
            @Override
            public boolean generates(double percent) {
                return generates;
            }

            @Override
            public boolean purple(double percent) {
                return purple;
            }
        };
    }

    @Test
    void nothingGrowsBeforeThirtyDays() {
        TreeState tree = TreeState.fresh(100);
        assertEquals(0, tree.advance(129, dice(true, false)));
        assertEquals(0, tree.held());
    }

    @Test
    void afterThirtyDaysEachDayRollsAndAFruitResetsTheClock() {
        TreeState tree = TreeState.fresh(100);
        assertEquals(1, tree.advance(135, dice(true, false)));
        assertEquals(1, tree.green());
        assertEquals(0, tree.advance(159, dice(true, false)));
        assertEquals(1, tree.advance(165, dice(true, true)));
        assertEquals(1, tree.purple());
    }

    @Test
    void aFruitIsGuaranteedAfterFortyTwoDaysWithoutLuck() {
        TreeState tree = TreeState.fresh(0);
        assertEquals(0, tree.advance(41, dice(false, false)));
        assertEquals(1, tree.advance(42, dice(false, false)));
        assertEquals(1, tree.green());
    }

    @Test
    void theTreeHoldsAtMostFiveCandies() {
        TreeState tree = TreeState.fresh(0);
        tree.advance(10_000, dice(true, false));
        assertEquals(TreeState.CAPACITY, tree.held());
        tree.take();
        assertEquals(0, tree.held());
    }

    @Test
    void stateSurvivesASaveAndLoad() {
        TreeState tree = TreeState.fresh(10);
        tree.advance(60, dice(true, true));
        TreeState loaded = TreeState.parse(tree.serialize(), 999);
        assertEquals(tree.purple(), loaded.purple());
        assertEquals(tree.green(), loaded.green());
        assertTrue(loaded.checkedOn(60));
    }

    @Test
    void aCalendarThatWentBackwardsDoesNotGrowCandy() {
        TreeState tree = TreeState.fresh(500);
        tree.advance(510, dice(true, false));
        assertEquals(0, tree.advance(5, dice(true, false)));
        assertEquals(0, tree.held());
    }
}
