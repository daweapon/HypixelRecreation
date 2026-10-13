package net.swofty.type.skyblockgeneric.garden.greenhouse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public final class SnakeGame {
    public static final int ROWS = 5;
    public static final int COLUMNS = 9;
    public static final int GOAL = 8;

    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    public enum State {
        PLAYING, WON, LOST
    }

    private final Deque<Integer> body = new ArrayDeque<>();
    private final Random random;
    private int apple;
    private int apples;
    private State state = State.PLAYING;

    public SnakeGame(Random random) {
        this.random = random;
        body.addFirst(cell(COLUMNS / 2, ROWS / 2));
        placeApple();
    }

    private static int cell(int column, int row) {
        return row * COLUMNS + column;
    }

    private void placeApple() {
        do {
            apple = random.nextInt(ROWS * COLUMNS);
        } while (body.contains(apple));
    }

    public State move(Direction direction) {
        if (state != State.PLAYING) return state;
        int head = body.peekFirst();
        int column = head % COLUMNS + direction.dx;
        int row = head / COLUMNS + direction.dy;
        if (column < 0 || row < 0 || column >= COLUMNS || row >= ROWS) return state = State.LOST;
        int next = cell(column, row);
        boolean eats = next == apple;
        if (!eats) body.removeLast();
        if (body.contains(next)) return state = State.LOST;
        body.addFirst(next);
        if (eats) {
            apples++;
            if (apples >= GOAL) return state = State.WON;
            placeApple();
        }
        return state;
    }

    public State state() {
        return state;
    }

    public int apples() {
        return apples;
    }

    public int head() {
        return body.peekFirst();
    }

    public int apple() {
        return apple;
    }

    public boolean occupies(int cell) {
        return body.contains(cell);
    }

    public int length() {
        return body.size();
    }
}
