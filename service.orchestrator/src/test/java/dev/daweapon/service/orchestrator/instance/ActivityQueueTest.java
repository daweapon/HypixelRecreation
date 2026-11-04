package dev.daweapon.service.orchestrator.instance;

import dev.daweapon.commons.ServerType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityQueueTest {
    private static final ServerType TYPE = ServerType.SKYBLOCK_KUUDRA;

    private static Allocation won() {
        return new Allocation(null, "game", null, null);
    }

    @Test
    void returnsImmediatelyWhenTheFirstAttemptSucceeds() {
        ActivityQueue queue = new ActivityQueue();

        assertTrue(queue.await(TYPE, Duration.ofSeconds(1), ActivityQueueTest::won).succeeded());
        assertEquals(0, queue.length(TYPE));
    }

    @Test
    void doesNotWaitForFailuresThatWaitingCannotFix() {
        ActivityQueue queue = new ActivityQueue();
        AtomicInteger attempts = new AtomicInteger();

        Allocation result = queue.await(TYPE, Duration.ofSeconds(5), () -> {
            attempts.incrementAndGet();
            return Allocation.failure(FailureReason.REFUSED, "no");
        });

        assertEquals(FailureReason.REFUSED, result.failure());
        assertEquals(1, attempts.get());
    }

    @Test
    void retriesWhileFullAndSucceedsOnceCapacityAppears() {
        ActivityQueue queue = new ActivityQueue();
        AtomicInteger attempts = new AtomicInteger();

        Allocation result = queue.await(TYPE, Duration.ofSeconds(10), () ->
                attempts.incrementAndGet() < 3 ? Allocation.failure(FailureReason.FULL, "full") : won());

        assertTrue(result.succeeded());
        assertEquals(3, attempts.get());
    }

    @Test
    void givesUpWithTheLastFailureAfterTheDeadline() {
        ActivityQueue queue = new ActivityQueue();

        Allocation result = queue.await(TYPE, Duration.ofMillis(300),
                () -> Allocation.failure(FailureReason.FULL, "full"));

        assertFalse(result.succeeded());
        assertEquals(FailureReason.FULL, result.failure());
        assertEquals(0, queue.length(TYPE));
    }

    @Test
    void servesWaitersInArrivalOrder() throws Exception {
        ActivityQueue queue = new ActivityQueue();
        AtomicBoolean capacity = new AtomicBoolean();
        List<String> served = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(2);

        Thread first = Thread.startVirtualThread(() -> {
            queue.await(TYPE, Duration.ofSeconds(10), () -> capacity.get()
                    ? serve(served, "first", capacity) : Allocation.failure(FailureReason.FULL, "full"));
            done.countDown();
        });
        while (queue.length(TYPE) < 1) Thread.sleep(10);
        Thread second = Thread.startVirtualThread(() -> {
            queue.await(TYPE, Duration.ofSeconds(10), () -> capacity.get()
                    ? serve(served, "second", capacity) : Allocation.failure(FailureReason.FULL, "full"));
            done.countDown();
        });
        while (queue.length(TYPE) < 2) Thread.sleep(10);

        capacity.set(true);
        queue.capacityMayHaveChanged();
        done.await();
        first.join();
        second.join();

        assertEquals(List.of("first", "second"), served);
    }

    @Test
    void aJoinThatSucceedsAtOnceIsNeverToldItsPosition() {
        List<Integer> told = Collections.synchronizedList(new ArrayList<>());
        ActivityQueue queue = new ActivityQueue((_, _, position, _) -> told.add(position));

        queue.await(TYPE, Duration.ofSeconds(1), waiter("a"), ActivityQueueTest::won);

        assertEquals(List.of(), told);
    }

    @Test
    void waitersAreToldTheirPositionAndToldAgainAsTheyMoveUp() throws Exception {
        List<String> told = Collections.synchronizedList(new ArrayList<>());
        ActivityQueue queue = new ActivityQueue((_, waiter, position, length) ->
                told.add(waiter.mode() + "#" + position + "/" + length));
        AtomicBoolean capacity = new AtomicBoolean();
        CountDownLatch done = new CountDownLatch(2);

        Thread first = Thread.startVirtualThread(() -> {
            queue.await(TYPE, Duration.ofSeconds(10), waiter("first"), () -> capacity.getAndSet(false)
                    ? won() : Allocation.failure(FailureReason.FULL, "full"));
            done.countDown();
        });
        while (queue.length(TYPE) < 1) Thread.sleep(10);
        Thread second = Thread.startVirtualThread(() -> {
            queue.await(TYPE, Duration.ofSeconds(10), waiter("second"), () -> capacity.getAndSet(false)
                    ? won() : Allocation.failure(FailureReason.FULL, "full"));
            done.countDown();
        });
        while (queue.length(TYPE) < 2) Thread.sleep(10);
        while (!told.contains("second#2/2")) Thread.sleep(10);

        capacity.set(true);
        queue.capacityMayHaveChanged();
        while (!told.contains("second#1/1")) Thread.sleep(10);
        capacity.set(true);
        queue.capacityMayHaveChanged();
        done.await();
        first.join();
        second.join();

        assertTrue(told.contains("first#1/1"));
        assertTrue(told.indexOf("second#2/2") < told.indexOf("second#1/1"));
    }

    @Test
    void theSnapshotListsEveryWaiterInOrder() throws Exception {
        ActivityQueue queue = new ActivityQueue();
        AtomicBoolean release = new AtomicBoolean();
        CountDownLatch done = new CountDownLatch(2);
        for (String name : List.of("first", "second")) {
            Thread.startVirtualThread(() -> {
                queue.await(TYPE, Duration.ofSeconds(10), waiter(name), () -> release.get()
                        ? won() : Allocation.failure(FailureReason.FULL, "full"));
                done.countDown();
            });
            int expected = name.equals("first") ? 1 : 2;
            while (queue.length(TYPE) < expected) Thread.sleep(10);
        }

        var snapshot = queue.snapshot();

        assertEquals(2, snapshot.size());
        assertEquals("first", snapshot.get(0).mode());
        assertEquals(1, snapshot.get(0).position());
        assertEquals("second", snapshot.get(1).mode());
        assertEquals(2, snapshot.get(1).position());
        assertEquals("server", snapshot.get(1).originServer());
        release.set(true);
        queue.capacityMayHaveChanged();
        done.await();
    }

    private static ActivityQueue.Waiter waiter(String mode) {
        return new ActivityQueue.Waiter("server", mode, List.of(java.util.UUID.randomUUID()));
    }

    private static Allocation serve(List<String> served, String name, AtomicBoolean capacity) {
        served.add(name);
        capacity.set(true);
        return won();
    }
}
