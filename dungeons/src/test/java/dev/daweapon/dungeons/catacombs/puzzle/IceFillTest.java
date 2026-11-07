package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillBoard;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillProgress;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillSolver;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillStep;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceTile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IceFillTest {
    private static final int[][] CAPTURED_WALK = {{23, 1, 15}, {23, 1, 16}, {22, 1, 16}, {22, 1, 15}, {22, 1, 14}, {21, 1, 14},
            {21, 1, 15}, {20, 1, 15}, {18, 2, 15}, {18, 2, 16}, {18, 2, 17}, {17, 2, 17}, {16, 2, 17}, {15, 2, 17}, {15, 2, 16},
            {16, 2, 16}, {16, 2, 15}, {17, 2, 15}, {17, 2, 14}, {18, 2, 14}, {18, 2, 13}, {17, 2, 13}, {16, 2, 13}, {15, 2, 13},
            {14, 2, 13}, {14, 2, 14}, {15, 2, 14}, {15, 2, 15}, {14, 2, 15}, {13, 2, 15}, {11, 3, 15}, {11, 3, 16}, {10, 3, 16},
            {9, 3, 16}, {9, 3, 17}, {10, 3, 17}, {11, 3, 17}, {11, 3, 18}, {10, 3, 18}, {9, 3, 18}, {8, 3, 18}, {7, 3, 18},
            {6, 3, 18}, {5, 3, 18}, {5, 3, 17}, {6, 3, 17}, {7, 3, 17}, {7, 3, 16}, {6, 3, 16}, {6, 3, 15}, {6, 3, 14}, {7, 3, 14},
            {7, 3, 13}, {8, 3, 13}, {9, 3, 13}, {9, 3, 14}, {10, 3, 14}, {11, 3, 14}, {11, 3, 13}, {11, 3, 12}, {10, 3, 12},
            {9, 3, 12}, {8, 3, 12}, {7, 3, 12}, {6, 3, 12}, {6, 3, 13}, {5, 3, 13}, {5, 3, 14}, {5, 3, 15}, {4, 3, 15}};

    private static List<IceFillBoard> boards() {
        return PuzzleCatalogueFixture.load().iceFill("ice_fill").orElseThrow().boards();
    }

    @Test
    void theRoomHasThreeBoardsOfGrowingSize() {
        List<IceFillBoard> boards = boards();
        assertEquals(3, boards.size());
        assertEquals(List.of(8, 22, 40), boards.stream().map(board -> board.tiles().size()).toList());
        assertEquals(List.of(1, 2, 3), boards.stream().map(IceFillBoard::y).toList());
    }

    @Test
    void theCapturedWalkSolvesEveryBoard() {
        IceFillPuzzle puzzle = new IceFillPuzzle(boards());
        int boardsDone = 0;
        for (int[] step : CAPTURED_WALK) {
            IceFillPuzzle.Outcome outcome = puzzle.step(step[1], new IceTile(step[0], step[2]));
            assertTrue(outcome.step() == IceFillStep.PACKED || outcome.step() == IceFillStep.BOARD_DONE, java.util.Arrays.toString(step));
            if (outcome.step() == IceFillStep.BOARD_DONE) boardsDone++;
        }
        assertEquals(3, boardsDone);
        assertTrue(puzzle.solved());
    }

    @Test
    void everyBoardHasAWalkFromItsEntryToItsExit() {
        for (IceFillBoard board : boards()) {
            List<IceTile> walk = IceFillSolver.solve(board).orElseThrow();
            assertEquals(board.tiles().size(), walk.size());
            assertEquals(Set.copyOf(walk), board.tiles());
            assertEquals(board.exit(), walk.getLast());
        }
    }

    @Test
    void steppingBackOntoPackedIceShattersTheBoard() {
        IceFillProgress progress = new IceFillProgress(boards().getFirst());
        assertEquals(IceFillStep.PACKED, progress.step(new IceTile(23, 15)));
        assertEquals(IceFillStep.PACKED, progress.step(new IceTile(23, 16)));
        assertEquals(IceFillStep.SHATTERED, progress.step(new IceTile(23, 15)));
    }

    @Test
    void standingStillOnATileChangesNothing() {
        IceFillProgress progress = new IceFillProgress(boards().getFirst());
        progress.step(new IceTile(23, 15));
        assertEquals(IceFillStep.IGNORED, progress.step(new IceTile(23, 15)));
        assertEquals(1, progress.walked().size());
    }

    @Test
    void aDiagonalMoveOrAJumpShattersTheBoard() {
        IceFillProgress diagonal = new IceFillProgress(boards().getFirst());
        diagonal.step(new IceTile(23, 15));
        assertEquals(IceFillStep.SHATTERED, diagonal.step(new IceTile(22, 14)));

        IceFillProgress jump = new IceFillProgress(boards().getFirst());
        jump.step(new IceTile(23, 15));
        assertEquals(IceFillStep.SHATTERED, jump.step(new IceTile(21, 15)));
    }

    @Test
    void aBoardMustBeEnteredOnItsFirstTile() {
        IceFillProgress progress = new IceFillProgress(boards().getFirst());
        assertEquals(IceFillStep.SHATTERED, progress.step(new IceTile(21, 14)));
        assertFalse(progress.started());
    }

    @Test
    void aShatteredBoardCanBeWalkedAgain() {
        IceFillProgress progress = new IceFillProgress(boards().getFirst());
        progress.step(new IceTile(23, 15));
        progress.reset();
        assertFalse(progress.started());
        List<IceTile> walk = IceFillSolver.solve(progress.board()).orElseThrow();
        IceFillStep last = IceFillStep.IGNORED;
        for (IceTile tile : walk) last = progress.step(tile);
        assertEquals(IceFillStep.BOARD_DONE, last);
    }

    @Test
    void tilesOffTheBoardsAreIgnored() {
        IceFillPuzzle puzzle = new IceFillPuzzle(boards());
        assertEquals(IceFillPuzzle.Outcome.NONE, puzzle.step(1, new IceTile(0, 0)));
        assertEquals(-1, puzzle.boardAt(2, new IceTile(23, 15)));
        assertEquals(0, puzzle.boardAt(1, new IceTile(23, 15)));
    }
}
