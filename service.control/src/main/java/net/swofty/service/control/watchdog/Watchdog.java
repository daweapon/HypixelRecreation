package net.swofty.service.control.watchdog;

import net.swofty.service.control.fleet.ContainerInfo;
import net.swofty.service.control.fleet.ContainerState;
import net.swofty.service.control.fleet.DependencyStatus;
import net.swofty.service.control.fleet.FleetService;
import net.swofty.service.control.fleet.FleetSnapshot;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Watches the fleet and raises alerts on state transitions.
 *
 * This is the capability the whole control plane is justified by: Mongo and Redis sat EXITED for
 * two weeks and the proxy for ten days, discovered only by chance. A dashboard nobody opens
 * cannot catch that, so detection has to run server-side and push.
 *
 * Alerts fire on transitions only, and a condition must persist for several consecutive polls
 * before it alerts. Without that, every routine staggered restart would page the operator.
 */
public final class Watchdog {
    private static final int DEFAULT_POLL_SECONDS = 30;
    private static final int DEFAULT_CONFIRM_POLLS = 3;

    private final Supplier<FleetSnapshot> snapshotSource;
    private final AlertDispatcher dispatcher;
    private final int pollSeconds;
    private final int confirmPolls;

    /** How many consecutive polls each currently-bad condition has been observed for. */
    private final Map<String, Integer> pendingConditions = new HashMap<>();
    /** Conditions already alerted on, awaiting recovery. */
    private final Set<String> firedConditions = ConcurrentHashMap.newKeySet();
    /** Restart count per container as of the previous poll, for detecting an active loop. */
    private final Map<String, Integer> lastRestartCounts = new HashMap<>();

    private ScheduledExecutorService scheduler;
    private volatile Instant lastPollAt;
    private volatile boolean enabled = true;

    public Watchdog(FleetService fleetService, AlertDispatcher dispatcher) {
        this(fleetService::refresh, dispatcher, readInt("WATCHDOG_POLL_SECONDS", DEFAULT_POLL_SECONDS),
                readInt("WATCHDOG_CONFIRM_POLLS", DEFAULT_CONFIRM_POLLS));
    }

    public Watchdog(Supplier<FleetSnapshot> snapshotSource, AlertDispatcher dispatcher,
                    int pollSeconds, int confirmPolls) {
        this.snapshotSource = snapshotSource;
        this.dispatcher = dispatcher;
        this.pollSeconds = pollSeconds;
        this.confirmPolls = confirmPolls;
    }

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "control-watchdog");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::safePoll, pollSeconds, pollSeconds, TimeUnit.SECONDS);
        Logger.info("Watchdog started: polling every {}s, confirming over {} polls", pollSeconds, confirmPolls);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Logger.info("Watchdog {}", enabled ? "enabled" : "muted");
    }

    public Instant getLastPollAt() {
        return lastPollAt;
    }

    public Set<String> getFiredConditions() {
        return Set.copyOf(firedConditions);
    }

    public AlertDispatcher getDispatcher() {
        return dispatcher;
    }

    private void safePoll() {
        try {
            poll();
        } catch (Exception e) {
            // The watchdog must survive anything the fleet throws at it; a dead watchdog is
            // indistinguishable from a healthy stack, which is the exact failure being guarded against.
            Logger.error(e, "Watchdog poll failed");
        }
    }

    /** Runs one evaluation immediately. Exposed so a check can be forced, and for tests. */
    public void poll() {
        lastPollAt = Instant.now();
        if (!enabled) {
            return;
        }

        FleetSnapshot snapshot = snapshotSource.get();
        Map<String, Alert> currentlyBad = new HashMap<>();

        if (!snapshot.dockerReachable()) {
            currentlyBad.put("docker:unreachable", Alert.critical("docker:unreachable",
                    "Control agent cannot reach Docker",
                    snapshot.dockerError() == null ? "Docker socket unavailable." : snapshot.dockerError()));
        } else {
            collectDependencyAlerts(snapshot, currentlyBad);
            collectLoopAlerts(snapshot, currentlyBad);
        }

        promoteAndFire(currentlyBad);
        clearRecovered(currentlyBad.keySet());
    }

    private void collectDependencyAlerts(FleetSnapshot snapshot, Map<String, Alert> currentlyBad) {
        for (DependencyStatus dependency : snapshot.dependencies()) {
            if (dependency.isNominal()) {
                continue;
            }
            String key = "dependency:" + dependency.dependency().name();
            String name = dependency.dependency().getDisplayName();
            currentlyBad.put(key, Alert.critical(key,
                    name + " is " + dependency.state(),
                    name + " (" + dependency.statusText() + ") is not available. Every game server "
                            + "and service depending on it will retry-loop and drive CPU high across "
                            + "the whole stack until it is back."));
        }
    }

    /**
     * Flags containers that are actively cycling.
     *
     * Two independent signals: Docker reporting the container as "restarting", or its restart
     * count rising between polls. The second is what catches a fast crash-loop, where a poll
     * often lands while the container happens to be briefly "running".
     *
     * The absolute restart count is never used - it is cumulative over the container's whole
     * life, so it would page on a routine manual restart of anything with past incidents.
     */
    private void collectLoopAlerts(FleetSnapshot snapshot, Map<String, Alert> currentlyBad) {
        for (ContainerInfo container : snapshot.containers()) {
            Integer previous = lastRestartCounts.put(container.name(), container.restartCount());
            boolean restartsRising = previous != null && container.restartCount() > previous;

            if (container.state() != ContainerState.LOOP && !restartsRising) {
                continue;
            }

            String key = "loop:" + container.name();
            String why = restartsRising
                    ? "Restart count rose from " + previous + " to " + container.restartCount()
                            + " since the last check."
                    : "Docker reports it as restarting.";
            currentlyBad.put(key, Alert.warning(key,
                    container.name() + " is restarting in a loop",
                    why + " Check its logs for a startup failure."));
        }

        // Forget containers that have gone away, so the map cannot grow without bound.
        Set<String> present = new HashSet<>();
        for (ContainerInfo container : snapshot.containers()) {
            present.add(container.name());
        }
        lastRestartCounts.keySet().retainAll(present);
    }

    /** Counts how long each condition has held and fires once it clears the confirmation threshold. */
    private void promoteAndFire(Map<String, Alert> currentlyBad) {
        for (Map.Entry<String, Alert> entry : currentlyBad.entrySet()) {
            String key = entry.getKey();
            if (firedConditions.contains(key)) {
                continue;
            }
            int observed = pendingConditions.merge(key, 1, Integer::sum);
            if (observed >= confirmPolls) {
                dispatcher.dispatch(entry.getValue());
                firedConditions.add(key);
                pendingConditions.remove(key);
            }
        }
    }

    /** Drops conditions that are no longer true, and announces recovery for ones that had fired. */
    private void clearRecovered(Set<String> stillBad) {
        pendingConditions.keySet().removeIf(key -> !stillBad.contains(key));

        for (String key : new HashSet<>(firedConditions)) {
            if (stillBad.contains(key)) {
                continue;
            }
            firedConditions.remove(key);
            dispatcher.dispatch(Alert.recovery(key, describeRecovery(key), "Condition cleared."));
        }
    }

    private String describeRecovery(String key) {
        int separator = key.indexOf(':');
        String subject = separator >= 0 ? key.substring(separator + 1) : key;
        return subject + " recovered";
    }

    public Duration getPollInterval() {
        return Duration.ofSeconds(pollSeconds);
    }

    private static int readInt(String envName, int fallback) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
