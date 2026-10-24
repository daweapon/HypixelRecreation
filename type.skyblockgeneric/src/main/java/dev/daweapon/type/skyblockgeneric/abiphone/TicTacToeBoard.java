package dev.daweapon.type.skyblockgeneric.abiphone;

public final class TicTacToeBoard {
    public static final char EMPTY = ' ';
    public static final char PLAYER = 'X';
    public static final char AI = 'O';
    private static final int[][] LINES = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};

    private final char[] cells = new char[9];

    public TicTacToeBoard() {
        java.util.Arrays.fill(cells, EMPTY);
    }

    public char cell(int index) {
        return cells[index];
    }

    public boolean play(int index, char mark) {
        if (index < 0 || index > 8 || cells[index] != EMPTY || finished()) return false;
        cells[index] = mark;
        return true;
    }

    public int aiMove() {
        int best = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < 9; i++) {
            if (cells[i] != EMPTY) continue;
            cells[i] = AI;
            int score = minimax(false, 1);
            cells[i] = EMPTY;
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private int minimax(boolean aiTurn, int depth) {
        char won = winner();
        if (won == AI) return 10 - depth;
        if (won == PLAYER) return depth - 10;
        if (full()) return 0;
        int best = aiTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < 9; i++) {
            if (cells[i] != EMPTY) continue;
            cells[i] = aiTurn ? AI : PLAYER;
            int score = minimax(!aiTurn, depth + 1);
            cells[i] = EMPTY;
            best = aiTurn ? Math.max(best, score) : Math.min(best, score);
        }
        return best;
    }

    public char winner() {
        for (int[] line : LINES) {
            char mark = cells[line[0]];
            if (mark != EMPTY && mark == cells[line[1]] && mark == cells[line[2]]) return mark;
        }
        return EMPTY;
    }

    public boolean full() {
        for (char cell : cells) if (cell == EMPTY) return false;
        return true;
    }

    public boolean finished() {
        return winner() != EMPTY || full();
    }
}
