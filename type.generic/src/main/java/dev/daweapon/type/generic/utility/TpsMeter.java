package dev.daweapon.type.generic.utility;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerTickMonitorEvent;

/**
 * Ticks actually completed per second over a short rolling window. The previous readout was
 * 1000 / (last tick's duration), which reported a single slow tick as "TPS 1.6" even when the
 * server was otherwise keeping up.
 */
public final class TpsMeter {
    public static final double TARGET_TPS = 20;
    public static final double WARN_BELOW_TPS = 19;
    private static final long WINDOW_NANOS = 5_000_000_000L;
    private static final int CAPACITY = 256;

    private static final long[] tickEnds = new long[CAPACITY];
    private static int next;
    private static int size;
    private static volatile double lastTickMillis;
    private static boolean started;

    private TpsMeter() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event -> {
            lastTickMillis = event.getTickMonitor().getTickTime();
            record(System.nanoTime());
        });
    }

    static synchronized void record(long nanos) {
        tickEnds[next] = nanos;
        next = (next + 1) % CAPACITY;
        if (size < CAPACITY) size++;
    }

    public static double tps() {
        return tps(System.nanoTime());
    }

    static synchronized double tps(long now) {
        if (size == 0) return TARGET_TPS;
        int counted = 0;
        long oldest = now;
        for (int i = 0; i < size; i++) {
            long end = tickEnds[(next - 1 - i + CAPACITY) % CAPACITY];
            if (now - end > WINDOW_NANOS) break;
            counted++;
            oldest = end;
        }
        if (counted < 2) return Math.min(TARGET_TPS, lastTickMillis <= 0 ? TARGET_TPS : 1000 / lastTickMillis);
        double seconds = Math.max(now - oldest, 1_000_000_000L) / 1e9;
        return Math.min(TARGET_TPS, Math.round(counted / seconds * 10) / 10.0);
    }

    public static double lastTickMillis() {
        return lastTickMillis;
    }

    static synchronized void reset() {
        next = 0;
        size = 0;
        lastTickMillis = 0;
    }
}
