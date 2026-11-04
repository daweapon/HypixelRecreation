package dev.daweapon.service.control.integrity;

import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.fleet.LifecycleController;
import dev.daweapon.service.control.http.Json;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Records whether the agent came back after restarting itself or having its jar replaced.
 *
 * The agent cannot watch its own restart, so it leaves a marker before one and settles it on
 * the next boot, as a finished "verify:ServiceControl.jar" job in the ordinary job history.
 * service.api reads that history and writes the verdict to the audit log. A jar swapped by
 * hand through DSM is caught the same way, by the jar's hash differing from the last boot.
 * If the agent never comes back there is no boot to record it, which service.api reports
 * separately as the agent going unreachable.
 */
public final class BootVerifier {
    public record State(String jarSha256, Instant bootedAt, String pendingReason, String pendingBy,
                        Instant pendingAt) {
    }

    /** What a boot should record, or null when there is nothing to verify. */
    public record Verdict(String requestedBy, String detail) {
    }

    private final Path stateFile;
    private final Path selfJar;

    public BootVerifier(Path jarStore) {
        this.stateFile = Path.of(System.getenv().getOrDefault("CONTROL_STATE_FILE",
                jarStore.resolve(".control-agent-state.json").toString()));
        this.selfJar = Path.of(System.getenv().getOrDefault("CONTROL_SELF_JAR", "/app/ServiceControl.jar"));
    }

    public static Verdict judge(State previous, String currentSha256) {
        if (previous == null) {
            return null;
        }
        boolean jarChanged = previous.jarSha256() != null && currentSha256 != null
                && !previous.jarSha256().equals(currentSha256);
        if (previous.pendingReason() == null && !jarChanged) {
            return null;
        }
        String detail = jarChanged
                ? "jar changed " + shortHash(previous.jarSha256()) + " -> " + shortHash(currentSha256)
                : "same jar " + shortHash(currentSha256);
        if (previous.pendingReason() != null) {
            detail = previous.pendingReason() + " requested at " + previous.pendingAt() + "; " + detail;
        }
        return new Verdict(previous.pendingBy() == null ? "boot" : previous.pendingBy(), detail);
    }

    public void verifyOnBoot(LifecycleController lifecycle, DockerApi docker) {
        String current = currentJarHash();
        State previous = read();
        Verdict verdict = judge(previous, current);
        write(new State(current, Instant.now(), null, null, null));
        if (verdict == null) {
            return;
        }
        lifecycle.submitExternal("verify:ServiceControl.jar", verdict.requestedBy(), job -> {
            job.record("service_control", "boot", true, "agent is back up: " + verdict.detail());
            boolean dockerOk = docker.isAvailable() && docker.ping();
            job.record("service_control", "docker", dockerOk,
                    dockerOk ? "Docker socket reachable" : "Docker socket NOT reachable after restart");
        });
    }

    /** Called before anything restarts the agent's own container. */
    public void markPending(String reason, String requestedBy) {
        State previous = read();
        write(new State(previous == null ? currentJarHash() : previous.jarSha256(),
                previous == null ? null : previous.bootedAt(), reason, requestedBy, Instant.now()));
    }

    private String currentJarHash() {
        try {
            return Files.isRegularFile(selfJar) ? HashCache.digest(selfJar, "SHA-256") : null;
        } catch (IOException e) {
            Logger.warn("Could not hash the agent's own jar {}: {}", selfJar, e.getMessage());
            return null;
        }
    }

    private State read() {
        try {
            return Files.isRegularFile(stateFile) ? Json.read(Files.readString(stateFile), State.class) : null;
        } catch (Exception e) {
            Logger.warn("Unreadable agent state file {}: {}", stateFile, e.getMessage());
            return null;
        }
    }

    private void write(State state) {
        try {
            Files.writeString(stateFile, Json.write(state));
        } catch (IOException e) {
            Logger.warn("Could not write agent state file {} - post-restart verification is off: {}",
                    stateFile, e.getMessage());
        }
    }

    private static String shortHash(String hash) {
        return hash == null ? "unknown" : hash.substring(0, Math.min(12, hash.length()));
    }
}
