package dev.daweapon.service.control.integrity;

import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.docker.DockerExec;
import dev.daweapon.service.control.docker.DockerResponse;
import dev.daweapon.service.control.docker.DockerSocketClient;
import dev.daweapon.service.control.docker.TarWriter;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.ContainerState;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetService;
import dev.daweapon.service.control.fleet.LifecycleController;
import dev.daweapon.service.control.fleet.OperationJob;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Config-drift detector (capability 4): each game server's /app/configuration against the host
 * configuration directory, plus a sync that pushes the host copies in over the Engine API.
 *
 * The host tree reaches the agent through a read-only bind mount. When that mount is absent the
 * report says so and names the compose line, rather than comparing against the configuration
 * baked into the agent's own image, which would look authoritative and be wrong.
 */
public final class ConfigDriftService {
    public static final String COMPOSE_LINE = "- ./configuration:/app/host-configuration:ro";

    private static final String CONTAINER_CONFIG_DIR = "/app/configuration";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration HASH_BUDGET = Duration.ofSeconds(120);
    private static final Duration PUSH_BUDGET = Duration.ofSeconds(180);
    private static final long MAX_SYNC_BYTES = 256L * 1024 * 1024;
    private static final int RESTART_TIMEOUT_SECONDS = 20;

    public enum Status {
        CLEAN,
        DRIFTED,
        STOPPED,
        UNREADABLE
    }

    public record ContainerDrift(String container, Status status, String scope, int changed,
                                 int missing, int extra, List<ConfigDrift.FileDrift> files, String error) {
    }

    public record Report(Instant takenAt, boolean hostMounted, String hostDirectory, String composeLine,
                         int hostFiles, Set<String> ignored, List<ContainerDrift> containers) {
    }

    private final DockerApi docker;
    private final DockerExec exec;
    private final DockerSocketClient socketClient;
    private final FleetService fleetService;
    private final LifecycleController lifecycle;
    private final Path hostDirectory;
    private final Set<String> ignored;
    private final String packServer;
    private final HashCache hostHashes = new HashCache();
    private final AtomicReference<Report> cached = new AtomicReference<>();

