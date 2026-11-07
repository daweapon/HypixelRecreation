package dev.daweapon.dungeons.catacombs.puzzle.icefill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Finds a walk over every tile of a board from its entry to its exit, moving only to side neighbours. */
public final class IceFillSolver {
    private IceFillSolver() {
    }

    public static Optional<List<IceTile>> solve(IceFillBoard board) {
        List<IceTile> path = new ArrayList<>();
        path.add(board.start());
        Set<IceTile> seen = new HashSet<>(path);
        return extend(board, path, seen) ? Optional.of(List.copyOf(path)) : Optional.empty();
    }

    private static boolean extend(IceFillBoard board, List<IceTile> path, Set<IceTile> seen) {
        IceTile last = path.getLast();
        if (seen.size() == board.tiles().size()) return last.equals(board.exit());
        if (last.equals(board.exit())) return false;
        for (IceTile next : neighbours(last)) {
            if (!board.contains(next) || !seen.add(next)) continue;
            path.add(next);
            if (extend(board, path, seen)) return true;
            path.removeLast();
            seen.remove(next);
        }
        return false;
    }

    private static List<IceTile> neighbours(IceTile tile) {
        return List.of(new IceTile(tile.x() + 1, tile.z()), new IceTile(tile.x() - 1, tile.z()),
                new IceTile(tile.x(), tile.z() + 1), new IceTile(tile.x(), tile.z() - 1));
    }
}
