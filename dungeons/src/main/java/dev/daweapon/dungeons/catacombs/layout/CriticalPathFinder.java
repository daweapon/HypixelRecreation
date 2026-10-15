package dev.daweapon.dungeons.catacombs.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

final class CriticalPathFinder {
    private static final int SEARCH_BUDGET = 20_000;
    private static final int[][] STEPS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private final int width;
    private final int height;
    private final Cell end;
    private final int minLength;
    private final int maxLength;
    private final Random random;
    private final List<Cell> path = new ArrayList<>();
    private final Set<Cell> visited = new HashSet<>();
    private int steps;

    private CriticalPathFinder(int width, int height, Cell end, int minLength, int maxLength, Random random) {
        this.width = width;
        this.height = height;
        this.end = end;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.random = random;
    }

    static Optional<List<Cell>> find(int width, int height, Cell start, Cell end, int minLength, int maxLength, Random random) {
        CriticalPathFinder finder = new CriticalPathFinder(width, height, end, minLength, maxLength, random);
        finder.path.add(start);
        finder.visited.add(start);
        return finder.walk(start) ? Optional.of(List.copyOf(finder.path)) : Optional.empty();
    }

    private boolean walk(Cell current) {
        if (current.equals(end)) {
            return path.size() >= minLength;
        }
        if (++steps > SEARCH_BUDGET) {
            return false;
        }
        List<Cell> neighbours = new ArrayList<>();
        for (int[] step : STEPS) {
            Cell next = current.translate(step[0], step[1]);
            if (inside(next) && !visited.contains(next) && path.size() + 1 + next.distanceTo(end) <= maxLength) {
                neighbours.add(next);
            }
        }
        Collections.shuffle(neighbours, random);
        for (Cell next : neighbours) {
            path.add(next);
            visited.add(next);
            if (walk(next)) {
                return true;
            }
            path.removeLast();
            visited.remove(next);
        }
        return false;
    }

    private boolean inside(Cell cell) {
        return cell.col() >= 0 && cell.col() < width && cell.row() >= 0 && cell.row() < height;
    }
}
