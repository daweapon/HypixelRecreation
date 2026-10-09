package net.swofty.commons.proxy;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyLinkSupervisorTest {
    private static final int GAME_PORT = 20003;

    @Test
    void backoffDoublesFromOneSecondAndCapsAtThirty() {
        ProxyBackoff backoff = new ProxyBackoff();
        List<Long> seconds = new ArrayList<>();
        for (int i = 0; i < 8; i++) seconds.add(backoff.next().toSeconds());

        assertEquals(List.of(1L, 2L, 4L, 8L, 16L, 30L, 30L, 30L), seconds);

        backoff.reset();
        assertEquals(1, backoff.next().toSeconds());
    }

    @Test
    void bootWaitsForALateProxyWithBackoffInsteadOfGivingUp() {
        FakeProxy proxy = new FakeProxy();
        ManualClock clock = new ManualClock();
        RecordingListener listener = new RecordingListener();
        ProxyLinkSupervisor supervisor = new ProxyLinkSupervisor(proxy, clock, listener);

        CompletableFuture<Integer> port = supervisor.connect();
        clock.advance(Duration.ofSeconds(60));

        assertFalse(port.isDone());
        assertEquals(List.of(1L, 2L, 4L, 8L, 16L, 30L), clock.scheduledSeconds);

        proxy.up = true;
        clock.advance(Duration.ofSeconds(30));

        assertEquals(GAME_PORT, port.join());
        assertEquals(1, proxy.registrations);
        assertNull(proxy.portsRequested.getFirst());
    }

    @Test
    void initialNameIsAdoptedOnceMonitoringStarts() {
        FakeProxy proxy = FakeProxy.running();
        ManualClock clock = new ManualClock();
        RecordingListener listener = new RecordingListener();
        ProxyLinkSupervisor supervisor = new ProxyLinkSupervisor(proxy, clock, listener);

        supervisor.connect().join();
        supervisor.startMonitoring();

        assertEquals(List.of("mini1A"), listener.names);
        assertEquals(List.of(true), listener.initialFlags);
        assertFalse(supervisor.isRecovering());
    }

    @Test
    void proxyRestartKeepsTheServerUpAndReRegistersOnTheSamePort() {
        FakeProxy proxy = FakeProxy.running();
        ManualClock clock = new ManualClock();
        RecordingListener listener = new RecordingListener();
        ProxyLinkSupervisor supervisor = startedSupervisor(proxy, clock, listener);

        proxy.up = false;
        clock.advance(Duration.ofSeconds(9));

        assertTrue(supervisor.isProxyLost());
        assertEquals(1, listener.lost);

        clock.advance(Duration.ofMinutes(5));
        assertEquals(1, listener.lost);
        assertEquals(0, listener.restored);

        proxy.restart();
        clock.advance(Duration.ofSeconds(3));

        assertFalse(supervisor.isProxyLost());
        assertFalse(supervisor.isRecovering());
        assertEquals(1, listener.restored);
        assertEquals(2, proxy.registrations);
        assertEquals(GAME_PORT, proxy.portsRequested.getLast());
        assertEquals(List.of("mini1A", "mini2A"), listener.names);
        assertEquals(List.of(true, false), listener.initialFlags);
        assertEquals("mini2A", supervisor.currentName().name());
    }

    @Test
    void heartbeatGapWithoutAProxyRestartKeepsTheExistingEntryAndName() {
        FakeProxy proxy = FakeProxy.running();
        ManualClock clock = new ManualClock();
        RecordingListener listener = new RecordingListener();
        ProxyLinkSupervisor supervisor = startedSupervisor(proxy, clock, listener);

        proxy.heartbeat = false;
        clock.advance(Duration.ofSeconds(12));
        assertTrue(supervisor.isProxyLost());

        proxy.heartbeat = true;
        clock.advance(Duration.ofSeconds(3));

        assertEquals(2, proxy.registrations);
        assertEquals(1, proxy.entries());
        assertEquals(1, proxy.nameCounter);
        assertEquals(List.of("mini1A"), listener.names);
        assertEquals(1, listener.restored);
    }

    @Test
    void fastProxyRestartThatNeverTripsTheHeartbeatIsCaughtByVerification() {
        FakeProxy proxy = FakeProxy.running();
        ManualClock clock = new ManualClock();
        RecordingListener listener = new RecordingListener();
        ProxyLinkSupervisor supervisor = startedSupervisor(proxy, clock, listener);

        proxy.restart();
        clock.advance(HEARTBEAT_PERIOD.multipliedBy(ProxyLinkSupervisor.VERIFY_EVERY_TICKS));

        assertEquals(2, proxy.registrations);
        assertEquals(1, proxy.entries());
        assertEquals("mini2A", supervisor.currentName().name());
        assertEquals(0, listener.lost);
    }

    @Test
    void singleMissedHeartbeatTriggersAnImmediateRegistrationCheck() {
        FakeProxy proxy = FakeProxy.running();
        ManualClock clock = new ManualClock();
        RecordingListener listener = new RecordingListener();
        startedSupervisor(proxy, clock, listener);

        proxy.up = false;
        clock.advance(Duration.ofSeconds(3));
        proxy.restart();
        clock.advance(Duration.ofSeconds(3));

        assertEquals(2, proxy.registrations);
        assertEquals(GAME_PORT, proxy.portsRequested.getLast());
        assertEquals(0, listener.lost);
    }

    @Test
    void registrationThatThrowsIsRetriedRatherThanAbandoned() {
        FakeProxy proxy = FakeProxy.running();
        proxy.throwOnRegister = 2;
        ManualClock clock = new ManualClock();
        ProxyLinkSupervisor supervisor = new ProxyLinkSupervisor(proxy, clock, new RecordingListener());

        CompletableFuture<Integer> port = supervisor.connect();
        clock.advance(Duration.ofSeconds(3));

        assertEquals(GAME_PORT, port.join());
    }

    @Test
    void refusedRegistrationKeepsRetryingAtTheCappedInterval() {
        FakeProxy proxy = FakeProxy.running();
        proxy.refuse = true;
        ManualClock clock = new ManualClock();
        ProxyLinkSupervisor supervisor = new ProxyLinkSupervisor(proxy, clock, new RecordingListener());

        CompletableFuture<Integer> port = supervisor.connect();
        clock.advance(Duration.ofMinutes(3));
        assertFalse(port.isDone());
        assertEquals(30, clock.scheduledSeconds.getLast());

        proxy.refuse = false;
        clock.advance(Duration.ofSeconds(30));
        assertEquals(GAME_PORT, port.join());
    }

    private static final Duration HEARTBEAT_PERIOD = ProxyLinkSupervisor.HEARTBEAT_INTERVAL;

    private static ProxyLinkSupervisor startedSupervisor(FakeProxy proxy, ManualClock clock, RecordingListener listener) {
        ProxyLinkSupervisor supervisor = new ProxyLinkSupervisor(proxy, clock, listener);
        supervisor.connect().join();
        supervisor.startMonitoring();
        clock.scheduledSeconds.clear();
        return supervisor;
    }

    private static final class FakeProxy implements ProxyLinkSupervisor.ProxyLink {
        boolean up;
        boolean heartbeat = true;
        boolean refuse;
        int throwOnRegister;
        int registrations;
        int nameCounter;
        String registeredName;
        Integer registeredPort;
        final List<Integer> portsRequested = new ArrayList<>();

        static FakeProxy running() {
            FakeProxy proxy = new FakeProxy();
            proxy.up = true;
            return proxy;
        }

        void restart() {
            up = true;
            heartbeat = true;
            registeredName = null;
            registeredPort = null;
        }

        int entries() {
            return registeredName == null ? 0 : 1;
        }

        @Override
        public boolean isProxyAlive() {
            return up && heartbeat;
        }

        @Override
        public CompletableFuture<ProxyLinkSupervisor.Registration> register(Integer port) {
            if (throwOnRegister > 0) {
                throwOnRegister--;
                throw new IllegalStateException("redis unavailable");
            }
            portsRequested.add(port);
            if (!up) return CompletableFuture.failedFuture(new TimeoutException());
            if (refuse) {
                return CompletableFuture.completedFuture(
                        new ProxyLinkSupervisor.Registration(null, 0, false, "refused"));
            }
            registrations++;
            if (registeredName == null) {
                registeredName = "mini" + (++nameCounter) + "A";
                registeredPort = port == null ? GAME_PORT : port;
            }
            return CompletableFuture.completedFuture(
                    new ProxyLinkSupervisor.Registration("game", registeredPort, true, null));
        }

        @Override
        public CompletableFuture<ProxyLinkSupervisor.AssignedName> requestName() {
            if (!up) return CompletableFuture.failedFuture(new TimeoutException());
            if (registeredName == null) {
                return CompletableFuture.completedFuture(
                        new ProxyLinkSupervisor.AssignedName(null, null, false, "not registered"));
            }
            return CompletableFuture.completedFuture(new ProxyLinkSupervisor.AssignedName(
                    registeredName, registeredName.replace("mini", "m"), true, null));
        }
    }

    private static final class ManualClock implements ProxyLinkSupervisor.Delayer {
        private record Task(long dueMillis, long order, Runnable runnable) {}

        private final PriorityQueue<Task> tasks = new PriorityQueue<>(
                Comparator.comparingLong(Task::dueMillis).thenComparingLong(Task::order));
        final List<Long> scheduledSeconds = new ArrayList<>();
        private long now;
        private long order;

        @Override
        public void schedule(Duration delay, Runnable task) {
            if (!delay.equals(HEARTBEAT_PERIOD)) scheduledSeconds.add(delay.toSeconds());
            tasks.add(new Task(now + delay.toMillis(), order++, task));
        }

        void advance(Duration duration) {
            long target = now + duration.toMillis();
            while (!tasks.isEmpty() && tasks.peek().dueMillis() <= target) {
                Task task = tasks.poll();
                now = task.dueMillis();
                task.runnable().run();
            }
            now = target;
        }
    }

    private static final class RecordingListener implements ProxyLinkSupervisor.Listener {
        final List<String> names = new ArrayList<>();
        final List<Boolean> initialFlags = new ArrayList<>();
        int lost;
        int restored;

        @Override
        public void onNameAssigned(ProxyLinkSupervisor.AssignedName name, boolean initial) {
            names.add(name.name());
            initialFlags.add(initial);
        }

        @Override
        public void onProxyLost() {
            lost++;
        }

        @Override
        public void onProxyRestored() {
            restored++;
        }
    }
}
