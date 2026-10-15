package dev.daweapon.type.crystalhollows.layout;

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
    private static final int ATTEMPTS = 60;
    private static final int SEPARATION = 4;
    private static volatile long seed;

    private CrystalHollowsLayout() {
    }

    public static void generate() {
        generate(StructureCatalogue.load(), new Random().nextLong());
    }

    public static void generate(List<StructureSpec> specs, long layoutSeed) {
        Random random = new Random(layoutSeed);
        Map<String, StructurePlacement> best = Map.of();
        int bestMissing = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < ATTEMPTS && bestMissing > 0; attempt++) {
            Map<String, StructurePlacement> result = attempt(specs, random);
            int missing = missingRequired(specs, result);
            if (missing < bestMissing || (missing == bestMissing && result.size() > best.size())) {
                best = result;
                bestMissing = missing;
            }
        }
        for (StructureSpec spec : specs) {
            if (!best.containsKey(spec.id()) && !spec.optional() && !spec.candidates().isEmpty()) {
                Logger.warn("Crystal Hollows layout could not place {}", spec.id());
            }
        }
        seed = layoutSeed;
        placements = Map.copyOf(best);
        Logger.info("Crystal Hollows layout {} placed {} of {} structures", layoutSeed, best.size(), specs.size());
    }

    private static Map<String, StructurePlacement> attempt(List<StructureSpec> specs, Random random) {
        Map<String, StructurePlacement> result = new LinkedHashMap<>();
        List<StructureSpec> order = new ArrayList<>(specs);
        Collections.shuffle(order, random);
        order.sort((a, b) -> {
            if (a.optional() != b.optional()) return a.optional() ? 1 : -1;
            return Integer.compare(a.candidates().size(), b.candidates().size());
        });
        for (StructureSpec spec : order) {
            Pos origin = choose(spec, result.values(), random);
            if (origin != null) result.put(spec.id(), new StructurePlacement(spec, origin));
        }
        return result;
    }

    private static int missingRequired(List<StructureSpec> specs, Map<String, StructurePlacement> result) {
        int missing = 0;
        for (StructureSpec spec : specs) {
            if (!spec.optional() && !spec.candidates().isEmpty() && !result.containsKey(spec.id())) missing++;
        }
        return missing;
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
            if (spec.site() != null && other.spec().site() != null) {
                if (overlaps(spec.site(), option, other.spec().site(), other.origin())) return false;
                continue;
            }
            double needed = Math.max(spec.clearance(), other.spec().clearance());
            double dx = option.x() - other.origin().x();
            double dz = option.z() - other.origin().z();
            if (Math.sqrt(dx * dx + dz * dz) < needed) return false;
        }
        return true;
    }

    private static boolean overlaps(StructureSite a, Pos aOrigin, StructureSite b, Pos bOrigin) {
        return aOrigin.blockX() - SEPARATION < bOrigin.blockX() + b.width() && bOrigin.blockX() - SEPARATION < aOrigin.blockX() + a.width()
                && aOrigin.blockY() - SEPARATION < bOrigin.blockY() + b.height() && bOrigin.blockY() - SEPARATION < aOrigin.blockY() + a.height()
                && aOrigin.blockZ() - SEPARATION < bOrigin.blockZ() + b.length() && bOrigin.blockZ() - SEPARATION < aOrigin.blockZ() + a.length();
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
