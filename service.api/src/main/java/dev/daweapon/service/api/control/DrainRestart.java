package dev.daweapon.service.api.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.commons.metrics.ServerMetrics;
import dev.daweapon.commons.protocol.objects.game.DrainServerPushProtocol;
import dev.daweapon.commons.redis.RedisClient;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Restarts a game server only after its players have been sent elsewhere.
 *
 * The game server moves its own players through the normal proxy transfer, which saves their
 * data on the way out. The restart is issued once the server's heartbeat reports nobody left,
 * or when the timeout passes, whichever comes first.
 */
public final class DrainRestart {
    public static final int DEFAULT_TIMEOUT_SECONDS = 45;
    public static final int MIN_TIMEOUT_SECONDS = 5;
    public static final int MAX_TIMEOUT_SECONDS = 120;
    private static final long POLL_MILLIS = 2000;
    private static final int KEPT_JOBS = 30;
    private static final DrainServerPushProtocol DRAIN = new DrainServerPushProtocol();
    private static final Map<String, Job> JOBS = new LinkedHashMap<>();

    public enum Phase {
        DRAINING,
        RESTARTING,
        DONE,
        FAILED
    }

    public static final class Job {
        private final String id = UUID.randomUUID().toString();
        private final String container;
        private final int timeoutSeconds;
        private final Instant startedAt = Instant.now();
        private final List<String> log = new ArrayList<>();
        private volatile Phase phase = Phase.DRAINING;
        private volatile int playersAtStart = -1;
        private volatile int playersLeft = -1;
        private volatile @Nullable String destination;
        private volatile @Nullable String agentJobId;

        Job(String container, int timeoutSeconds) {
            this.container = container;
            this.timeoutSeconds = timeoutSeconds;
        }

        public String id() {
            return id;
        }

        public Phase phase() {
            return phase;
        }

        synchronized void note(String line) {
            log.add(line);
        }

