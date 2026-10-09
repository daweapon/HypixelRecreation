package net.swofty.type.generic.entity.npc.walk;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A* over a {@link WalkGrid} for a player-sized NPC: it steps up one block, drops up to three,
 * cuts diagonal corners only across open floor, and gives up after a node budget, returning the
 * route to the explored spot closest to the goal.
 */
public final class NPCPathfinder {
    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };
    private static final int[] STEPS = {0, 1, -1, -2, -3};
    private static final double MAX_STEP_UP = 1.1;
    private static final double BODY_HALF_WIDTH = 0.3;
    private static final double SAMPLE_SPACING = 0.25;

    public record Path(List<Vec> points, boolean complete) {
        public double length() {
            double length = 0;
            for (int i = 1; i < points.size(); i++) length += points.get(i - 1).distance(points.get(i));
            return length;
        }
    }

    private record Cell(int x, int y, int z, double height) {
        long key() {
            return NPCPathfinder.key(x, y, z);
        }
    }

    private static final class Node {
        final Cell cell;
        final Node parent;
        final double cost;
        final double estimate;

        Node(Cell cell, Node parent, double cost, double estimate) {
            this.cell = cell;
            this.parent = parent;
            this.cost = cost;
            this.estimate = estimate;
        }
    }

    private NPCPathfinder() {
    }

    public static Path find(WalkGrid grid, Point start, Point goal, int maxNodes) {
        Cell origin = locate(grid, start);
        int goalX = floor(goal.x());
        int goalY = floor(goal.y());
        int goalZ = floor(goal.z());

        PriorityQueue<Node> open = new PriorityQueue<>((a, b) -> Double.compare(a.cost + a.estimate, b.cost + b.estimate));
        Map<Long, Double> bestCost = new HashMap<>();
        Node first = new Node(origin, null, 0, heuristic(origin, goalX, goalY, goalZ));
        open.add(first);
        bestCost.put(origin.key(), 0.0);
        Node closest = first;
        int expanded = 0;

        while (!open.isEmpty() && expanded < maxNodes) {
            Node node = open.poll();
            if (node.cost > bestCost.getOrDefault(node.cell.key(), Double.MAX_VALUE)) continue;
            expanded++;
            if (reached(node.cell, goalX, goalY, goalZ)) {
                return build(grid, start, goal, node, true);
            }
            if (node.estimate < closest.estimate) closest = node;

            for (int[] direction : DIRECTIONS) {
                Cell next = step(grid, node.cell, direction[0], direction[1]);
                if (next == null) continue;
                double cost = node.cost + Math.hypot(direction[0], direction[1]) + 0.5 * Math.abs(next.y - node.cell.y);
                if (cost >= bestCost.getOrDefault(next.key(), Double.MAX_VALUE)) continue;
                bestCost.put(next.key(), cost);
                open.add(new Node(next, node, cost, heuristic(next, goalX, goalY, goalZ)));
            }
        }
        return build(grid, start, goal, closest, false);
    }

    static Cell step(WalkGrid grid, Cell from, int dx, int dz) {
        int x = from.x + dx;
        int z = from.z + dz;
        boolean diagonal = dx != 0 && dz != 0;
        for (int dy : STEPS) {
            if (diagonal && dy != 0) continue;
            int y = from.y + dy;
            double height = grid.standHeight(x, y, z);
            if (Double.isNaN(height)) continue;
            if (height - from.height > MAX_STEP_UP) continue;
            if (dy > 0 && !grid.passable(from.x, from.y + 2, from.z)) continue;
            if (dy < 0 && !clearDrop(grid, x, y, z, from.y)) continue;
            if (diagonal && (!grid.standable(from.x + dx, from.y, from.z) || !grid.standable(from.x, from.y, from.z + dz))) {
                return null;
            }
            return new Cell(x, y, z, height);
        }
        return null;
    }

    private static boolean clearDrop(WalkGrid grid, int x, int landing, int z, int fromY) {
        for (int y = landing + 2; y <= fromY + 1; y++) {
            if (!grid.passable(x, y, z)) return false;
        }
        return true;
    }

    private static Cell locate(WalkGrid grid, Point point) {
        int x = floor(point.x());
        int y = floor(point.y());
        int z = floor(point.z());
        for (int dy : new int[]{0, 1, -1, 2, -2}) {
            double height = grid.standHeight(x, y + dy, z);
            if (!Double.isNaN(height)) return new Cell(x, y + dy, z, height);
        }
        return new Cell(x, y, z, point.y());
    }

    private static boolean reached(Cell cell, int goalX, int goalY, int goalZ) {
        return Math.abs(cell.x - goalX) <= 1 && Math.abs(cell.z - goalZ) <= 1 && Math.abs(cell.y - goalY) <= 1;
    }

    private static double heuristic(Cell cell, int goalX, int goalY, int goalZ) {
        int dx = Math.abs(cell.x - goalX);
        int dz = Math.abs(cell.z - goalZ);
        return Math.max(dx, dz) + (Math.sqrt(2) - 1) * Math.min(dx, dz) + Math.abs(cell.y - goalY);
    }

    private static Path build(WalkGrid grid, Point start, Point goal, Node end, boolean complete) {
        List<Cell> cells = new ArrayList<>();
        for (Node node = end; node != null; node = node.parent) cells.add(node.cell);
        Collections.reverse(cells);

        List<Vec> points = new ArrayList<>();
        points.add(new Vec(start.x(), start.y(), start.z()));
        int anchor = 0;
        while (anchor < cells.size() - 1) {
            int reach = anchor + 1;
            while (reach + 1 < cells.size() && straight(grid, cells, anchor, reach + 1)) reach++;
            points.add(centre(cells.get(reach)));
            anchor = reach;
        }
        if (complete) {
            Vec target = new Vec(goal.x(), goal.y(), goal.z());
            if (!points.getLast().equals(target)) points.add(target);
        }
        return new Path(List.copyOf(points), complete);
    }

    private static boolean straight(WalkGrid grid, List<Cell> cells, int from, int to) {
        Cell a = cells.get(from);
        for (int i = from + 1; i <= to; i++) {
            if (cells.get(i).y != a.y || cells.get(i).height != a.height) return false;
        }
        Vec start = centre(a);
        Vec end = centre(cells.get(to));
        double length = start.distance(end);
        int samples = Math.max(1, (int) Math.ceil(length / SAMPLE_SPACING));
        for (int i = 0; i <= samples; i++) {
            Vec point = start.lerp(end, (double) i / samples);
            for (double ox : new double[]{-BODY_HALF_WIDTH, BODY_HALF_WIDTH}) {
                for (double oz : new double[]{-BODY_HALF_WIDTH, BODY_HALF_WIDTH}) {
                    int x = floor(point.x() + ox);
                    int z = floor(point.z() + oz);
                    if (grid.standHeight(x, a.y, z) != a.height) return false;
                }
            }
        }
        return true;
    }

    private static Vec centre(Cell cell) {
        return new Vec(cell.x + 0.5, cell.height, cell.z + 0.5);
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | ((y + 2048) & 0xFFF);
    }
}
