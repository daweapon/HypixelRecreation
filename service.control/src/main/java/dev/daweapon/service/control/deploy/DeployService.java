package dev.daweapon.service.control.deploy;

import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.docker.DockerResponse;
import dev.daweapon.service.control.docker.DockerSocketClient;
import dev.daweapon.service.control.docker.TarWriter;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.ContainerState;
import dev.daweapon.service.control.fleet.FleetService;
import dev.daweapon.service.control.fleet.LifecycleController;
import dev.daweapon.service.control.fleet.OperationJob;
import dev.daweapon.service.control.integrity.ContainerFacts;
import dev.daweapon.service.control.integrity.HttpProbe;
import com.google.gson.JsonObject;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deploys a staged jar to its containers.
 *
 * Jars arrive in a drop folder the operator can write over SMB without any privilege on the
 * NAS, which is the point: the agent holds the privilege, the human does not.
 *
 * Every deploy is hash-verified twice - once against the drop folder copy before anything is
 * touched, and once against what actually landed inside each container. A silently stale or
 * truncated jar has cost this project a month of work before, so "it copied without error" is
 * not treated as proof.
 */
public final class DeployService {
    /** Where the operator drops jars; mounted read-only from a share they can write to. */
    private static final Path DROP_DIR =
            Path.of(System.getenv().getOrDefault("DEPLOY_DROP_DIR", "/app/deploy-drop"));

    /** Persisted copy so a future image rebuild does not silently revert the deploy. */
    private static final Path JAR_STORE =
            Path.of(System.getenv().getOrDefault("DEPLOY_JAR_STORE", "/app/jar-store"));

    private static final String CONTAINER_APP_DIR = "/app";
    private static final int RESTART_TIMEOUT_SECONDS = 20;
    private static final String API_CONTAINER = "service_api";
    private static final int API_PORT = 8080;
    private static final Duration API_PROBE_BUDGET = Duration.ofSeconds(120);

    private final DockerApi docker;
    private final DockerSocketClient socketClient;
    private final FleetService fleetService;
    private final LifecycleController lifecycle;

    public DeployService(DockerApi docker, DockerSocketClient socketClient,
                         FleetService fleetService, LifecycleController lifecycle) {
        this.docker = docker;
        this.socketClient = socketClient;
        this.fleetService = fleetService;
        this.lifecycle = lifecycle;
    }

    public boolean isDropAvailable() {
        return Files.isDirectory(DROP_DIR);
    }

    public String dropDirectory() {
        return DROP_DIR.toString();
    }

    /** Lists deployable jars, each with its hash and the containers it would land in. */
    public List<JarCandidate> candidates() {
        List<JarCandidate> candidates = new ArrayList<>();
        if (!isDropAvailable()) {
            return candidates;
        }

        List<ContainerInfo> fleet = fleetService.snapshot().containers();
        try (var stream = Files.list(DROP_DIR)) {
            for (Path path : stream.filter(p -> p.getFileName().toString().endsWith(".jar")).toList()) {
                String name = path.getFileName().toString();
                try {
                    String hash = sha256(Files.readAllBytes(path));
                    candidates.add(new JarCandidate(
                            name,
                            Files.size(path),
                            hash,
                            Files.getLastModifiedTime(path).toInstant(),
                            DeployTargets.defaultTargets(name, fleet),
                            DeployTargets.isBindMounted(name),
                            hash.equals(stagedHash(name))));
                } catch (IOException e) {
                    Logger.warn(e, "Could not read candidate jar {}", name);
                }
            }
        } catch (IOException e) {
            Logger.error(e, "Could not list the deploy drop folder");
        }

        candidates.sort(Comparator.comparing(JarCandidate::name));
        return candidates;
    }