    public ConfigDriftService(DockerApi docker, DockerExec exec, DockerSocketClient socketClient,
                              FleetService fleetService, LifecycleController lifecycle) {
        this.docker = docker;
        this.exec = exec;
        this.socketClient = socketClient;
        this.fleetService = fleetService;
        this.lifecycle = lifecycle;
        this.hostDirectory = Path.of(System.getenv().getOrDefault("CONFIG_HOST_DIR", "/app/host-configuration"));
        // config.yml is rewritten inside each container by entrypoint.sh (the velocity secret),
        // so it always differs from the host and is never worth reporting.
        this.ignored = Arrays.stream(System.getenv().getOrDefault("CONFIG_DRIFT_IGNORE",
                        "config.yml,config.example.yml").split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.packServer = System.getenv().getOrDefault("PACK_SERVER_CONTAINER", "hypixel_pack_server");
    }

    public boolean isHostMounted() {
        return Files.isDirectory(hostDirectory);
    }

    public Report report(boolean refresh) {
        Report current = cached.get();
        if (!refresh && current != null
                && Duration.between(current.takenAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return current;
        }
        Report fresh = build();
        cached.set(fresh);
        return fresh;
    }

    private Report build() {
        if (!isHostMounted()) {
            return new Report(Instant.now(), false, hostDirectory.toString(), COMPOSE_LINE, 0, ignored, List.of());
        }

        Map<String, String> host;
        try {
            host = hashHost();
        } catch (IOException e) {
            Logger.error(e, "Could not hash the host configuration at {}", hostDirectory);
            return new Report(Instant.now(), false, hostDirectory.toString(), COMPOSE_LINE, 0, ignored, List.of());
        }

        List<ContainerDrift> results = new ArrayList<>();
        for (ContainerInfo container : targets()) {
            results.add(inspectOne(container, host));
        }
        return new Report(Instant.now(), true, hostDirectory.toString(), COMPOSE_LINE, host.size(), ignored, results);
    }

    /** Game servers read the whole tree; the pack server overlays only resourcepacks/. */
    private List<ContainerInfo> targets() {
        return fleetService.refresh().containers().stream()
                .filter(container -> container.group() == FleetGroup.GAME_SERVER
                        || container.name().equals(packServer))
                .toList();
    }

    private String scopeOf(String container) {
        return container.equals(packServer) ? "resourcepacks" : "";
    }

    private ContainerDrift inspectOne(ContainerInfo container, Map<String, String> host) {
        String scope = scopeOf(container.name());
        if (container.state() == ContainerState.EXIT || container.state() == ContainerState.DWN) {
            return new ContainerDrift(container.name(), Status.STOPPED, scope, 0, 0, 0, List.of(),
                    "not running; it copies the host configuration in when it next starts");
        }
        try {
            String root = scope.isEmpty() ? CONTAINER_CONFIG_DIR : CONTAINER_CONFIG_DIR + "/" + scope;
            DockerExec.Result result = exec.run(container.name(), List.of("sh", "-c",
                    "find " + root + " -type f -exec sha256sum {} +"), HASH_BUDGET);
            Map<String, String> inside = ConfigDrift.parseSha256sum(result.output(), CONTAINER_CONFIG_DIR);
            if (inside.isEmpty() && !result.ok()) {
                return new ContainerDrift(container.name(), Status.UNREADABLE, scope, 0, 0, 0, List.of(),
                        "hashing exited " + result.exitCode());
            }

            List<ConfigDrift.FileDrift> files = ConfigDrift.compare(host, inside, ConfigDrift.scopeFor(scope), ignored);
            int changed = count(files, ConfigDrift.Kind.CHANGED);
            int missing = count(files, ConfigDrift.Kind.MISSING_IN_CONTAINER);
            int extra = count(files, ConfigDrift.Kind.EXTRA_IN_CONTAINER);
            return new ContainerDrift(container.name(), files.isEmpty() ? Status.CLEAN : Status.DRIFTED,
                    scope, changed, missing, extra, files, null);
        } catch (DockerException e) {
            return new ContainerDrift(container.name(), Status.UNREADABLE, scope, 0, 0, 0, List.of(), e.getMessage());
        }
    }

    private static int count(List<ConfigDrift.FileDrift> files, ConfigDrift.Kind kind) {
        return (int) files.stream().filter(file -> file.kind() == kind).count();
    }

    private Map<String, String> hashHost() throws IOException {
        Map<String, String> hashes = new LinkedHashMap<>();
        Set<Path> seen = new HashSet<>();
        try (Stream<Path> files = Files.walk(hostDirectory)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                seen.add(file);
                String relative = hostDirectory.relativize(file).toString().replace('\\', '/');
                try {
                    hashes.put(relative, hostHashes.sha256(file));
                } catch (IOException e) {
                    Logger.warn("Skipping unreadable host config file {}: {}", file, e.getMessage());
                }
            }
        }
        hostHashes.retainOnly(seen);
        return hashes;
    }

    /**
     * Pushes every syncable drifted file into each named container (all drifted ones when none
     * are named), optionally restarting them afterwards.
     *
     * Without a restart the files match but the running JVM still holds the values it read at
     * boot, so the app defaults to restarting; stopped containers are never started.
     */
    public OperationJob sync(List<String> names, boolean restart, int stagger, String actor) {
        if (!isHostMounted()) {
            throw new IllegalArgumentException("The host configuration is not mounted into the agent. "
                    + "Add '" + COMPOSE_LINE + "' to service_control's volumes and recreate it.");
        }
        return lifecycle.submitExternal("config-sync", actor, job -> {
            Report report = report(true);
            List<ContainerDrift> chosen = report.containers().stream()
                    .filter(drift -> drift.status() == Status.DRIFTED)
                    .filter(drift -> names == null || names.isEmpty() || names.contains(drift.container()))
                    .toList();
            if (chosen.isEmpty()) {
                job.record("configuration", "compare", true, "no drifted container to sync");
                return;
            }

            List<String> pushed = new ArrayList<>();
            for (ContainerDrift drift : chosen) {
                job.setCurrentStep("syncing configuration into " + drift.container());
                if (pushFiles(job, drift)) {
                    pushed.add(drift.container());
                }
                if (drift.extra() > 0) {
                    job.record(drift.container(), "extra", true, drift.extra()
                            + " file(s) deleted on the host remain inside; only a recreate removes them");
                }
            }

            if (restart) {
                // The pack server goes first: game servers advertise the hash of the pack they
                // build at boot, and a pack server still serving the old one kicks every player.
                pushed.sort(java.util.Comparator.comparingInt(name -> name.equals(packServer) ? 0 : 1));
                for (String container : pushed) {
                    job.setCurrentStep("restarting " + container);
                    try {
                        docker.restart(container, RESTART_TIMEOUT_SECONDS);
                        job.record(container, "restart", true, "restart issued");
                    } catch (DockerException e) {
                        job.record(container, "restart", false, e.getMessage());
                    }
                    sleep(stagger > 0 ? stagger : 8);
                }
                for (String container : pushed) {
                    verifyRunning(job, container);
                }
            }
            cached.set(null);
        });
    }

    private boolean pushFiles(OperationJob job, ContainerDrift drift) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        long total = 0;
        try {
            for (ConfigDrift.FileDrift file : drift.files()) {
                if (!file.syncable()) {
                    continue;
                }
                byte[] content = Files.readAllBytes(hostDirectory.resolve(file.path()));
                total += content.length;
                if (total > MAX_SYNC_BYTES) {
                    job.record(drift.container(), "push", false, "more than "
                            + (MAX_SYNC_BYTES / 1048576) + " MB drifted; restart the container instead, "
                            + "its overlay copies the host tree in at boot");
                    return false;
                }
                entries.put(file.path(), content);
            }
            if (entries.isEmpty()) {
                return false;
            }

            byte[] tar = TarWriter.files(entries);
            String path = "/containers/" + URLEncoder.encode(drift.container(), StandardCharsets.UTF_8)
                    + "/archive?path=" + URLEncoder.encode(CONTAINER_CONFIG_DIR, StandardCharsets.UTF_8);
            DockerResponse response = socketClient.putBinary(path, tar, "application/x-tar", PUSH_BUDGET);
            if (!response.isSuccess()) {
                job.record(drift.container(), "push", false, "HTTP " + response.status() + " " + response.body());
                return false;
            }
            job.record(drift.container(), "push", true, "copied " + entries.size() + " file(s), "
                    + (total / 1024) + " KB into " + CONTAINER_CONFIG_DIR);
            return true;
        } catch (IOException | IllegalArgumentException | DockerException e) {
            job.record(drift.container(), "push", false, e.getMessage());
            return false;
        }
    }

    private void verifyRunning(OperationJob job, String container) {
        Instant deadline = Instant.now().plusSeconds(180);
        while (Instant.now().isBefore(deadline)) {
            try {
                ContainerInfo info = docker.inspect(container);
                if (info != null && info.state() == ContainerState.UP) {
                    job.record(container, "health", true, "back up");
                    return;
                }
                if (info == null || info.state() == ContainerState.EXIT) {
                    job.record(container, "health", false, info == null ? "container disappeared"
                            : "exited with code " + info.exitCode());
                    return;
                }
            } catch (DockerException e) {
                job.record(container, "health", false, e.getMessage());
                return;
            }
            sleep(5);
        }
        job.record(container, "health", false, "not healthy within 180s");
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
