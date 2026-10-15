package dev.daweapon.service.orchestrator.instance;

import dev.daweapon.commons.ServerType;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ActivityQueue {
    private static final long POLL_MILLIS = 1000;

    private final Object monitor = new Object();
    private final Map<ServerType, Deque<Object>> waiting = new EnumMap<>(ServerType.class);

    public Allocation await(ServerType type, Duration maxWait, Supplier<Allocation> attempt) {
        Object ticket = new Object();
        long deadline = System.nanoTime() + maxWait.toNanos();
        synchronized (monitor) {
            waiting.computeIfAbsent(type, _ -> new ArrayDeque<>()).addLast(ticket);
        }
        try {
            Allocation last = null;
            while (true) {
                if (isHead(type, ticket)) {
                    last = attempt.get();
                    if (last.succeeded() || !last.failure().worthWaiting()) return last;
                }
                synchronized (monitor) {
                    long remainingMillis = (deadline - System.nanoTime()) / 1_000_000;
                    if (remainingMillis <= 0) {
                        return last != null ? last : Allocation.failure(FailureReason.FULL, "Timed out in the queue");
                    }
                    monitor.wait(Math.min(POLL_MILLIS, remainingMillis));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Allocation.failure(FailureReason.TIMEOUT, "Interrupted while queued");
        } finally {
            synchronized (monitor) {
                waiting.get(type).remove(ticket);
                monitor.notifyAll();
            }
        }
    }

    private boolean isHead(ServerType type, Object ticket) {
        synchronized (monitor) {
            return waiting.get(type).peekFirst() == ticket;
        }
    }

    public void capacityMayHaveChanged() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public int length(ServerType type) {
        synchronized (monitor) {
            Deque<Object> queue = waiting.get(type);
            return queue == null ? 0 : queue.size();
        }
    }
}
