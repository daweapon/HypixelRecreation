package dev.daweapon.service.orchestrator.instance;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.objects.orchestrator.OrchestratorQueueProtocol.WaitingTicket;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class ActivityQueue {
    private static final long POLL_MILLIS = 1000;

    private final Object monitor = new Object();
    private final Map<ServerType, Deque<Ticket>> waiting = new EnumMap<>(ServerType.class);

    public record Waiter(@Nullable String originServer, @Nullable String mode, List<UUID> players) {
        public static final Waiter ANONYMOUS = new Waiter(null, null, List.of());

        public Waiter {
            players = players == null ? List.of() : List.copyOf(players);
        }
    }

    @FunctionalInterface
    public interface PositionListener {
        PositionListener NONE = (_, _, _, _) -> {};

        void moved(ServerType type, Waiter waiter, int position, int length);
    }

    private record Ticket(Waiter waiter, long enqueuedAt) {}

    private final PositionListener listener;

    public ActivityQueue() {
        this(PositionListener.NONE);
    }

    public ActivityQueue(PositionListener listener) {
        this.listener = listener;
    }

    public Allocation await(ServerType type, Duration maxWait, Supplier<Allocation> attempt) {
        return await(type, maxWait, Waiter.ANONYMOUS, attempt);
    }

    public Allocation await(ServerType type, Duration maxWait, Waiter waiter, Supplier<Allocation> attempt) {
        Ticket ticket = new Ticket(waiter, System.currentTimeMillis());
        long deadline = System.nanoTime() + maxWait.toNanos();
        synchronized (monitor) {
            waiting.computeIfAbsent(type, _ -> new ArrayDeque<>()).addLast(ticket);
        }
        try {
            Allocation last = null;
            int announced = 0;
            while (true) {
                if (isHead(type, ticket)) {
                    last = attempt.get();
                    if (last.succeeded() || !last.failure().worthWaiting()) return last;
                }
                int[] place = positionOf(type, ticket);
                if (place[0] != announced) {
                    announced = place[0];
                    listener.moved(type, waiter, place[0], place[1]);
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

    private boolean isHead(ServerType type, Ticket ticket) {
        synchronized (monitor) {
            return waiting.get(type).peekFirst() == ticket;
        }
    }

    private int[] positionOf(ServerType type, Ticket ticket) {
        synchronized (monitor) {
            Deque<Ticket> queue = waiting.get(type);
            int index = 1;
            for (Ticket queued : queue) {
                if (queued == ticket) return new int[]{index, queue.size()};
                index++;
            }
            return new int[]{0, queue.size()};
        }
    }

    public void capacityMayHaveChanged() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public int length(ServerType type) {
        synchronized (monitor) {
            Deque<Ticket> queue = waiting.get(type);
            return queue == null ? 0 : queue.size();
        }
    }

    public List<WaitingTicket> snapshot() {
        long now = System.currentTimeMillis();
        List<WaitingTicket> tickets = new ArrayList<>();
        synchronized (monitor) {
            waiting.forEach((type, queue) -> {
                int position = 1;
                for (Ticket ticket : queue) {
                    Waiter waiter = ticket.waiter();
                    tickets.add(new WaitingTicket(type, position++, queue.size(), waiter.mode(),
                            waiter.originServer(), waiter.players(), Math.max(0, now - ticket.enqueuedAt())));
                }
            });
        }
        return tickets;
    }
}
