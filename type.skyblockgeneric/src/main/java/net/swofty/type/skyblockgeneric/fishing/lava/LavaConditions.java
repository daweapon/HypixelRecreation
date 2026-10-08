package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.Optional;

/**
 * Blazing Volcano state that lava fishing reads (wiki: Blazing_Volcano): whether the volcano is erupting, how long
 * ago the last eruption began (the Gusher window) and where the one active geyser is (the Steaming-Hot Flounder).
 * The volcano service installs its own implementation with {@link #install}; until then the volcano never erupts
 * and the geyser moves between its nine points every 10 minutes.
 */
public interface LavaConditions {
    List<Pos> GEYSER_POINTS = List.of(
            new Pos(-397, 118, -772), new Pos(-390, 118, -754), new Pos(-386, 118, -795),
            new Pos(-377, 118, -767), new Pos(-365, 118, -761), new Pos(-360, 118, -782),
            new Pos(-357, 118, -797), new Pos(-344, 118, -786), new Pos(-349, 118, -766));
    long GEYSER_MOVE_MILLIS = 10 * 60 * 1000L;

    boolean isErupting();

    long millisSinceEruptionStarted();

    Optional<Pos> activeGeyser();

    static LavaConditions current() {
        return Holder.current;
    }

    static void install(LavaConditions conditions) {
        Holder.current = conditions;
    }

    final class Holder {
        private static volatile LavaConditions current = new Dormant();

        private Holder() {
        }
    }

    final class Dormant implements LavaConditions {
        @Override
        public boolean isErupting() {
            return false;
        }

        @Override
        public long millisSinceEruptionStarted() {
            return Long.MAX_VALUE;
        }

        @Override
        public Optional<Pos> activeGeyser() {
            int index = (int) Math.floorMod(System.currentTimeMillis() / GEYSER_MOVE_MILLIS, (long) GEYSER_POINTS.size());
            return Optional.of(GEYSER_POINTS.get(index));
        }
    }
}
