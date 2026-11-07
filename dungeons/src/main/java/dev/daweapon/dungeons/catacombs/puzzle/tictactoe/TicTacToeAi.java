package dev.daweapon.dungeons.catacombs.puzzle.tictactoe;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Mark;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Hypixel's side of the board. It always moves first (in the middle up to Floor III, in the middle or a corner from
 * Floor IV) and then plays perfectly, so the best a player can do is a tie.
 */
public final class TicTacToeAi {
    private static final CatacombsFloor LAST_CENTRE_ONLY_FLOOR = CatacombsFloor.FLOOR_THREE;

    private TicTacToeAi() {
    }

    public static int opening(CatacombsFloor floor, RandomGenerator random) {
        if (floor.ordinal() <= LAST_CENTRE_ONLY_FLOOR.ordinal() || random.nextBoolean()) return TicTacToeBoard.CENTRE;
        return TicTacToeBoard.CORNERS.get(random.nextInt(TicTacToeBoard.CORNERS.size()));
    }

    public static int move(TicTacToeBoard board, RandomGenerator random) {
        List<Integer> best = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        for (int cell : board.freeCells()) {
            TicTacToeBoard next = board.copy();
            next.place(cell, Mark.HYPIXEL);
            int score = score(next, Mark.PLAYER, 1);
            if (score > bestScore) {
                bestScore = score;
                best.clear();
            }
            if (score == bestScore) best.add(cell);
        }
        if (best.isEmpty()) throw new IllegalStateException("The board is full");
        return best.get(random.nextInt(best.size()));
    }

    private static int score(TicTacToeBoard board, Mark toMove, int depth) {
        Result result = board.result();
        if (result == Result.HYPIXEL_WON) return 10 - depth;
        if (result == Result.PLAYER_WON) return depth - 10;
        if (result == Result.TIE) return 0;
        int best = toMove == Mark.HYPIXEL ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int cell : board.freeCells()) {
            board.place(cell, toMove);
            int score = score(board, toMove == Mark.HYPIXEL ? Mark.PLAYER : Mark.HYPIXEL, depth + 1);
            board.clear(cell);
            best = toMove == Mark.HYPIXEL ? Math.max(best, score) : Math.min(best, score);
        }
        return best;
    }
}