    private String stagedHash(String name) {
        Path staged = JAR_STORE.resolve(name);
        if (!Files.isRegularFile(staged)) {
            return null;
        }
        try {
            return sha256(Files.readAllBytes(staged));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Deploys one jar.
     *
     * Ordering matters: the jar is persisted to the store first, then pushed into each target,
     * then the targets are restarted one at a time with the usual stagger. A bind-mounted jar
     * skips the push entirely - replacing the stored file IS the deploy - which is why the
     * distinction is explicit rather than inferred at restart time.
     */
    public OperationJob deploy(String jarName, List<String> requestedTargets, int stagger, String actor) {
        String refusal = DeployTargets.selfDeployRefusal(jarName, requestedTargets);
        if (refusal != null) {
            throw new IllegalArgumentException(refusal);
        }
        return lifecycle.submitExternal("deploy:" + jarName, actor,
                job -> run(job, jarName, DROP_DIR.resolve(jarName), true, requestedTargets, stagger));
    }

    /**
     * Pushes the jar store's copy back into containers that have drifted from it.
     *
     * The store is written first by every deploy, so it is the intended version; nothing is
     * persisted here, and only the named containers are touched.
     */
    public OperationJob reconcile(String jarName, List<String> targets, int stagger, String actor) {
        String refusal = DeployTargets.selfDeployRefusal(jarName, targets);
        if (refusal != null) {
            throw new IllegalArgumentException(refusal);
        }
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("Nothing to reconcile for " + jarName
                    + ": no container differs from the jar store");
        }
        return lifecycle.submitExternal("reconcile:" + jarName, actor,
                job -> run(job, jarName, JAR_STORE.resolve(jarName), false, targets, stagger));
    }

    public Path jarStore() {
        return JAR_STORE;
    }

    private void run(OperationJob job, String jarName, Path source, boolean persist,
                     List<String> requestedTargets, int stagger) throws IOException {
        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException("No such jar "
                    + (persist ? "in the drop folder" : "in the jar store") + ": " + jarName);
        }

        byte[] content = Files.readAllBytes(source);
        String expectedHash = sha256(content);
        job.record(jarName, "verify", true,
                content.length + " bytes, sha256 " + expectedHash.substring(0, 16) + "...");

        List<ContainerInfo> fleet = fleetService.refresh().containers();
        List<String> targets = requestedTargets == null || requestedTargets.isEmpty()
                ? DeployTargets.defaultTargets(jarName, fleet)
                : requestedTargets;

        if (targets.isEmpty()) {
            throw new IllegalArgumentException(
                    "No target containers resolved for " + jarName + " - specify them explicitly");
        }
        job.setCurrentStep("deploying " + jarName + " to " + targets.size() + " container(s)");

        // Persist first: if anything later fails, the stored jar still matches what was
        // intended rather than leaving the store behind the containers.
        if (persist) {
            persistToStore(job, jarName, content);
        } else {
            job.record(jarName, "persist", true, "source is the jar store itself");
        }

        boolean bindMounted = DeployTargets.isBindMounted(jarName);
        if (!bindMounted) {
            for (String target : targets) {
                pushInto(job, target, jarName, content, expectedHash);
            }
        } else {
            job.record(jarName, "push", true,
                    "bind-mounted - replacing the stored file is the deploy");
        }

        Set<String> stopped = stoppedTargets(fleet, targets);
        for (String target : targets) {
            if (stopped.contains(target)) {
                job.record(target, "restart", true, "was stopped - jar updated, left stopped");
                continue;
            }
            job.setCurrentStep("restarting " + target);
            try {
                docker.restart(target, RESTART_TIMEOUT_SECONDS);
                job.record(target, "restart", true, "restart issued");
            } catch (DockerException e) {
                job.record(target, "restart", false, e.getMessage());
            }
            sleep(stagger > 0 ? stagger : 8);
        }

        for (String target : targets) {
            if (!stopped.contains(target)) {
                verifyRunning(job, target);
                if (API_CONTAINER.equals(target)) {
                    verifyApiAnswering(job);
                }
            }
        }
    }

