package dev.daweapon.dungeons.catacombs.puzzle.waterboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Water Board's maze (F1 run 1 capture): a wall of fixed blocks with six kinds of gate blocks that their levers push
 * in and out, a water inlet at the top switched by its own lever, and five holes along the bottom. Water reaching a hole
 * toggles that hole's door; the puzzle is solved while all five doors are open at once.
 */
public final class WaterBoard {
    public static final String WATER_LEVER = "water";

    public record Layout(int minZ, int maxY, List<String> rows, List<Gate> gates, int sourceZ, int sourceY, List<Hole> holes) {
        public Layout {
            rows = List.copyOf(rows);
            gates = List.copyOf(gates);
            holes = List.copyOf(holes);
        }

        public int width() {
            return rows.getFirst().length();
        }

        public int height() {
            return rows.size();
        }
    }

    public record Gate(String type, int z, int y, boolean extended) {
    }

    public record Hole(int z, String door) {
    }

    private final Layout layout;
    private final boolean[][] fixed;
    private final Map<Integer, Gate> gates = new LinkedHashMap<>();
    private final Map<String, Boolean> doors = new LinkedHashMap<>();
    private final WaterFlow flow;
    private final boolean[] holeWet;
    private boolean waterOn;
    private boolean moving = true;

    public WaterBoard(Layout layout, Set<String> closedDoors) {
        this.layout = layout;
        this.fixed = new boolean[layout.width()][layout.height()];
        for (int row = 0; row < layout.height(); row++) {
            String line = layout.rows().get(row);
            for (int column = 0; column < layout.width(); column++) fixed[column][row] = line.charAt(column) == '#';
        }
        layout.gates().forEach(gate -> gates.put(index(column(gate.z()), row(gate.y())), gate));
        layout.holes().forEach(hole -> doors.put(hole.door(), !closedDoors.contains(hole.door())));
        this.flow = new WaterFlow(layout.width(), layout.height(), this::open);
        this.holeWet = new boolean[layout.holes().size()];
    }

    private int index(int column, int row) {
        return row * layout.width() + column;
    }

    public int column(int z) {
        return z - layout.minZ();
    }

    public int row(int y) {
        return layout.maxY() - y;
    }

    public int z(int column) {
        return column + layout.minZ();
    }

    public int y(int row) {
        return layout.maxY() - row;
    }

    private boolean open(int column, int row) {
        if (fixed[column][row]) return false;
        if (column == column(layout.sourceZ()) && row == row(layout.sourceY())) return waterOn;
        Gate gate = gates.get(index(column, row));
        return gate == null || !gate.extended();
    }

    /** Flips every gate of one kind; returns the gates as they now stand. */
    public List<Gate> pull(String type) {
        if (WATER_LEVER.equals(type)) {
            setWater(!waterOn);
            return List.of();
        }
        List<Gate> moved = new ArrayList<>();
        for (Map.Entry<Integer, Gate> entry : gates.entrySet()) {
            Gate gate = entry.getValue();
            if (!gate.type().equals(type)) continue;
            Gate flipped = new Gate(gate.type(), gate.z(), gate.y(), !gate.extended());
            entry.setValue(flipped);
            if (flipped.extended()) flow.clear(column(gate.z()), row(gate.y()));
            moved.add(flipped);
            moving = true;
        }
        return moved;
    }

    public void setWater(boolean on) {
        waterOn = on;
        moving = true;
        flow.setSource(column(layout.sourceZ()), row(layout.sourceY()), on);
    }

    public boolean waterOn() {
        return waterOn;
    }

    /** One five-tick water update; returns the doors whose hole water has just reached, after toggling them. */
    public List<String> step() {
        moving = flow.step();
        List<String> toggled = new ArrayList<>();
        int bottom = layout.height() - 1;
        for (int index = 0; index < layout.holes().size(); index++) {
            Hole hole = layout.holes().get(index);
            boolean wet = flow.wet(column(hole.z()), bottom);
            if (wet && !holeWet[index]) {
                doors.computeIfPresent(hole.door(), (door, isOpen) -> !isOpen);
                toggled.add(hole.door());
            }
            holeWet[index] = wet;
        }
        return toggled;
    }

    /** True once the last water update changed nothing. */
    public boolean settled() {
        return !moving;
    }

    public boolean allDoorsOpen() {
        return doors.values().stream().allMatch(Boolean::booleanValue);
    }

    public void openAllDoors() {
        doors.replaceAll((door, open) -> true);
    }

    public boolean doorOpen(String door) {
        return doors.getOrDefault(door, false);
    }

    public Map<String, Boolean> doors() {
        return Map.copyOf(doors);
    }

    public List<Gate> gates() {
        return List.copyOf(gates.values());
    }

    public WaterFlow flow() {
        return flow;
    }

    public Layout layout() {
        return layout;
    }
}
