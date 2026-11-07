package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeAi;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Mark;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Result;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicTacToeTest {
    @Test
    void earlyFloorsAlwaysOpenInTheMiddle() {
        Random random = new Random(5);
        for (CatacombsFloor floor : new CatacombsFloor[]{CatacombsFloor.ENTRANCE, CatacombsFloor.FLOOR_ONE, CatacombsFloor.FLOOR_THREE}) {
            for (int game = 0; game < 50; game++) assertEquals(TicTacToeBoard.CENTRE, TicTacToeAi.opening(floor, random));
        }
    }

    @Test
    void laterFloorsOpenInTheMiddleOrACorner() {
        Random random = new Random(5);
        Set<Integer> openings = new HashSet<>();
        for (int game = 0; game < 200; game++) openings.add(TicTacToeAi.opening(CatacombsFloor.FLOOR_SEVEN, random));
        assertTrue(openings.contains(TicTacToeBoard.CENTRE));
        assertTrue(openings.size() > 2);
        openings.forEach(cell -> assertTrue(cell == TicTacToeBoard.CENTRE || TicTacToeBoard.CORNERS.contains(cell)));
    }

    @Test
    void hypixelNeverLosesWhateverThePlayerDoes() {
        for (int opening : new int[]{4, 0, 2, 6, 8}) {
            TicTacToeBoard board = new TicTacToeBoard();
            board.place(opening, Mark.HYPIXEL);
            assertNotEquals(Result.PLAYER_WON, worstCase(board));
        }
    }

    private static Result worstCase(TicTacToeBoard board) {
        Result best = null;
        for (int cell : board.freeCells()) {
            TicTacToeBoard next = board.copy();
            next.place(cell, Mark.PLAYER);
            Result result = next.result();
            if (result == Result.PLAYING) {
                next.place(TicTacToeAi.move(next, new Random(cell)), Mark.HYPIXEL);
                result = next.result() == Result.PLAYING ? worstCase(next) : next.result();
            }
            if (result == Result.PLAYER_WON) return result;
            if (best == null || result == Result.TIE) best = result;
        }
        return best;
    }

    @Test
    void theCapturedGameEndsInATie() {
        TicTacToeBoard board = new TicTacToeBoard();
        int[] hypixel = {8, 6, 1, 3, 2};
        int[] player = {4, 7, 5, 0};
        for (int move = 0; move < 5; move++) {
            board.place(hypixel[move], Mark.HYPIXEL);
            if (move < 4) board.place(player[move], Mark.PLAYER);
        }
        assertEquals(Result.TIE, board.result());
    }

    @Test
    void hypixelBlocksAndTakesWins() {
        TicTacToeBoard block = new TicTacToeBoard();
        block.place(4, Mark.HYPIXEL);
        block.place(0, Mark.PLAYER);
        block.place(8, Mark.HYPIXEL);
        block.place(2, Mark.PLAYER);
        assertEquals(1, TicTacToeAi.move(block, new Random(1)));

        TicTacToeBoard win = new TicTacToeBoard();
        win.place(4, Mark.HYPIXEL);
        win.place(1, Mark.PLAYER);
        win.place(0, Mark.HYPIXEL);
        win.place(2, Mark.PLAYER);
        assertEquals(8, TicTacToeAi.move(win, new Random(1)));
    }

    @Test
    void aTakenCellCannotBePlayedAgain() {
        TicTacToeBoard board = new TicTacToeBoard();
        board.place(4, Mark.HYPIXEL);
        assertThrows(IllegalArgumentException.class, () -> board.place(4, Mark.PLAYER));
    }

    @Test
    void theCatalogueHasNineButtonsAndTheSinkingWall() {
        PuzzleCatalogue.TicTacToeRoom room = PuzzleCatalogueFixture.load().ticTacToe("tic_tac_toe").orElseThrow();
        assertEquals(9, room.cells().size());
        assertEquals(new RoomBlock(13, 4, 8), room.cells().getFirst());
        assertEquals(new RoomBlock(15, 2, 8), room.cells().getLast());
        assertEquals(12, room.wall().size());
        assertEquals(new RoomBlock(14, 2, 2), room.chest());
    }
}
