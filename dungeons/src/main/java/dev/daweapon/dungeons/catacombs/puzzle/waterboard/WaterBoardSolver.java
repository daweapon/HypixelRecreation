package dev.daweapon.dungeons.catacombs.puzzle.waterboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Works out which doors the board can open. Each lever setting, run with the water on until it settles, toggles some
 * doors; any mix of those runs (water off in between) toggles their combination, so the doors that can be opened are
 * the combinations of those toggle sets.
 */
public final class WaterBoardSolver {
    public static final int MAX_STEPS = 400;

    public record Run(List<String> levers, Set<String> toggled) {
    }

    private WaterBoardSolver() {
    }

    public static List<Run> runs(WaterBoard.Layout layout) {
        List<String> types = gateTypes(layout);
        List<Run> runs = new ArrayList<>();
        for (int mask = 0; mask < 1 << types.size(); mask++) {
            WaterBoard board = new WaterBoard(layout, Set.of());
            List<String> pulled = new ArrayList<>();
            for (int bit = 0; bit < types.size(); bit++) {
                if ((mask & 1 << bit) == 0) continue;
                board.pull(types.get(bit));
                pulled.add(types.get(bit));
            }
            Map<String, Boolean> before = board.doors();
            board.setWater(true);
            for (int step = 0; step < MAX_STEPS && !board.settled(); step++) board.step();
            Set<String> toggled = new LinkedHashSet<>();
            board.doors().forEach((door, open) -> {
                if (!open.equals(before.get(door))) toggled.add(door);
            });
            runs.add(new Run(List.copyOf(pulled), Set.copyOf(toggled)));
        }
        return runs;
    }

    public static List<String> gateTypes(WaterBoard.Layout layout) {
        return layout.gates().stream().map(WaterBoard.Gate::type).distinct().sorted().toList();
    }

    /** Whether some mix of runs toggles exactly these doors. */
    public static boolean canToggle(WaterBoard.Layout layout, List<Run> runs, Set<String> doors) {
        List<String> order = layout.holes().stream().map(WaterBoard.Hole::door).toList();
        int target = bits(order, doors);
        int[] basis = new int[Integer.SIZE];
        for (Run run : runs) insert(basis, bits(order, run.toggled()));
        return reduce(basis, target) == 0;
    }

    /** One way to toggle exactly these doors: the lever settings to run one after another, or empty when there is none. */
    public static List<Run> plan(WaterBoard.Layout layout, List<Run> runs, Set<String> doors) {
        List<String> order = layout.holes().stream().map(WaterBoard.Hole::door).toList();
        int target = bits(order, doors);
        Map<Integer, Run> byVector = new LinkedHashMap<>();
        for (Run run : runs) byVector.putIfAbsent(bits(order, run.toggled()), run);
        List<Integer> vectors = new ArrayList<>(byVector.keySet());
        for (int choice = 0; choice < 1 << Math.min(vectors.size(), 20); choice++) {
            int sum = 0;
            for (int index = 0; index < vectors.size(); index++) if ((choice & 1 << index) != 0) sum ^= vectors.get(index);
            if (sum != target) continue;
            List<Run> plan = new ArrayList<>();
            for (int index = 0; index < vectors.size(); index++) if ((choice & 1 << index) != 0) plan.add(byVector.get(vectors.get(index)));
            return plan;
        }
        return List.of();
    }

    private static int bits(List<String> order, Set<String> doors) {
        int bits = 0;
        for (int index = 0; index < order.size(); index++) if (doors.contains(order.get(index))) bits |= 1 << index;
        return bits;
    }

    private static void insert(int[] basis, int vector) {
        int reduced = vector;
        for (int bit = basis.length - 1; bit >= 0 && reduced != 0; bit--) {
            if ((reduced >>> bit & 1) == 0) continue;
            if (basis[bit] == 0) {
                basis[bit] = reduced;
                return;
            }
            reduced ^= basis[bit];
        }
    }

    private static int reduce(int[] basis, int vector) {
        int reduced = vector;
        for (int bit = basis.length - 1; bit >= 0; bit--) {
            if ((reduced >>> bit & 1) != 0 && basis[bit] != 0) reduced ^= basis[bit];
        }
        return reduced;
    }
}
