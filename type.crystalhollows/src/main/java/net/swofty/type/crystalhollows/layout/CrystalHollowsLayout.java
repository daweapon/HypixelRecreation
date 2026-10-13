package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class CrystalHollowsLayout {
    private static volatile Map<String, StructurePlacement> placements = Map.of();
    private static volatile long seed;

    private CrystalHollowsLayout() {
    }

    public static void generate() {
        generate(StructureCatalogue.load(), new Random().nextLong());
    }

    public static void generate(List<StructureSpec> specs, long layoutSeed) {
        Random random = new Random(layoutSeed);
        Map<String, StructurePlacement> result = new LinkedHashMap<>();
        List<StructureSpec> order = new ArrayList<>(specs);
        order.sort((a, b) -> Integer.compare(a.candidates().size(), b.candidates().size()));
        for (StructureSpec spec : order) {
            Pos origin = choose(spec, result.values(), random);
            if (origin == null) {
                if (!spec.optional() && !spec.candidates().isEmpty()) {
                    Logger.warn("Crystal Hollows layout could not place {}", spec.id());
                }
                continue;
            }
            result.put(spec.id(), new StructurePlacement(spec, origin));
        }
        seed = layoutSeed;
        placements = Map.copyOf(result);
        Logger.info("Crystal Hollows layout {} placed {} of {} structures", layoutSeed, result.size(), specs.size());
    }

    private static Pos choose(StructureSpec spec, Iterable<StructurePlacement> placed, Random random) {
        List<Pos> options = new ArrayList<>(spec.candidates());
        Collections.shuffle(options, random);
        for (Pos option : options) {
            if (clear(spec, option, placed)) return option;
        }
        return null;
    }

    private static boolean clear(StructureSpec spec, Pos option, Iterable<StructurePlacement> placed) {
        for (StructurePlacement other : placed) {
            double needed = Math.max(spec.clearance(), other.spec().clearance());
            double dx = option.x() - other.origin().x();
            double dz = option.z() - other.origin().z();
            if (Math.sqrt(dx * dx + dz * dz) < needed) return false;
        }
        return true;
    }

    public static boolean isPlaced(String id) {
        return placements.containsKey(id);
    }

    public static StructurePlacement placement(String id) {
        return placements.get(id);
    }

    public static Pos origin(String id) {
        StructurePlacement placement = placements.get(id);
        return placement == null ? null : placement.origin();
    }

    public static Pos anchor(String id, String anchor) {
        StructurePlacement placement = placements.get(id);
        return placement == null ? null : placement.anchor(anchor);
    }

    public static Map<String, StructurePlacement> placements() {
        return placements;
    }

    public static long seed() {
        return seed;
    }
}
