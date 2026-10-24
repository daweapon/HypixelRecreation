package dev.daweapon.type.skyblockgeneric.abiphone;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbiphoneGamesTest {
    private static boolean playerEverWins(TicTacToeBoard board) {
        for (int move = 0; move < 9; move++) {
            if (board.cell(move) != TicTacToeBoard.EMPTY) continue;
            TicTacToeBoard next = copy(board);
            next.play(move, TicTacToeBoard.PLAYER);
            if (next.winner() == TicTacToeBoard.PLAYER) return true;
            if (!next.finished()) next.play(next.aiMove(), TicTacToeBoard.AI);
            if (next.winner() == TicTacToeBoard.PLAYER || (!next.finished() && playerEverWins(next))) return true;
        }
        return false;
    }

    private static TicTacToeBoard copy(TicTacToeBoard board) {
        TicTacToeBoard copy = new TicTacToeBoard();
        for (int i = 0; i < 9; i++) if (board.cell(i) != TicTacToeBoard.EMPTY) copy.play(i, board.cell(i));
        return copy;
    }

    @Test
    void ticTacToeAiNeverLoses() {
        assertFalse(playerEverWins(new TicTacToeBoard()));
    }

    @Test
    void ticTacToeAiTakesAnImmediateWin() {
        TicTacToeBoard board = new TicTacToeBoard();
        board.play(0, TicTacToeBoard.AI);
        board.play(1, TicTacToeBoard.AI);
        board.play(3, TicTacToeBoard.PLAYER);
        board.play(4, TicTacToeBoard.PLAYER);
        assertEquals(2, board.aiMove());
    }

    @Test
    void snakeEndsOnTheWall() {
        SnakeGame game = new SnakeGame(6, 6, new Random(1));
        int steps = 0;
        while (game.step()) steps++;
        assertTrue(game.isOver());
        assertTrue(steps <= 6);
    }

    @Test
    void snakeGrowsAndScoresWhenEatingAnApple() {
        SnakeGame game = new SnakeGame(6, 6, new Random(3));
        for (int tries = 0; tries < 200 && game.score() == 0 && !game.isOver(); tries++) {
            int head = game.head();
            int target = game.apple();
            int dx = target % 6 - head % 6;
            int dy = target / 6 - head / 6;
            if (dx > 0) game.turn(SnakeGame.Direction.RIGHT);
            else if (dx < 0) game.turn(SnakeGame.Direction.LEFT);
            else if (dy > 0) game.turn(SnakeGame.Direction.DOWN);
            else game.turn(SnakeGame.Direction.UP);
            game.step();
        }
        assertEquals(1, game.score());
        assertNotEquals(game.apple(), game.head());
    }

    @Test
    void snakeCannotReverseOntoItself() {
        SnakeGame game = new SnakeGame(6, 6, new Random(1));
        game.turn(SnakeGame.Direction.LEFT);
        assertTrue(game.step());
    }

    @Test
    void gameStatsRoundTrip() {
        AbiphoneGameStats stats = AbiphoneGameStats.parse("").withSnakeScore(7).withSnakeScore(3).withWin().withLoss().withDraw().withDraw();
        assertEquals(new AbiphoneGameStats(7, 1, 1, 2), AbiphoneGameStats.parse(stats.serialize()));
    }
}
