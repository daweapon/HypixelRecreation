package net.swofty.type.kuudra.fight;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * Where the floating crates go: six of the seven named spots, each a little way out into the lava with
 * some jitter (research K1 2.2: "pre-defined locations with a tad bit of randomness").
 */
public final class SupplySpots {
    private SupplySpots() {
    }

    public static List<String> pick(Collection<String> spots, int count, RandomGenerator random) {
        if (count > spots.size()) throw new IllegalArgumentException("Only " + spots.size() + " supply spots for " + count + " crates");
        List<String> remaining = new ArrayList<>(spots);
        List<String> picked = new ArrayList<>();
        while (picked.size() < count) picked.add(remaining.remove(random.nextInt(remaining.size())));
        return picked;
    }

    public static Pos crateFor(KuudraArena arena, KuudraArena.SupplySpot spot, double distance, double jitter,
                               RandomGenerator random) {
        if (spot.crate() != null) {
            return spot.crate().add((random.nextDouble() * 2 - 1) * jitter, 0, (random.nextDouble() * 2 - 1) * jitter);
        }
        Pos base = arena.lavaBeside(spot.position(), distance + random.nextDouble() * jitter);
        boolean alongX = base.z() < arena.platform().minZ() || base.z() > arena.platform().maxZ() + 1;
        double slide = (random.nextDouble() * 2 - 1) * jitter;
        return alongX ? base.add(slide, 0, 0) : base.add(0, 0, slide);
    }

    /**
     * One reel: the crate moves up to {@code blocks} toward the player along the lava, stopping where it would
     * no longer float.
     */
    public static Pos reel(Pos crate, Pos player, double blocks, Predicate<Pos> floats) {
        double dx = player.x() - crate.x();
        double dz = player.z() - crate.z();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1e-6) return crate;
        double step = Math.min(blocks, length);
        Pos result = crate;
        for (double moved = 0.25; moved <= step + 1e-9; moved += 0.25) {
            Pos next = crate.add(dx / length * moved, 0, dz / length * moved);
            if (!floats.test(next)) break;
            result = next;
        }
        return result;
    }

    /** Whether the crate has been pulled as far toward {@code shore} as it goes, so it can be lifted out. */
    public static boolean atShore(Pos crate, Pos shore, Predicate<Pos> floats) {
        return reel(crate, shore, 0.75, floats).distance(crate) < 0.5;
    }
}
