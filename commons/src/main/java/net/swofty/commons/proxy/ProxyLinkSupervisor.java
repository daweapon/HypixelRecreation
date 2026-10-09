package net.swofty.commons.proxy;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps a game server registered with the proxy for its whole lifetime.
 *
 * The proxy keeps its server list in memory, keyed by this server's internal UUID, so a proxy restart forgets
 * every server. Instead of exiting, the game server waits out the outage and registers again under the same
 * UUID and port; the proxy may hand back a different display name, which is adopted through the listener.
 */
public final class ProxyLinkSupervisor {
    public static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(3);
    public static final int HEARTBEAT_MAX_MISSES = 3;
    public static final int VERIFY_EVERY_TICKS = 10;
    private static final int STILL_WAITING_LOG_EVERY_MISSES = 10;

    public interface ProxyLink {
        boolean isProxyAlive() throws Exception;

        CompletableFuture<Registration> register(@Nullable Integer port);

        CompletableFuture<AssignedName> requestName();
    }

    @FunctionalInterface
    public interface Delayer {
        void schedule(Duration delay, Runnable task);
    }

    public interface Listener {
        void onNameAssigned(AssignedName name, boolean initial);

        default void onProxyLost() {}

        default void onProxyRestored() {}
    }

    public record Registration(String host, int port, boolean accepted, @Nullable String error) {}

    public record AssignedName(@Nullable String name, @Nullable String shortName,
                               boolean registered, @Nullable String error) {}

    private final ProxyLink link;
    private final Delayer delayer;
    private final Listener listener;
    private final AtomicInteger missed = new AtomicInteger();
    private final AtomicBoolean lost = new AtomicBoolean();
    private final AtomicBoolean recovering = new AtomicBoolean();
    private final AtomicBoolean monitoring = new AtomicBoolean();
    private volatile @Nullable Integer port;
    private volatile @Nullable AssignedName currentName;
    private int ticksSinceVerify;

    public ProxyLinkSupervisor(ProxyLink link, Delayer delayer, Listener listener) {
        this.link = link;
        this.delayer = delayer;
        this.listener = listener;
    }

    public CompletableFuture<Integer> connect() {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        registerUntilAccepted(new ProxyBackoff(), 1, result);
        return result;
    }

    public void startMonitoring() {
        if (!monitoring.compareAndSet(false, true)) return;
        recovering.set(true);
        acquireName(new ProxyBackoff(), true);
        delayer.schedule(HEARTBEAT_INTERVAL, this::tick);
    }

    public boolean isProxyLost() {
        return lost.get();
    }

    public boolean isRecovering() {
        return recovering.get();
    }

    public @Nullable Integer port() {
        return port;
    }

    public @Nullable AssignedName currentName() {
        return currentName;
    }

    private void registerUntilAccepted(ProxyBackoff backoff, int attempt, CompletableFuture<Integer> result) {
        CompletableFuture<Registration> request;
        try {
            request = Objects.requireNonNull(link.register(port), "register returned no future");
        } catch (Throwable throwable) {
            request = CompletableFuture.failedFuture(throwable);
        }

        request.whenComplete((registration, throwable) -> {
            if (throwable == null && registration != null && registration.accepted()) {
                Integer previous = port;
                if (previous != null && previous != registration.port()) {
                    Logger.warn("The proxy routed this server to port {} but it is listening on {}",
                            registration.port(), previous);
                } else {
                    port = registration.port();
                }
                if (attempt > 1) {
                    Logger.info("Registered with the proxy after {} attempts", attempt);
                }
                result.complete(registration.port());
                return;
            }

            Duration delay = backoff.next();
            Logger.warn("Couldn't register with the proxy (attempt {}: {}). Retrying in {}s",
                    attempt, describe(registration, throwable), delay.toSeconds());
            delayer.schedule(delay, () -> registerUntilAccepted(backoff, attempt + 1, result));
        });
    }

