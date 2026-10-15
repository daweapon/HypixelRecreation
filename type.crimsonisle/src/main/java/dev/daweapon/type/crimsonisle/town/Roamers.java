package dev.daweapon.type.crimsonisle.town;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Town NPCs that move between stops on a timer (Pam, Uvia, Azax, Forast, Deng). Advancing the stop
 * changes their position, and each player's copy walks there (see {@code HypixelNPC#updateForPlayer}).
 */
public final class Roamers {
    private static final List<Route> ROUTES = new CopyOnWriteArrayList<>();

    private Roamers() {
    }

    public static final class Route {
        private final HypixelNPC npc;
        private final List<Pos> stops;
        private final long intervalTicks;
        private volatile int index;
        private long elapsed;

        private Route(HypixelNPC npc, List<Pos> stops, Duration interval) {
            this.npc = npc;
            this.stops = List.copyOf(stops);
            this.intervalTicks = Math.max(20, interval.toMillis() / 50);
        }

        public Pos current() {
            return stops.get(index);
        }

        private void tick(long ticks) {
            elapsed += ticks;
            if (elapsed < intervalTicks) return;
            elapsed = 0;
            index = (index + 1) % stops.size();
        }
    }

    public static Route register(HypixelNPC npc, List<Pos> stops, Duration interval) {
        Route route = new Route(npc, stops, interval);
        ROUTES.add(route);
        return route;
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(() -> ROUTES.forEach(route -> route.tick(20)))
                .repeat(TaskSchedule.seconds(1)).schedule();
    }
}
