package dev.daweapon.service.control.triage;

import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.fleet.ContainerInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Scans recent logs of every project container and groups what went wrong. Read-only.
 *
 * A scan reads thousands of lines from every container, so results are cached briefly; the app
 * can force a fresh scan.
 */
public final class TriageService {
    public static final int DEFAULT_TAIL = 2000;
    private static final int MAX_TAIL = 5000;
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    public record Result(ExceptionTriage.Report report, int tail, boolean includeNoise,
                         List<String> containersScanned, Map<String, String> errors) {
    }

    private final DockerApi docker;
    private final List<String> projectPackages;
    private volatile Result cached;

    public TriageService(DockerApi docker) {
        this.docker = docker;
        String configured = System.getenv("TRIAGE_PROJECT_PACKAGES");
        this.projectPackages = configured == null || configured.isBlank()
                ? ExceptionTriage.DEFAULT_PROJECT_PACKAGES
                : Arrays.stream(configured.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public synchronized Result scan(int requestedTail, boolean includeNoise, boolean refresh) {
        int tail = Math.min(Math.max(requestedTail, 50), MAX_TAIL);
        Result previous = cached;
        if (!refresh && previous != null && previous.tail() == tail && previous.includeNoise() == includeNoise
                && Duration.between(previous.report().scannedAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return previous;
        }

        List<String> names = docker.listProjectContainers().stream().map(ContainerInfo::name).toList();
        Map<String, Future<String>> logs = new LinkedHashMap<>();
        Map<String, String> errors = new LinkedHashMap<>();
        ExceptionTriage triage = new ExceptionTriage(projectPackages, includeNoise);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String name : names) {
                logs.put(name, executor.submit(() -> docker.logs(name, tail)));
            }
            for (Map.Entry<String, Future<String>> entry : logs.entrySet()) {
                try {
                    triage.scan(entry.getKey(), entry.getValue().get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    errors.put(entry.getKey(), String.valueOf(cause.getMessage()));
                }
            }
        }

        Result result = new Result(triage.report(), tail, includeNoise, names, errors);
        cached = result;
        return result;
    }
}
