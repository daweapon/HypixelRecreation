package dev.daweapon.service.control.integrity;

import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A slow report built off the request thread.
 *
 * Hashing every jar or every configuration file in the fleet can outlast the HTTP timeouts
 * between the app, service.api and the agent, so a request returns the last result at once and
 * starts a rebuild when that result is stale; the app polls while {@code refreshing} is true.
 */
public final class BackgroundReport<T> {
    public record View<T>(boolean refreshing, Instant computedAt, T report) {
    }

    private final String name;
    private final Duration ttl;
    private final Supplier<T> builder;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile T latest;
    private volatile Instant computedAt;

    public BackgroundReport(String name, Duration ttl, Supplier<T> builder) {
        this.name = name;
        this.ttl = ttl;
        this.builder = builder;
    }

    public View<T> view(boolean refresh) {
        boolean stale = computedAt == null || Duration.between(computedAt, Instant.now()).compareTo(ttl) >= 0;
        if (refresh || stale) {
            start();
        }
        return new View<>(running.get(), computedAt, latest);
    }

    /** The latest result, built on this thread when there is none yet. */
    public T latestOrBuild() {
        T current = latest;
        return current != null ? current : buildNow();
    }

    public T buildNow() {
        T fresh = builder.get();
        latest = fresh;
        computedAt = Instant.now();
        return fresh;
    }

    public void invalidate() {
        computedAt = null;
    }

    private void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                buildNow();
            } catch (Exception e) {
                Logger.error(e, "Building the {} report failed", name);
            } finally {
                running.set(false);
            }
        });
    }
}