    private void acquireName(ProxyBackoff backoff, boolean initial) {
        requestNameSafely().whenComplete((name, throwable) -> {
            if (throwable == null && name != null && name.registered()) {
                adopt(name, initial);
                backoff.reset();
                recovering.set(false);
                if (!initial) listener.onProxyRestored();
                return;
            }

            Duration delay = backoff.next();
            if (throwable == null && name != null) {
                Logger.warn("The proxy does not know this server ({}). Re-registering, then asking again in {}s",
                        name.error() == null ? "not registered" : name.error(), delay.toSeconds());
                CompletableFuture<Integer> reRegistered = new CompletableFuture<>();
                registerUntilAccepted(new ProxyBackoff(), 1, reRegistered);
                reRegistered.thenRun(() -> delayer.schedule(delay, () -> acquireName(backoff, initial)));
                return;
            }

            Logger.warn("Couldn't get a server name from the proxy ({}). Retrying in {}s",
                    throwable == null ? "no response" : throwable.toString(), delay.toSeconds());
            delayer.schedule(delay, () -> acquireName(backoff, initial));
        });
    }

    private void adopt(AssignedName name, boolean initial) {
        AssignedName previous = currentName;
        currentName = name;
        if (initial || previous == null) {
            listener.onNameAssigned(name, true);
            return;
        }
        if (!Objects.equals(previous.name(), name.name()) || !Objects.equals(previous.shortName(), name.shortName())) {
            Logger.info("The proxy renamed this server from {} to {}", previous.name(), name.name());
            listener.onNameAssigned(name, false);
        }
    }

    private void tick() {
        try {
            checkHeartbeat();
        } catch (Throwable throwable) {
            Logger.error(throwable, "Proxy link check failed");
        } finally {
            delayer.schedule(HEARTBEAT_INTERVAL, this::tick);
        }
    }

    private void checkHeartbeat() {
        boolean alive;
        try {
            alive = link.isProxyAlive();
        } catch (Exception exception) {
            alive = false;
        }

        if (!alive) {
            int misses = missed.incrementAndGet();
            if (lost.get()) {
                if (misses % STILL_WAITING_LOG_EVERY_MISSES == 0) {
                    Logger.warn("Still waiting for the proxy to come back (heartbeat absent for ~{}s)",
                            misses * HEARTBEAT_INTERVAL.toSeconds());
                }
                return;
            }
            Logger.warn("Proxy heartbeat missing ({}/{})", misses, HEARTBEAT_MAX_MISSES);
            if (misses >= HEARTBEAT_MAX_MISSES && lost.compareAndSet(false, true)) {
                Logger.error("Proxy heartbeat absent for ~{}s. Staying up and waiting for the proxy to return",
                        misses * HEARTBEAT_INTERVAL.toSeconds());
                listener.onProxyLost();
            }
            return;
        }

        int previousMisses = missed.getAndSet(0);
        if (lost.compareAndSet(true, false)) {
            Logger.info("Proxy heartbeat is back after ~{}s. Re-registering with the proxy",
                    previousMisses * HEARTBEAT_INTERVAL.toSeconds());
            ticksSinceVerify = 0;
            reestablish();
            return;
        }

        if (previousMisses > 0 || ++ticksSinceVerify >= VERIFY_EVERY_TICKS) {
            ticksSinceVerify = 0;
            verifyRegistration();
        }
    }

    private void verifyRegistration() {
        if (recovering.get()) return;
        requestNameSafely().whenComplete((name, throwable) -> {
            if (throwable != null || name == null) return;
            if (name.registered()) {
                adopt(name, false);
                return;
            }
            Logger.warn("The proxy no longer lists this server ({}). Re-registering",
                    name.error() == null ? "not registered" : name.error());
            reestablish();
        });
    }

    private void reestablish() {
        if (!recovering.compareAndSet(false, true)) return;
        CompletableFuture<Integer> registered = new CompletableFuture<>();
        registerUntilAccepted(new ProxyBackoff(), 1, registered);
        registered.thenRun(() -> acquireName(new ProxyBackoff(), false));
    }

    private CompletableFuture<AssignedName> requestNameSafely() {
        try {
            return Objects.requireNonNull(link.requestName(), "requestName returned no future");
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(throwable);
        }
    }

    private static String describe(@Nullable Registration registration, @Nullable Throwable throwable) {
        if (throwable != null) return throwable.toString();
        if (registration == null) return "no response";
        return registration.error() == null ? "registration refused" : registration.error();
    }
}
