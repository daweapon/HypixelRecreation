package dev.daweapon.type.skyblockgeneric.garden.analyzer;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DnaBoardTest {
    @Test
    void generatedBoardsStartScrambledAndSolveBack() {
        for (int seed = 0; seed < 200; seed++) {
            DnaBoard board = DnaBoard.generate(new Random(seed));
            assertFalse(board.solved(), "seed " + seed);
            board.solve();
            assertTrue(board.solved(), "seed " + seed);
        }
    }

    @Test
    void swapsKeepEachColumnAPermutation() {
        DnaBoard board = DnaBoard.generate(new Random(1));
        assertFalse(board.swap(0, 0, 2));
        assertTrue(board.swap(3, 1, 2));
        int sum = 0;
        for (int row = 0; row < DnaBoard.COLOURS; row++) sum += board.get(row, 3);
        org.junit.jupiter.api.Assertions.assertEquals(6, sum);
    }

    @Test
    void aPlayerCanSolveItWithAdjacentSwaps() {
        DnaBoard board = DnaBoard.generate(new Random(5));
        DnaBoard target = DnaBoard.generate(new Random(5));
        target.solve();
        for (int col = 0; col < DnaBoard.COLUMNS; col++) {
            for (int pass = 0; pass < DnaBoard.COLOURS; pass++) {
                for (int row = 0; row + 1 < DnaBoard.COLOURS; row++) {
                    if (board.get(row, col) != target.get(row, col)) {
                        for (int below = row + 1; below < DnaBoard.COLOURS; below++) {
                            if (board.get(below, col) == target.get(row, col)) {
                                for (int up = below; up > row; up--) board.swap(col, up, up - 1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(board.solved());
    }
}
