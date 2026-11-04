package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelayPuzzleTest {
    @Test
    void lockingEveryColumnLowestFirstSolvesTheRelay() {
        RelayPuzzle puzzle = RelayPuzzle.create(NineFRelay.WILDERNESS_TREE, 0, new Random(1));
        RelayPuzzle.Outcome last = RelayPuzzle.Outcome.NOTHING;
        for (int column = 0; column < RelayPuzzle.COLUMNS; column++) {
            for (int step = 0; step < RelayPuzzle.ROWS; step++) last = puzzle.lockIn(column, lowestOpen(puzzle, column), 0, new Random(2));
        }
        assertEquals(RelayPuzzle.Outcome.SOLVED, last);
        assertTrue(puzzle.isOver());
    }

    @Test
    void wrongOrderResetsTheColumn() {
        RelayPuzzle puzzle = RelayPuzzle.create(NineFRelay.WILDERNESS_TREE, 0, new Random(3));
        int lowest = lowestOpen(puzzle, 0);
        int wrong = (lowest + 1) % RelayPuzzle.ROWS;
        assertEquals(RelayPuzzle.Outcome.WRONG, puzzle.lockIn(0, wrong, 0, new Random(4)));
        assertFalse(puzzle.isLocked(0, lowest));
        assertEquals(0, puzzle.column());
    }

    @Test
    void runningOutOfTimeFails() {
        RelayPuzzle puzzle = RelayPuzzle.create(NineFRelay.AUCTION_HOUSE, 0, new Random(5));
        assertEquals(RelayPuzzle.Outcome.TIMED_OUT, puzzle.tick(NineFRelay.AUCTION_HOUSE.timerSeconds() * 1000L, new Random(6)));
    }

    @Test
    void rewardsMatchTheWikiTable() {
        assertEquals(18, NineFRelay.healthFor(9));
        assertEquals(8, NineFRelay.bonusContactsFor(9));
        assertEquals(0, NineFRelay.bonusContactsFor(3));
        assertEquals(1, NineFRelay.bonusContactsFor(4));
    }

    private static int lowestOpen(RelayPuzzle puzzle, int column) {
        int best = -1;
        for (int row = 0; row < RelayPuzzle.ROWS; row++) {
            if (puzzle.isLocked(column, row)) continue;
            if (best < 0 || puzzle.tone(column, row).pitch() < puzzle.tone(column, best).pitch()) best = row;
        }
        return best;
    }
}
