package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.catacombs.arena.CatacombsArena;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Every run on this server, capped at {@link #MAX_RUNS}; abandoned runs are cleaned up every second. */
public final class CatacombsRuns {
    public static final int MAX_RUNS = 4;
    public static final long EMPTY_LIFETIME_MILLIS = 60_000;
    private static final Map<UUID, CatacombsRun> RUNS = new ConcurrentHashMap<>();

    private CatacombsRuns() {
    }

    public static CatacombsRun create(CatacombsFloorId floor, List<UUID> roster) {
        return create(floor, roster, ThreadLocalRandom.current().nextLong());
    }

    public static synchronized CatacombsRun create(CatacombsFloorId floor, List<UUID> roster, long seed) {
        if (CatacombsRunSlots.full(MAX_RUNS, RUNS.size())) {
            throw new IllegalStateException("All " + MAX_RUNS + " Catacombs runs are in use");
        }
        CatacombsRoster.validated(roster);
        CatacombsArena arena = CatacombsArena.create(floor.floor(), floor.mode(), seed);
        CatacombsRun run;
        try {
            run = new CatacombsRun(floor, roster, arena, seed);
        } catch (RuntimeException exception) {
            arena.destroy();
            throw exception;
        }
        RUNS.put(run.id(), run);
        run.open();
        Logger.info("Catacombs run {} created: {}, {} players, seed {}", run.id(), floor.encode(), roster.size(), seed);
        return run;
    }

    public static @Nullable CatacombsRun get(UUID id) {
        return RUNS.get(id);
    }

    public static @Nullable CatacombsRun get(String id) {
        try {
            return get(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static @Nullable CatacombsRun expecting(UUID player) {
        return RUNS.values().stream().filter(run -> run.isExpecting(player))
                .max(Comparator.comparingLong(CatacombsRun::createdAt)).orElse(null);
    }

    public static @Nullable CatacombsRun of(UUID player) {
        return RUNS.values().stream()
                .filter(run -> run.onlineMembers().stream().anyMatch(member -> member.getUuid().equals(player)))
                .findFirst().orElse(null);
    }

    public static List<CatacombsRun> all() {
        return new ArrayList<>(RUNS.values());
    }

    public static int remainingSlots() {
        return CatacombsRunSlots.remaining(MAX_RUNS, RUNS.size());
    }

    public static void dispose(CatacombsRun run) {
        if (RUNS.remove(run.id()) == null) return;
        run.shutDown();
        Logger.info("Catacombs run {} disposed", run.id());
    }

    public static void startCleanup() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            long now = System.currentTimeMillis();
            for (CatacombsRun run : RUNS.values()) {
                if (run.isAbandoned(now)) dispose(run);
            }
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }
}
