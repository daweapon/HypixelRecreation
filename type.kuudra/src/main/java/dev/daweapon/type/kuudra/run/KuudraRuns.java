package dev.daweapon.type.kuudra.run;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.kuudra.arena.KuudraHollow;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Every run on this server, capped at {@link #MAX_RUNS}; abandoned runs are cleaned up every second. */
public final class KuudraRuns {
    public static final int MAX_RUNS = 4;
    public static final long EMPTY_LIFETIME_MILLIS = 60_000;
    private static final Map<UUID, KuudraRun> RUNS = new ConcurrentHashMap<>();

    private KuudraRuns() {
    }

    public static synchronized KuudraRun create(KuudraTier tier, List<UUID> roster) {
        if (RUNS.size() >= MAX_RUNS) throw new IllegalStateException("All " + MAX_RUNS + " Kuudra runs are in use");
        KuudraConfig config = KuudraConfig.get();
        KuudraHollow hollow = KuudraHollow.create(config.arena());
        KuudraRun run;
        try {
            run = new KuudraRun(tier, roster, hollow, config);
        } catch (RuntimeException exception) {
            hollow.destroy();
            throw exception;
        }
        RUNS.put(run.id(), run);
        run.open();
        Logger.info("Kuudra run {} created: {} tier, {} players, {} arena", run.id(), tier, roster.size(),
                hollow.placeholder() ? "placeholder" : "captured");
        return run;
    }

    public static @Nullable KuudraRun get(UUID id) {
        return RUNS.get(id);
    }

    public static @Nullable KuudraRun get(String id) {
        try {
            return get(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static @Nullable KuudraRun expecting(UUID player) {
        return RUNS.values().stream().filter(run -> run.isExpecting(player))
                .max(Comparator.comparingLong(KuudraRun::createdAt)).orElse(null);
    }

    public static @Nullable KuudraRun of(UUID player) {
        return RUNS.values().stream()
                .filter(run -> run.onlineMembers().stream().anyMatch(member -> member.getUuid().equals(player)))
                .findFirst().orElse(null);
    }

    public static List<KuudraRun> all() {
        return new ArrayList<>(RUNS.values());
    }

    public static int remainingSlots() {
        return Math.max(0, MAX_RUNS - RUNS.size());
    }

    public static void dispose(KuudraRun run) {
        if (RUNS.remove(run.id()) == null) return;
        run.shutDown();
        Logger.info("Kuudra run {} disposed", run.id());
    }

    public static void startCleanup() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            long now = System.currentTimeMillis();
            for (KuudraRun run : RUNS.values()) {
                if (run.isAbandoned(now)) dispose(run);
            }
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }
}
