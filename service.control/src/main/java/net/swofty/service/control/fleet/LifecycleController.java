package net.swofty.service.control.fleet;

import net.swofty.service.control.docker.DockerApi;
import net.swofty.service.control.docker.DockerException;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Staggered start, stop and restart across the fleet.
 *
 * Ports the ordering and timing of start-servers.sh, with two deliberate changes:
 *
 * 1. A health-wait timeout no longer aborts the run. The shell script runs under `set -euo
 *    pipefail`, so wait_for_health returning non-zero kills the whole script - on slower
 *    hardware a first-boot JVM can exceed the 90s budget and the game servers then never get
 *    created at all. Here a timeout is recorded against the step and the sequence continues.
 * 2. JVM containers are never started in parallel. Simultaneous starts are what caused the
 *    vmmem CPU spikes, so the stagger is enforced in code rather than left to the caller.
 */
public final class LifecycleController {
    private static final int DEFAULT_STAGGER_SECONDS = 8;
    private static final int DEFAULT_HEALTH_TIMEOUT_SECONDS = 180;
    private static final int HEALTH_POLL_SECONDS = 5;
    private static final int STOP_TIMEOUT_SECONDS = 20;

    private final DockerApi docker;
    private final Map<String, OperationJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean operationInFlight = new AtomicBoolean(false);

    public LifecycleController(DockerApi docker) {
        this.docker = docker;
    }

    public OperationJob getJob(String id) {
        return jobs.get(id);
    }

