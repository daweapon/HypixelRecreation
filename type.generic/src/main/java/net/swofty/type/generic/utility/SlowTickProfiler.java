package net.swofty.type.generic.utility;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.tinylog.Logger;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Samples the tick thread's stack while a tick is overrunning and periodically logs where the
 * time went, so a TPS drop on a live server can be attributed without attaching a profiler.
 */
public final class SlowTickProfiler {
    private static final long THRESHOLD_NANOS = Long.getLong("slowTickMs",
            Long.parseLong(System.getenv().getOrDefault("SLOW_TICK_MS", "100"))) * 1_000_000L;
    private static final long SAMPLE_INTERVAL_MS = 20;
    private static final long REPORT_INTERVAL_MS = 60_000;
    private static final int FRAMES_PER_SIGNATURE = 4;
    private static final int SIGNATURES_REPORTED = 8;

    private static volatile Thread tickThread;
    private static volatile long tickStartNanos;
    private static final Map<String, AtomicInteger> samples = new ConcurrentHashMap<>();
    private static final AtomicInteger overrunTicks = new AtomicInteger();
    private static final AtomicLong worstTickMillis = new AtomicLong();
    private static boolean started;

    private SlowTickProfiler() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tickThread = Thread.currentThread();
            tickStartNanos = System.nanoTime();
            return TaskSchedule.nextTick();
        }, ExecutionType.TICK_START);

        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event -> {
            double tickMillis = event.getTickMonitor().getTickTime();
            if (tickMillis > 50) overrunTicks.incrementAndGet();
            worstTickMillis.accumulateAndGet((long) tickMillis, Math::max);
        });

        Thread sampler = new Thread(SlowTickProfiler::run, "slow-tick-profiler");
        sampler.setDaemon(true);
        sampler.start();
    }

    private static void run() {
        long nextReport = System.currentTimeMillis() + REPORT_INTERVAL_MS;
        long lastGcMillis = totalGcMillis();
        while (true) {
            try {
                Thread.sleep(SAMPLE_INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }

            Thread thread = tickThread;
            if (thread != null && System.nanoTime() - tickStartNanos > THRESHOLD_NANOS) {
                samples.computeIfAbsent(signature(thread.getStackTrace()), key -> new AtomicInteger())
                        .incrementAndGet();
            }

            if (System.currentTimeMillis() >= nextReport) {
                long gcMillis = totalGcMillis();
                report(gcMillis - lastGcMillis);
                lastGcMillis = gcMillis;
                nextReport = System.currentTimeMillis() + REPORT_INTERVAL_MS;
            }
        }
    }

    /** The top frames plus the first project frame, which is usually the one worth fixing. */
    static String signature(StackTraceElement[] stack) {
        if (stack.length == 0) return "<idle>";
        String top = java.util.Arrays.stream(stack)
                .limit(FRAMES_PER_SIGNATURE)
                .map(SlowTickProfiler::frame)
                .collect(Collectors.joining(" <- "));
        for (StackTraceElement element : stack) {
            if (element.getClassName().startsWith("net.swofty")) {
                return top + " || first project frame: " + frame(element);
            }
        }
        return top;
    }

    private static String frame(StackTraceElement element) {
        String className = element.getClassName();
        return className.substring(className.lastIndexOf('.') + 1) + "." + element.getMethodName()
                + ":" + element.getLineNumber();
    }

    private static void report(long gcMillis) {
        int overruns = overrunTicks.getAndSet(0);
        long worst = worstTickMillis.getAndSet(0);
        if (overruns == 0 && samples.isEmpty()) return;

        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1_000_000;
        long maxMb = runtime.maxMemory() / 1_000_000;

        StringBuilder message = new StringBuilder()
                .append("Slow ticks in the last minute: ").append(overruns).append(" over 50ms, worst ")
                .append(worst).append("ms, GC ").append(gcMillis).append("ms, heap ")
                .append(usedMb).append('/').append(maxMb).append("MB. Samples while over ")
                .append(THRESHOLD_NANOS / 1_000_000).append("ms:");
        samples.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, AtomicInteger> entry) -> entry.getValue().get())
                        .reversed())
                .limit(SIGNATURES_REPORTED)
                .forEach(entry -> message.append("\n  ").append(entry.getValue().get()).append("x ")
                        .append(entry.getKey()));
        samples.clear();
        Logger.warn(message.toString());
    }

    private static long totalGcMillis() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                .filter(time -> time > 0)
                .sum();
    }
}
