package dev.daweapon.type.skyblockgeneric.torrhus;

import org.junit.jupiter.api.Test;

import static dev.daweapon.type.skyblockgeneric.torrhus.ColorPuzzle.Color.*;
import static dev.daweapon.type.skyblockgeneric.torrhus.ColorPuzzle.Result.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorPuzzleTest {
    @Test
    void sequenceIsLeastToMostCommonColor() {
        assertEquals(java.util.List.of(CYAN, YELLOW, RED, BROWN, ORANGE), ColorPuzzle.SEQUENCE);
    }

    @Test
    void correctOrderSolves() {
        ColorPuzzle puzzle = new ColorPuzzle();
        assertEquals(PROGRESS, puzzle.press(CYAN));
        assertEquals(PROGRESS, puzzle.press(YELLOW));
        assertEquals(PROGRESS, puzzle.press(RED));
        assertEquals(PROGRESS, puzzle.press(BROWN));
        assertEquals(SOLVED, puzzle.press(ORANGE));
        assertEquals(0, puzzle.progress());
    }

    @Test
    void aWrongColourResetsButAFreshFirstColourStillCounts() {
        ColorPuzzle puzzle = new ColorPuzzle();
        puzzle.press(CYAN);
        puzzle.press(YELLOW);
        assertEquals(WRONG, puzzle.press(BLUE));
        assertEquals(0, puzzle.progress());
        puzzle.press(CYAN);
        assertEquals(WRONG, puzzle.press(CYAN));
        assertEquals(1, puzzle.progress());
    }
}
