package dev.daweapon.service.control;

import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.ContainerState;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetSnapshot;
import dev.daweapon.service.control.watchdog.Alert;
import dev.daweapon.service.control.watchdog.AlertDispatcher;
import dev.daweapon.service.control.watchdog.AlertTransport;
import dev.daweapon.service.control.watchdog.Watchdog;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WatchdogTest {

    /** Captures dispatched alerts instead of sending them anywhere. */
    private static final class CapturingTransport implements AlertTransport {
        final List<Alert> sent = new ArrayList<>();

        @Override
        public String name() {
            return "capture";
        }

        @Override
        public void send(Alert alert) {
            sent.add(alert);
        }
    }

    private static ContainerInfo container(String name, ContainerState state, int restarts) {
        return new ContainerInfo("id", name, "image", FleetGroup.classify(name), state,
                state == ContainerState.UP ? "running" : "exited", "none", restarts, 0,
                Instant.now().minusSeconds(120), "status");
    }

    private static FleetSnapshot healthy() {
        return FleetSnapshot.of(List.of(
                container("hypixel_mongo", ContainerState.UP, 0),
                container("hypixel_redis", ContainerState.UP, 0),
                container("hypixel_proxy", ContainerState.UP, 0)));
    }

    private static FleetSnapshot mongoDown() {
        return FleetSnapshot.of(List.of(
                container("hypixel_mongo", ContainerState.EXIT, 0),
                container("hypixel_redis", ContainerState.UP, 0),
                container("hypixel_proxy", ContainerState.UP, 0)));
    }

    @Test
    void doesNotAlertBeforeTheConfirmationThresholdIsReached() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(mongoDown());
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 3);

        watchdog.poll();
        watchdog.poll();
        assertTrue(transport.sent.isEmpty(),
                "a transient blip must not page before it has been confirmed");

        watchdog.poll();
        assertEquals(1, transport.sent.size());
        assertEquals(Alert.Severity.CRITICAL, transport.sent.get(0).severity());
        assertTrue(transport.sent.get(0).title().contains("Mongo"));
    }

    @Test
    void doesNotRepeatAnAlertWhileTheConditionPersists() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(mongoDown());
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 1);

        for (int i = 0; i < 5; i++) {
            watchdog.poll();
        }
        assertEquals(1, transport.sent.size(), "a standing fault must page once, not every poll");
    }

    @Test
    void announcesRecoveryWhenTheConditionClears() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(mongoDown());
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 1);

        watchdog.poll();
        source.set(healthy());
        watchdog.poll();

        assertEquals(2, transport.sent.size());
        assertEquals(Alert.Severity.RECOVERY, transport.sent.get(1).severity());
    }

    @Test
    void aTransientBlipThatClearsNeverPages() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(mongoDown());
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 3);

        watchdog.poll();
        source.set(healthy());
        watchdog.poll();
        watchdog.poll();
        watchdog.poll();

        assertTrue(transport.sent.isEmpty(),
                "a fault that cleared before confirmation must not page, nor announce recovery");
    }

    @Test
    void risingRestartCountIsDetectedAsALoopEvenWhileTheContainerReadsAsRunning() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(FleetSnapshot.of(List.of(
                container("hypixel_mongo", ContainerState.UP, 0),
                container("hypixel_redis", ContainerState.UP, 0),
                container("hypixel_proxy", ContainerState.UP, 0),
                container("service_party", ContainerState.UP, 4))));
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 1);

        watchdog.poll();
        assertTrue(transport.sent.isEmpty(), "a static restart count is history, not a loop");

        source.set(FleetSnapshot.of(List.of(
                container("hypixel_mongo", ContainerState.UP, 0),
                container("hypixel_redis", ContainerState.UP, 0),
                container("hypixel_proxy", ContainerState.UP, 0),
                container("service_party", ContainerState.UP, 6))));
        watchdog.poll();

        assertEquals(1, transport.sent.size());
        assertTrue(transport.sent.get(0).title().contains("service_party"));
        assertTrue(transport.sent.get(0).body().contains("rose from 4 to 6"));
    }

    @Test
    void mutingStopsAlertsEntirely() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(mongoDown());
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 1);

        watchdog.setEnabled(false);
        for (int i = 0; i < 3; i++) {
            watchdog.poll();
        }
        assertTrue(transport.sent.isEmpty());
    }
}