    public List<OperationJob> recentJobs() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(OperationJob::getStartedAt).reversed())
                .limit(20)
                .toList();
    }

    public boolean isBusy() {
        return operationInFlight.get();
    }

    /**
     * Starts the whole fleet in dependency order.
     *
     * Phases mirror start-servers.sh: databases, then proxy, then services in batches, then
     * game servers one at a time.
     */
    public OperationJob startFleet(int staggerSeconds, String requestedBy) {
        return submit("start-fleet", requestedBy, job -> {
            List<ContainerInfo> containers = docker.listProjectContainers();
            int stagger = staggerSeconds > 0 ? staggerSeconds : DEFAULT_STAGGER_SECONDS;

            for (Map.Entry<String, List<String>> phase : buildStartPhases(containers).entrySet()) {
                job.setCurrentStep(phase.getKey());
                Logger.info("Fleet start phase: {}", phase.getKey());

                for (String container : phase.getValue()) {
                    startOne(job, container);
                    sleep(stagger);
                }

                for (String container : phase.getValue()) {
                    awaitHealth(job, container, DEFAULT_HEALTH_TIMEOUT_SECONDS);
                }
            }
        });
    }

    /** Stops the fleet in reverse dependency order so dependants go first. */
    public OperationJob stopFleet(String requestedBy) {
        return submit("stop-fleet", requestedBy, job -> {
            List<ContainerInfo> containers = docker.listProjectContainers();
            List<Map.Entry<String, List<String>>> phases =
                    new ArrayList<>(buildStartPhases(containers).entrySet());
            java.util.Collections.reverse(phases);

            for (Map.Entry<String, List<String>> phase : phases) {
                job.setCurrentStep("stopping " + phase.getKey());
                for (String container : phase.getValue()) {
                    stopOne(job, container);
                }
            }
        });
    }

    /** Restarts a chosen set of containers, staggered. */
    public OperationJob restart(List<String> containerNames, int staggerSeconds, String requestedBy) {
        return submit("restart", requestedBy, job -> {
            int stagger = staggerSeconds > 0 ? staggerSeconds : DEFAULT_STAGGER_SECONDS;
            for (String container : containerNames) {
                job.setCurrentStep("restarting " + container);
                try {
                    docker.restart(container, STOP_TIMEOUT_SECONDS);
                    job.record(container, "restart", true, "restart issued");
                } catch (DockerException e) {
                    job.record(container, "restart", false, e.getMessage());
                }
                sleep(stagger);
            }
            for (String container : containerNames) {
                awaitHealth(job, container, DEFAULT_HEALTH_TIMEOUT_SECONDS);
            }
        });
    }

    public OperationJob startContainers(List<String> containerNames, int staggerSeconds, String requestedBy) {
        return submit("start", requestedBy, job -> {
            int stagger = staggerSeconds > 0 ? staggerSeconds : DEFAULT_STAGGER_SECONDS;
            for (String container : containerNames) {
                job.setCurrentStep("starting " + container);
                startOne(job, container);
                sleep(stagger);
            }
            for (String container : containerNames) {
                awaitHealth(job, container, DEFAULT_HEALTH_TIMEOUT_SECONDS);
            }
        });
    }

    public OperationJob stopContainers(List<String> containerNames, String requestedBy) {
        return submit("stop", requestedBy, job -> {
            for (String container : containerNames) {
                job.setCurrentStep("stopping " + container);
                stopOne(job, container);
            }
        });
    }

    /**
     * Groups containers into ordered start phases.
     *
     * Derived from the live fleet rather than a hardcoded container list, because the generated
     * compose on the NAS carries a different set of servers than the repo's reference compose.
     */
    private Map<String, List<String>> buildStartPhases(List<ContainerInfo> containers) {
        Map<String, List<String>> phases = new LinkedHashMap<>();

        List<String> databases = new ArrayList<>();
        List<String> proxy = new ArrayList<>();
        List<String> otherCore = new ArrayList<>();
        List<String> services = new ArrayList<>();
        List<String> gameServers = new ArrayList<>();

        for (ContainerInfo container : containers) {
            String name = container.name();
            SharedDependency dependency = SharedDependency.of(name);

            if (dependency == SharedDependency.MONGO || dependency == SharedDependency.REDIS) {
                databases.add(name);
            } else if (dependency == SharedDependency.PROXY) {
                proxy.add(name);
            } else if (container.group() == FleetGroup.SERVICE) {
                services.add(name);
            } else if (container.group() == FleetGroup.GAME_SERVER) {
                gameServers.add(name);
            } else if (container.group() == FleetGroup.CORE
                    && !name.equals("game_server_builder")) {
                // game_server_builder is a one-shot image builder, not a long-lived container.
                otherCore.add(name);
            }
        }

        databases.sort(Comparator.naturalOrder());
        services.sort(Comparator.naturalOrder());
        otherCore.sort(Comparator.naturalOrder());
        gameServers.sort(gameServerOrder());

        putIfNotEmpty(phases, "databases", databases);
        putIfNotEmpty(phases, "proxy", proxy);
        putIfNotEmpty(phases, "services", services);
        putIfNotEmpty(phases, "supporting", otherCore);
        putIfNotEmpty(phases, "game servers", gameServers);
        return phases;
    }

    /** Prototype lobby first - it is the required server players land on. */
    private Comparator<String> gameServerOrder() {
        return Comparator.comparingInt((String name) -> name.contains("prototype_lobby") ? 0 : 1)
                .thenComparingInt(name -> name.contains("hub") ? 0 : 1)
                .thenComparing(Comparator.naturalOrder());
    }

    private void putIfNotEmpty(Map<String, List<String>> phases, String name, List<String> containers) {
        if (!containers.isEmpty()) {
            phases.put(name, containers);
        }
    }

    private void startOne(OperationJob job, String container) {
        try {
            docker.start(container);
            job.record(container, "start", true, "start issued");
        } catch (DockerException e) {
            job.record(container, "start", false, e.getMessage());
        }
    }

    private void stopOne(OperationJob job, String container) {
        try {
            docker.stop(container, STOP_TIMEOUT_SECONDS);
            job.record(container, "stop", true, "stop issued");
        } catch (DockerException e) {
            job.record(container, "stop", false, e.getMessage());
        }
    }

    /**
     * Waits for a container to report healthy or running.
     *
     * A timeout here is recorded and tolerated - never fatal to the surrounding sequence.
     */
    private void awaitHealth(OperationJob job, String container, int timeoutSeconds) {
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            try {
                ContainerInfo info = docker.inspect(container);
                if (info == null) {
                    job.record(container, "health", false, "container disappeared");
                    return;
                }
                if (info.state() == ContainerState.UP) {
                    job.record(container, "health", true, "healthy after "
                            + Duration.between(deadline.minusSeconds(timeoutSeconds), Instant.now()).toSeconds() + "s");
                    // The call may have overrun its budget and still taken effect.
                    for (String action : new String[]{"restart", "start", "stop"}) {
                        job.resolve(container, action, "confirmed by health check");
                    }
                    return;
                }
                if (info.state() == ContainerState.EXIT) {
                    job.record(container, "health", false, "exited with code " + info.exitCode());
                    return;
                }
            } catch (DockerException e) {
                job.record(container, "health", false, e.getMessage());
                return;
            }
            sleep(HEALTH_POLL_SECONDS);
        }
        job.record(container, "health", false,
                "did not become ready within " + timeoutSeconds + "s - continuing anyway");
    }

    /** Body of a long-running fleet operation. */
    public interface JobBody {
        void run(OperationJob job) throws Exception;
    }

    /**
     * Runs an externally-defined operation under the same one-at-a-time guard.
     *
     * A deploy restarts containers, so it must not interleave with a staggered fleet start -
     * that would defeat the stagger, which is the exact condition that causes the CPU spike.
     */
    public OperationJob submitExternal(String operation, String requestedBy, JobBody body) {
        return submit(operation, requestedBy, body);
    }

    /**
     * Runs one fleet operation at a time. Concurrent staggered operations would defeat the
     * stagger entirely, which is the exact condition that causes the CPU spike.
     */
    private OperationJob submit(String operation, String requestedBy, JobBody body) {
        OperationJob job = new OperationJob(UUID.randomUUID().toString(), operation, requestedBy);

        if (!operationInFlight.compareAndSet(false, true)) {
            job.reject("another fleet operation is already running");
            jobs.put(job.getId(), job);
            return job;
        }

        jobs.put(job.getId(), job);
        executor.submit(() -> {
            try {
                body.run(job);
                job.finish();
            } catch (Exception e) {
                Logger.error(e, "Fleet operation {} failed", operation);
                job.fail(e.getMessage() == null ? e.toString() : e.getMessage());
            } finally {
                operationInFlight.set(false);
            }
        });
        return job;
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
