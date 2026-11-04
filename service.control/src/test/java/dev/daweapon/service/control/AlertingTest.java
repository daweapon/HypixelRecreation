package dev.daweapon.service.control;

import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.ContainerState;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetSnapshot;
import dev.daweapon.service.control.watchdog.Alert;
import dev.daweapon.service.control.watchdog.AlertDispatcher;
import dev.daweapon.service.control.watchdog.AlertTransport;
import dev.daweapon.service.control.watchdog.NtfyAlertTransport;
import dev.daweapon.service.control.watchdog.Watchdog;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertingTest {

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

    private static ContainerInfo container(String name, ContainerState state) {
        return new ContainerInfo("id", name, "image", FleetGroup.classify(name), state,
                state == ContainerState.UP ? "running" : "exited", "none", 0, 0,
                Instant.now().minusSeconds(120), "status");
    }

    private static FleetSnapshot fleet(ContainerState redis) {
        return FleetSnapshot.of(List.of(
                container("hypixel_mongo", ContainerState.UP),
                container("hypixel_redis", redis),
                container("hypixel_proxy", ContainerState.UP)));
    }

    @Test
    void noAlertWhileADeliberateOperationIsRunning() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(fleet(ContainerState.EXIT));
        AtomicBoolean busy = new AtomicBoolean(true);
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 3);
        watchdog.suppressWhile(busy::get);

        for (int i = 0; i < 6; i++) {
            watchdog.poll();
        }
        assertTrue(transport.sent.isEmpty(), "a staggered restart or deploy must not page");

        busy.set(false);
        watchdog.poll();
        watchdog.poll();
        assertTrue(transport.sent.isEmpty(), "polls during the operation must not count towards confirmation");
        watchdog.poll();
        assertEquals(1, transport.sent.size(), "a fault that outlives the operation still pages");
    }

    @Test
    void recoveryIsStillAnnouncedDuringAnOperationAndSaysHowLongItLasted() {
        CapturingTransport transport = new CapturingTransport();
        AtomicReference<FleetSnapshot> source = new AtomicReference<>(fleet(ContainerState.EXIT));
        AtomicBoolean busy = new AtomicBoolean(false);
        Watchdog watchdog = new Watchdog(source::get, new AlertDispatcher(List.of(transport)), 30, 1);
        watchdog.suppressWhile(busy::get);

        watchdog.poll();
        busy.set(true);
        source.set(fleet(ContainerState.UP));
        watchdog.poll();

        assertEquals(2, transport.sent.size());
        Alert recovery = transport.sent.get(1);
        assertEquals(Alert.Severity.RECOVERY, recovery.severity());
        assertTrue(recovery.body().startsWith("Condition cleared after"));
    }

    @Test
    void ntfyFormattingCarriesPriorityAndTags() {
        NtfyAlertTransport.Message critical = NtfyAlertTransport.message(
                Alert.critical("dependency:MONGO", "Mongo is EXIT", "down"));
        assertEquals(5, critical.priority());
        assertEquals(List.of("rotating_light", "dependency"), critical.tags());
        assertEquals("Mongo is EXIT", critical.title());

        NtfyAlertTransport.Message recovery = NtfyAlertTransport.message(
                Alert.recovery("dependency:MONGO", "MONGO recovered", "Condition cleared."));
        assertEquals(3, recovery.priority());
        assertTrue(recovery.tags().contains("white_check_mark"));

        assertEquals(4, NtfyAlertTransport.message(Alert.warning("loop:x", "t", "b")).priority());
        assertEquals("t", NtfyAlertTransport.message(Alert.info("test:1", "t", "")).body());
    }

    @Test
    void ntfyTopicUrlIsSplitForJsonPublishing() {
        NtfyAlertTransport.Target target = NtfyAlertTransport.target("https://ntfy.sh/hypixel-ops-abc/");
        assertEquals("https://ntfy.sh", target.baseUrl());
        assertEquals("hypixel-ops-abc", target.topic());

        NtfyAlertTransport.Target selfHosted = NtfyAlertTransport.target("http://192.168.100.10:8090/alerts");
        assertEquals("http://192.168.100.10:8090", selfHosted.baseUrl());

        assertThrows(IllegalArgumentException.class, () -> NtfyAlertTransport.target("https://ntfy.sh"));
    }

    @Test
    void dispatchReportsEachTransportOutcome() {
        AlertTransport failing = new AlertTransport() {
            @Override
            public String name() {
                return "broken";
            }

            @Override
            public void send(Alert alert) {
                throw new IllegalStateException("no route");
            }
        };
        AlertDispatcher dispatcher = new AlertDispatcher(List.of(new CapturingTransport(), failing));
        Map<String, String> results = dispatcher.dispatchReporting(Alert.info("test:1", "t", "b"));
        assertEquals("ok", results.get("capture"));
        assertEquals("no route", results.get("broken"));
        assertEquals(1, dispatcher.recent().size());
    }

    @Test
    void durationsReadNaturally() {
        assertEquals("45s", Watchdog.describeDuration(Duration.ofSeconds(45)));
        assertEquals("12 min", Watchdog.describeDuration(Duration.ofMinutes(12)));
        assertEquals("3h 5min", Watchdog.describeDuration(Duration.ofMinutes(185)));
    }
}
