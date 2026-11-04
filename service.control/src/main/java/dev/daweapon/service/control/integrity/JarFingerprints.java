package dev.daweapon.service.control.integrity;

import com.google.gson.JsonObject;
import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.docker.DockerExec;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetService;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hashes the jar inside every JVM container and compares it with the jar store (capability 15).
 *
 * Hashing happens in place with `sha256sum`, so a 100 MB jar is never copied out of a container.
 * Results are cached because the check reads every jar in the fleet.
 */
public final class JarFingerprints {
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);
    private static final Duration EXEC_BUDGET = Duration.ofSeconds(30);
    private static final String CONTAINER_APP_DIR = "/app";

    public record Report(Instant takenAt, String jarStore, boolean storeAvailable,
                         int skewed, List<JarSkew.Entry> entries) {
    }

    private final DockerApi docker;
    private final DockerExec exec;
    private final FleetService fleetService;
    private final Path jarStore;
    private final HashCache storeHashes = new HashCache();
    private final BackgroundReport<Report> reports;

    public JarFingerprints(DockerApi docker, DockerExec exec, FleetService fleetService, Path jarStore) {
        this.docker = docker;
        this.exec = exec;
        this.fleetService = fleetService;
        this.jarStore = jarStore;
        this.reports = new BackgroundReport<>("version skew", CACHE_TTL, this::build);
    }

    public BackgroundReport.View<Report> view(boolean refresh) {
        return reports.view(refresh);
    }

    public Report latest() {
        return reports.latestOrBuild();
    }

    public void invalidate() {
        reports.invalidate();
    }

    private Report build() {
        List<JarSkew.Fingerprint> fingerprints = new ArrayList<>();
        Map<String, String> stored = new HashMap<>();

        for (ContainerInfo container : fleetService.refresh().containers()) {
            if (container.group() == FleetGroup.UNKNOWN || FleetGroup.isOneShot(container.name())) {
                continue;
            }
            ContainerFacts facts;
            try {
                JsonObject inspect = docker.inspectRaw(container.name());
                if (inspect == null) {
                    continue;
                }
                facts = ContainerFacts.from(container.name(), inspect);
            } catch (DockerException e) {
                Logger.warn("Skew check could not inspect {}: {}", container.name(), e.getMessage());
                continue;
            }
            if (facts.command() == null) {
                continue;
            }
            String jar = facts.command().jar();
            fingerprints.add(fingerprint(facts, jar));
            stored.computeIfAbsent(jar, this::storedHash);
        }

        List<JarSkew.Entry> entries = JarSkew.evaluate(fingerprints, stored);
        int skewed = (int) entries.stream().filter(entry -> entry.state() == JarSkew.State.SKEW).count();
        return new Report(Instant.now(), jarStore.toString(), Files.isDirectory(jarStore), skewed, entries);
    }

    private JarSkew.Fingerprint fingerprint(ContainerFacts facts, String jar) {
        if (!facts.running()) {
            return new JarSkew.Fingerprint(facts.name(), jar, false, null, null);
        }
        String path = CONTAINER_APP_DIR + "/" + jar;
        try {
            DockerExec.Result result = exec.run(facts.name(), List.of("sha256sum", path), EXEC_BUDGET);
            String hash = ConfigDrift.parseSha256sum(result.output(), CONTAINER_APP_DIR).get(jar);
            if (hash == null) {
                return new JarSkew.Fingerprint(facts.name(), jar, true, null,
                        "sha256sum exited " + result.exitCode() + ": " + firstLine(result.output()));
            }
            return new JarSkew.Fingerprint(facts.name(), jar, true, hash, null);
        } catch (DockerException e) {
            return new JarSkew.Fingerprint(facts.name(), jar, true, null, e.getMessage());
        }
    }

    private String storedHash(String jar) {
        Path stored = jarStore.resolve(jar);
        if (!Files.isRegularFile(stored)) {
            return null;
        }
        try {
            return storeHashes.sha256(stored);
        } catch (IOException e) {
            Logger.warn("Could not hash stored jar {}: {}", stored, e.getMessage());
            return null;
        }
    }

    private static String firstLine(String text) {
        if (text == null || text.isBlank()) {
            return "no output";
        }
        String trimmed = text.strip();
        int newline = trimmed.indexOf('\n');
        return newline < 0 ? trimmed : trimmed.substring(0, newline);
    }
}
