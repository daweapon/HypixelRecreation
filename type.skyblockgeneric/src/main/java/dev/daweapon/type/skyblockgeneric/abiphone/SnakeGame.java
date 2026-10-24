package dev.daweapon.type.skyblockgeneric.abiphone;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public final class SnakeGame {
    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        boolean opposes(Direction other) {
            return dx == -other.dx && dy == -other.dy;
        }
    }

    private final int width;
    private final int height;
    private final Random random;
    private final Deque<Integer> body = new ArrayDeque<>();
    private Direction direction = Direction.RIGHT;
    private int apple;
    private int score;
    private boolean over;

    public SnakeGame(int width, int height, Random random) {
        this.width = width;
        this.height = height;
        this.random = random;
        body.addFirst(index(1, height / 2));
        body.addFirst(index(2, height / 2));
        placeApple();
    }

    public void turn(Direction next) {
        if (!next.opposes(direction)) direction = next;
    }

    public boolean step() {
        if (over) return false;
        int head = body.peekFirst();
        int x = head % width + direction.dx;
        int y = head / width + direction.dy;
        if (x < 0 || y < 0 || x >= width || y >= height) return lose();
        int next = index(x, y);
        boolean eats = next == apple;
        if (!eats) body.removeLast();
        if (body.contains(next)) return lose();
        body.addFirst(next);
        if (eats) {
            score++;
            if (body.size() == width * height) return lose();
            placeApple();
        }
        return true;
    }

    private boolean lose() {
        over = true;
        return false;
    }

    private void placeApple() {
        int candidate;
        do {
            candidate = random.nextInt(width * height);
        } while (body.contains(candidate));
        apple = candidate;
    }

    private int index(int x, int y) {
        return y * width + x;
    }

    public boolean isOver() {
        return over;
    }

    public int score() {
        return score;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int apple() {
        return apple;
    }

    public int head() {
        return body.peekFirst();
    }

    public boolean occupies(int cell) {
        return body.contains(cell);
    }
}
