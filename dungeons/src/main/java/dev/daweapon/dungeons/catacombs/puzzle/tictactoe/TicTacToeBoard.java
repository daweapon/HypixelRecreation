package dev.daweapon.dungeons.catacombs.puzzle.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** A 3 x 3 board, cells numbered 0-8 left to right from the top row as the player sees it. */
public final class TicTacToeBoard {
    public enum Mark {
        HYPIXEL,
        PLAYER
    }

    public enum Result {
        PLAYING,
        HYPIXEL_WON,
        PLAYER_WON,
        TIE
    }

    public static final int CELLS = 9;
    public static final int CENTRE = 4;
    public static final List<Integer> CORNERS = List.of(0, 2, 6, 8);
    private static final int[][] LINES = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};

    private final Mark[] cells = new Mark[CELLS];

    public TicTacToeBoard copy() {
        TicTacToeBoard copy = new TicTacToeBoard();
        System.arraycopy(cells, 0, copy.cells, 0, CELLS);
        return copy;
    }

    public boolean free(int cell) {
        return cell >= 0 && cell < CELLS && cells[cell] == null;
    }

    public void place(int cell, Mark mark) {
        if (!free(cell)) throw new IllegalArgumentException("Cell " + cell + " is taken");
        cells[cell] = mark;
    }

    public void clear(int cell) {
        cells[cell] = null;
    }

    public Optional<Mark> at(int cell) {
        return Optional.ofNullable(cells[cell]);
    }

    public List<Integer> freeCells() {
        List<Integer> free = new ArrayList<>();
        for (int cell = 0; cell < CELLS; cell++) if (cells[cell] == null) free.add(cell);
        return free;
    }

    public boolean empty() {
        return Arrays.stream(cells).allMatch(cell -> cell == null);
    }

    public Result result() {
        for (int[] line : LINES) {
            Mark first = cells[line[0]];
            if (first != null && first == cells[line[1]] && first == cells[line[2]]) {
                return first == Mark.HYPIXEL ? Result.HYPIXEL_WON : Result.PLAYER_WON;
            }
        }
        return freeCells().isEmpty() ? Result.TIE : Result.PLAYING;
    }
}
