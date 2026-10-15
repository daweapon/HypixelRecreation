package dev.daweapon.service.control.docker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.ContainerState;
import dev.daweapon.service.control.fleet.FleetGroup;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Typed wrapper over the handful of Docker Engine API calls the control plane needs. */
public final class DockerApi {
    private static final Gson GSON = new Gson();

    private final DockerSocketClient client;

    public DockerApi(DockerSocketClient client) {
        this.client = client;
    }

    public boolean isAvailable() {
        return client.isAvailable();
    }

    /** Liveness check against the daemon itself, distinct from any container's state. */
    public boolean ping() {
        try {
            return client.get("/_ping").isSuccess();
        } catch (DockerException e) {
            return false;
        }
    }

    /**
     * Lists every container belonging to this project, enriched with inspect data.
     *
     * The list endpoint alone carries no restart count and no structured health, so each
     * container is inspected. Inspects run concurrently on virtual threads because a cold
     * sequential pass over ~22 containers is slow enough to be visible in the dashboard.
     */
    public List<ContainerInfo> listProjectContainers() {
        DockerResponse response = client.get("/containers/json?all=true");
        if (!response.isSuccess()) {
            throw new DockerException("Listing containers failed with HTTP " + response.status()
                    + ": " + response.body());
        }

        JsonArray raw = JsonParser.parseString(response.body()).getAsJsonArray();
        List<String> names = new ArrayList<>();
        for (JsonElement element : raw) {
            String name = extractName(element.getAsJsonObject());
            if (name != null && FleetGroup.isProjectContainer(name)) {
                names.add(name);
            }
        }

        List<ContainerInfo> containers = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ContainerInfo>> futures = names.stream()
                    .map(name -> executor.submit(() -> inspect(name)))
                    .toList();
            for (Future<ContainerInfo> future : futures) {
                try {
                    ContainerInfo info = future.get();
                    if (info != null) {
                        containers.add(info);
                    }
                } catch (Exception e) {
                    // A container can disappear between listing and inspecting; skip it rather
                    // than failing the whole snapshot.
                }
            }
        }

        containers.sort(Comparator.comparing((ContainerInfo c) -> c.group().ordinal())
                .thenComparing(ContainerInfo::name));
        return containers;
    }

    public ContainerInfo inspect(String name) {
        DockerResponse response = client.get("/containers/" + encode(name) + "/json");
        if (response.status() == 404) {
            return null;
        }
        if (!response.isSuccess()) {
            throw new DockerException("Inspecting " + name + " failed with HTTP " + response.status());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject stateObject = root.getAsJsonObject("State");

        String dockerState = optString(stateObject, "Status", "unknown");
        int exitCode = stateObject != null && stateObject.has("ExitCode")
                ? stateObject.get("ExitCode").getAsInt() : 0;
        Instant startedAt = parseInstant(optString(stateObject, "StartedAt", null));

        String health = "none";
        if (stateObject != null && stateObject.has("Health")
                && stateObject.get("Health").isJsonObject()) {
            health = optString(stateObject.getAsJsonObject("Health"), "Status", "none");
        }

        int restartCount = root.has("RestartCount") ? root.get("RestartCount").getAsInt() : 0;
        String cleanName = stripLeadingSlash(optString(root, "Name", name));
        String image = root.has("Config") && root.getAsJsonObject("Config").has("Image")
                ? root.getAsJsonObject("Config").get("Image").getAsString() : "unknown";

        ContainerState state = ContainerInfo.deriveState(dockerState, health, restartCount, startedAt);

        return new ContainerInfo(
                optString(root, "Id", ""),
                cleanName,
                image,
                FleetGroup.classify(cleanName),
                state,
                dockerState,
                health,
                restartCount,
                exitCode,
                startedAt,
                buildStatusText(state, dockerState, health, exitCode)
        );
    }

    public void start(String name) {
        act("start", name, "/containers/" + encode(name) + "/start");
    }

    public void stop(String name, int timeoutSeconds) {
        act("stop", name, "/containers/" + encode(name) + "/stop?t=" + timeoutSeconds,
                graceBudget(timeoutSeconds));
    }

    public void restart(String name, int timeoutSeconds) {
        act("restart", name, "/containers/" + encode(name) + "/restart?t=" + timeoutSeconds,
                graceBudget(timeoutSeconds));
    }

    /**
     * Docker withholds its response for the whole stop grace period, so the client must wait
     * longer than the grace it just asked for. The margin covers the container actually
     * starting again after the stop.
     */
    private static Duration graceBudget(int timeoutSeconds) {
        return Duration.ofSeconds(Math.max(timeoutSeconds, 0) + 30L);
    }

    private void act(String verb, String name, String path) {
        act(verb, name, path, null);
    }

    private void act(String verb, String name, String path, Duration budget) {
        DockerResponse response = budget == null ? client.post(path) : client.post(path, budget);
        // 304 means the container was already in the requested state, which is not a failure.
        if (!response.isSuccess() && response.status() != 304) {
            throw new DockerException("Failed to " + verb + " " + name + ": HTTP "
                    + response.status() + " " + response.body());
        }
    }

    /**
     * Fetches the tail of a container's logs.
     *
     * Docker multiplexes stdout and stderr into framed chunks unless the container was started
     * with a TTY, so the raw bytes are de-framed before being returned.
     */
    public String logs(String name, int tail) {
        DockerResponse response = client.get("/containers/" + encode(name)
                + "/logs?stdout=true&stderr=true&timestamps=true&tail=" + tail);
        if (!response.isSuccess()) {
            throw new DockerException("Fetching logs for " + name + " failed with HTTP " + response.status());
        }
        return LogFrameDecoder.decode(response.bodyBytes());
    }

    private static String buildStatusText(ContainerState state, String dockerState, String health, int exitCode) {
        return switch (state) {
            case UP -> health == null || health.equals("none") ? "running" : "running (" + health + ")";
            case WRN -> "running (" + health + ")";
            case LOOP -> "restarting";
            case EXIT -> "exited (" + exitCode + ")";
            case DWN -> dockerState;
        };
    }

    private static String extractName(JsonObject container) {
        if (!container.has("Names")) {
            return null;
        }
        JsonArray names = container.getAsJsonArray("Names");
        if (names.isEmpty()) {
            return null;
        }
        return stripLeadingSlash(names.get(0).getAsString());
    }

    private static String stripLeadingSlash(String value) {
        return value != null && value.startsWith("/") ? value.substring(1) : value;
    }

    private static String optString(JsonObject object, String key, String fallback) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }
        return object.get(key).getAsString();
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isEmpty() || value.startsWith("0001-01-01")) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