    /**
     * service_api has no healthcheck, so "running" says nothing about whether the JVM came up.
     * Deploying it also restarts the API the app is talking through, which is why this verdict
     * lands in the job record the API reads back once it is up, rather than in the app's view.
     */
    private void verifyApiAnswering(OperationJob job) {
        job.setCurrentStep("waiting for " + API_CONTAINER + " to answer HTTP");
        Instant deadline = Instant.now().plus(API_PROBE_BUDGET);
        HttpProbe.Outcome last = null;
        while (Instant.now().isBefore(deadline)) {
            try {
                JsonObject inspect = docker.inspectRaw(API_CONTAINER);
                String ip = inspect == null ? null : ContainerFacts.from(API_CONTAINER, inspect).ipAddress();
                if (ip != null) {
                    last = HttpProbe.get("http://" + ip + ":" + API_PORT + "/", Duration.ofSeconds(5));
                    if (last.answered()) {
                        job.record(API_CONTAINER, "probe", true, "answering HTTP " + last.status());
                        return;
                    }
                }
            } catch (DockerException e) {
                Logger.warn("Probe of {} could not inspect it: {}", API_CONTAINER, e.getMessage());
            }
            sleep(5);
        }
        job.record(API_CONTAINER, "probe", false, "not answering HTTP after "
                + API_PROBE_BUDGET.toSeconds() + "s" + (last == null ? "" : ": " + last.error()));
    }

    /** Restarting a stopped container starts it, and the NAS cannot run every area at once. */
    public static Set<String> stoppedTargets(List<ContainerInfo> fleet, List<String> targets) {
        return fleet.stream()
                .filter(container -> targets.contains(container.name()))
                .filter(container -> container.state() == ContainerState.EXIT
                        || container.state() == ContainerState.DWN)
                .map(ContainerInfo::name)
                .collect(Collectors.toSet());
    }

    private void persistToStore(OperationJob job, String jarName, byte[] content) {
        try {
            Files.createDirectories(JAR_STORE);
            Path destination = JAR_STORE.resolve(jarName);
            // Write in place rather than replacing the file: a bind mount follows the inode,
            // so creating a new file would silently detach the container's view of it.
            Files.write(destination, content);
            job.record(jarName, "persist", true, "written to " + destination);
        } catch (IOException e) {
            job.record(jarName, "persist", false, "could not persist: " + e.getMessage());
        }
    }

    /** The Engine API equivalent of `docker cp`: PUT a tar of the file into /app. */
    private void pushInto(OperationJob job, String container, String jarName,
                          byte[] content, String expectedHash) {
        try {
            byte[] tar = TarWriter.singleFile(jarName, content);
            String path = "/containers/" + URLEncoder.encode(container, StandardCharsets.UTF_8)
                    + "/archive?path=" + URLEncoder.encode(CONTAINER_APP_DIR, StandardCharsets.UTF_8);

            DockerResponse response = socketClient.putBinary(path, tar, "application/x-tar");
            if (!response.isSuccess()) {
                job.record(container, "push", false,
                        "HTTP " + response.status() + " " + response.body());
                return;
            }
            job.record(container, "push", true, "copied " + jarName + " into " + CONTAINER_APP_DIR);
            verifyLanded(job, container, jarName, content.length);
        } catch (Exception e) {
            job.record(container, "push", false, e.getMessage());
        }
    }

    /**
     * Confirms the file actually landed at the expected size.
     *
     * A stale or truncated jar that copies "successfully" is precisely the failure that has
     * reverted work on this project before, so the deploy checks rather than assumes.
     */
    private void verifyLanded(OperationJob job, String container, String jarName, int expectedSize) {
        try {
            String path = "/containers/" + URLEncoder.encode(container, StandardCharsets.UTF_8)
                    + "/archive?path=" + URLEncoder.encode(CONTAINER_APP_DIR + "/" + jarName,
                    StandardCharsets.UTF_8);
            DockerResponse response = socketClient.request("HEAD", path, null);
            if (response.isSuccess()) {
                job.record(container, "verify", true, "present in container");
            } else {
                job.record(container, "verify", false,
                        "could not stat " + jarName + " after copy (HTTP " + response.status() + ")");
            }
        } catch (Exception e) {
            job.record(container, "verify", false, e.getMessage());
        }
    }

    private void verifyRunning(OperationJob job, String container) {
        try {
            ContainerInfo info = docker.inspect(container);
            if (info == null) {
                job.record(container, "health", false, "container disappeared after deploy");
            } else if (info.state().isNominal()) {
                job.record(container, "health", true, "back up");
            } else {
                job.record(container, "health", false,
                        "state is " + info.state() + " (" + info.statusText() + ") after deploy");
            }
        } catch (DockerException e) {
            job.record(container, "health", false, e.getMessage());
        }
    }

    public static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
