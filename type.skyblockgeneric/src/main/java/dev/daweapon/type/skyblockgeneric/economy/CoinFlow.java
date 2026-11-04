package dev.daweapon.type.skyblockgeneric.economy;

import dev.daweapon.commons.economy.CoinFlowStore;
import dev.daweapon.commons.economy.CoinFlowStore.Delta;
import dev.daweapon.commons.economy.CoinFlowStore.Kind;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CoinFlow {
    private static final long FLUSH_SECONDS = 30;
    private static final List<String> PLUMBING = List.of("java.", "jdk.", "dev.daweapon.type.generic.data.",
            "dev.daweapon.type.skyblockgeneric.data.", "dev.daweapon.type.skyblockgeneric.economy.",
            "dev.daweapon.type.skyblockgeneric.user.PlayerDataAccessors", "dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer");
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static final Object LOCK = new Object();
    private static Map<String, double[]> pending = new HashMap<>();
    private static LocalDate pendingDate = CoinFlowStore.today();
    private static boolean started;

    private CoinFlow() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "coin-flow-flush");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(CoinFlow::flush, FLUSH_SECONDS, FLUSH_SECONDS, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(CoinFlow::flush, "coin-flow-final-flush"));
    }

    public static void recordChange(double delta) {
        if (delta == 0 || Double.isNaN(delta) || Double.isInfinite(delta)) return;
        String caller = WALKER.walk(frames -> frames
                .map(frame -> frame.getDeclaringClass().getName())
                .filter(name -> PLUMBING.stream().noneMatch(name::startsWith))
                .findFirst()
                .orElse("Unknown"));
        CoinFlowCategories.Classified classified = CoinFlowCategories.classify(caller, delta);
        String category = classified.kind() == Kind.TRANSFER
                ? classified.category() + (delta >= 0 ? CoinFlowStore.RECEIVED : CoinFlowStore.PAID)
                : classified.category();
        record(classified.kind(), category, delta);
    }

    public static void record(Kind kind, String category, double delta) {
        if (delta == 0) return;
        Batch finished = null;
        synchronized (LOCK) {
            LocalDate today = CoinFlowStore.today();
            if (!today.equals(pendingDate)) {
                finished = swap();
                pendingDate = today;
            }
            double[] totals = pending.computeIfAbsent(CoinFlowStore.field(kind, category), _ -> new double[2]);
            totals[0] += delta;
            totals[1]++;
        }
        if (finished != null) {
            Batch batch = finished;
            Thread.startVirtualThread(() -> write(batch));
        }
    }

    private record Batch(LocalDate date, Map<String, Delta> deltas) {}

    private static void flush() {
        Batch batch;
        synchronized (LOCK) {
            batch = swap();
        }
        write(batch);
    }

    private static Batch swap() {
        Map<String, Delta> deltas = new HashMap<>();
        pending.forEach((field, totals) -> deltas.put(field, new Delta(totals[0], (long) totals[1])));
        pending = new HashMap<>();
        return new Batch(pendingDate, deltas);
    }

    private static void write(Batch batch) {
        if (batch.deltas().isEmpty()) return;
        try {
            CoinFlowStore.add(batch.date(), batch.deltas());
        } catch (Exception exception) {
            Logger.warn("Coin flow flush failed, {} entries dropped: {}", batch.deltas().size(), exception.getMessage());
        }
    }
}