        public synchronized JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("container", container);
            json.addProperty("phase", phase.name());
            json.addProperty("timeoutSeconds", timeoutSeconds);
            json.addProperty("startedAt", startedAt.toString());
            json.addProperty("playersAtStart", playersAtStart);
            json.addProperty("playersLeft", playersLeft);
            json.addProperty("destination", destination);
            json.addProperty("agentJobId", agentJobId);
            JsonArray lines = new JsonArray();
            log.forEach(lines::add);
            json.add("log", lines);
            return json;
        }
    }

    private DrainRestart() {
    }

    public static int clampTimeout(int requested) {
        if (requested <= 0) return DEFAULT_TIMEOUT_SECONDS;
        return Math.max(MIN_TIMEOUT_SECONDS, Math.min(MAX_TIMEOUT_SECONDS, requested));
    }

    public static Optional<ServerMetrics> serverFor(List<ServerMetrics> heartbeats, String containerId, String containerName) {
        return heartbeats.stream().filter(metrics -> metrics.belongsToContainer(containerId, containerName)).findFirst();
    }

    public static @Nullable String containerId(JsonObject fleet, String name) {
        JsonElement containers = fleet.get("containers");
        if (containers == null || !containers.isJsonArray()) return null;
        for (JsonElement element : containers.getAsJsonArray()) {
            JsonObject container = element.getAsJsonObject();
            if (container.has("name") && name.equals(container.get("name").getAsString())) {
                return container.has("id") && !container.get("id").isJsonNull() ? container.get("id").getAsString() : "";
            }
        }
        return null;
    }

    public static synchronized @Nullable Job find(String id) {
        return JOBS.get(id);
    }

    public static synchronized List<Job> recent() {
        List<Job> jobs = new ArrayList<>(JOBS.values());
        Collections.reverse(jobs);
        return jobs;
    }

    public static Job start(String container, int timeoutSeconds, ControlAuth.Caller caller, ControlAgentClient agent) {
        Job job = new Job(container, clampTimeout(timeoutSeconds));
        synchronized (DrainRestart.class) {
            JOBS.put(job.id(), job);
            while (JOBS.size() > KEPT_JOBS) JOBS.remove(JOBS.keySet().iterator().next());
        }
        Thread.startVirtualThread(() -> run(job, caller, agent, LiveGameData::readMetrics));
        return job;
    }

    static void run(Job job, ControlAuth.Caller caller, ControlAgentClient agent, Supplier<List<ServerMetrics>> heartbeats) {
        String actor = caller.actor();
        try {
            ControlAgentClient.AgentResponse fleetResponse = agent.get("/control/fleet", actor);
            if (!fleetResponse.isSuccess()) {
                finish(job, caller, false, "could not read the fleet: agent returned HTTP " + fleetResponse.status());
                return;
            }
            String containerId = containerId(JsonParser.parseString(fleetResponse.body()).getAsJsonObject(), job.container);
            if (containerId == null) {
                finish(job, caller, false, "no container named " + job.container);
                return;
            }

            Optional<ServerMetrics> server = serverFor(heartbeats.get(), containerId, job.container);
            if (server.isEmpty()) {
                job.note("No game heartbeat from " + job.container + ", so there are no players to move");
                job.playersAtStart = 0;
                job.playersLeft = 0;
            } else {
                drain(job, server.get(), heartbeats);
            }

            job.phase = Phase.RESTARTING;
            JsonObject body = new JsonObject();
            JsonArray names = new JsonArray();
            names.add(job.container);
            body.add("names", names);
            ControlAgentClient.AgentResponse restart = agent.post("/control/containers/restart", body.toString(), actor);
            if (!restart.isSuccess()) {
                finish(job, caller, false, "agent refused the restart: HTTP " + restart.status() + " " + restart.body());
                return;
            }
            job.agentJobId = agentJobId(restart.body());
            finish(job, caller, true, "restart dispatched" + (job.agentJobId == null ? "" : " as agent job " + job.agentJobId));
        } catch (Exception exception) {
            Logger.error(exception, "Drain and restart of {} failed", job.container);
            finish(job, caller, false, "failed: " + exception.getMessage());
        }
    }

    private static void drain(Job job, ServerMetrics server, Supplier<List<ServerMetrics>> heartbeats) throws Exception {
        UUID uuid = UUID.fromString(server.serverUuid());
        job.playersAtStart = server.players();
        if (server.players() == 0) {
            job.playersLeft = 0;
            job.note(server.serverName() + " is already empty");
            return;
        }
        DrainServerPushProtocol.Response response = RedisClient.requestServerFromService(uuid, DRAIN,
                new DrainServerPushProtocol.Request("Scheduled Reboot")).get(10, TimeUnit.SECONDS);
        job.destination = response.destination();
        job.note("Sending " + response.players() + " player(s) from " + server.serverName() + " to " + response.destination());

        long deadline = System.nanoTime() + Duration.ofSeconds(job.timeoutSeconds).toNanos();
        int left = response.players();
        while (System.nanoTime() < deadline) {
            Thread.sleep(POLL_MILLIS);
            Optional<ServerMetrics> latest = heartbeats.get().stream()
                    .filter(metrics -> metrics.serverUuid().equals(server.serverUuid())).findFirst();
            if (latest.isEmpty()) {
                job.note("Heartbeat stopped while draining");
                break;
            }
            left = latest.get().players();
            job.playersLeft = left;
            if (left == 0) break;
        }
        job.playersLeft = left;
        job.note(left == 0 ? "Server is empty" : "Timed out with " + left + " player(s) still connected");
    }

    static @Nullable String agentJobId(String body) {
        try {
            JsonElement parsed = JsonParser.parseString(body);
            if (parsed.isJsonObject() && parsed.getAsJsonObject().has("id")) {
                return parsed.getAsJsonObject().get("id").getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void finish(Job job, ControlAuth.Caller caller, boolean ok, String detail) {
        job.note(detail);
        job.phase = ok ? Phase.DONE : Phase.FAILED;
        String summary = "moved " + Math.max(0, job.playersAtStart) + " player(s)"
                + (job.destination == null ? "" : " to " + job.destination)
                + ", " + Math.max(0, job.playersLeft) + " left at restart; " + detail;
        AuditDatabase.record(AuditEntry.of(
                caller.device().playerName(),
                caller.device().deviceId(),
                caller.device().deviceName(),
                caller.role().name(),
                "drain-restart",
                List.of(job.container),
                ok ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                summary));
    }
}
