package net.swofty.service.control.http;

import com.google.gson.JsonObject;
import net.swofty.service.control.docker.DockerSocketClient;
import net.swofty.service.control.fleet.FleetService;
import net.swofty.service.control.fleet.LifecycleController;
import net.swofty.service.control.fleet.OperationJob;
import net.swofty.service.control.logs.LogNoiseFilter;
import net.swofty.service.control.logs.LogTailer;
import net.swofty.service.control.watchdog.Watchdog;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP surface of the control agent.
 *
 * Every route requires a bearer token even though the agent is only published on the internal
 * compose network. The agent can stop containers, so network isolation alone is not treated as
 * sufficient.
 */
public final class ControlRoutes {
    private final FleetService fleetService;
    private final LifecycleController lifecycle;
    private final LogTailer logTailer;
    private final Watchdog watchdog;
    private final DockerSocketClient socketClient;
    private final String expectedToken;

    public ControlRoutes(FleetService fleetService, LifecycleController lifecycle, LogTailer logTailer,
                         Watchdog watchdog, DockerSocketClient socketClient) {
        this.fleetService = fleetService;
        this.lifecycle = lifecycle;
        this.logTailer = logTailer;
        this.watchdog = watchdog;
        this.socketClient = socketClient;
        this.expectedToken = System.getenv("CONTROL_TOKEN");

        if (expectedToken == null || expectedToken.isBlank()) {
            Logger.error("CONTROL_TOKEN is not set - the control agent will refuse every request. "
                    + "Set it to a shared secret known to service.api.");
        }
    }

    public void register() {
        Spark.before((req, res) -> {
            res.type("application/json");
            if (req.pathInfo().equals("/control/ping")) {
                return;
            }
            if (!isAuthorised(req)) {
                Spark.halt(401, Json.write(Map.of("error", "unauthorized")));
            }
        });

        // Unauthenticated liveness probe for Docker's own healthcheck. Reveals nothing.
        Spark.get("/control/ping", (req, res) -> Json.write(Map.of("ok", true)));

        Spark.get("/control/health", this::health);
        Spark.get("/control/fleet", this::fleet);
        Spark.get("/control/containers/:name/logs", this::logs);

        Spark.post("/control/fleet/start", this::startFleet);
        Spark.post("/control/fleet/stop", this::stopFleet);
        Spark.post("/control/containers/start", (req, res) -> containerAction(req, res, "start"));
        Spark.post("/control/containers/stop", (req, res) -> containerAction(req, res, "stop"));
        Spark.post("/control/containers/restart", (req, res) -> containerAction(req, res, "restart"));

        Spark.get("/control/jobs", (req, res) -> Json.write(lifecycle.recentJobs().stream()
                .map(ControlRoutes::describeJob).toList()));
        Spark.get("/control/jobs/:id", this::job);

        Spark.get("/control/alerts", (req, res) -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("enabled", watchdog.isEnabled());
            payload.put("lastPollAt", watchdog.getLastPollAt());
            payload.put("pollSeconds", watchdog.getPollInterval().toSeconds());
            payload.put("transports", watchdog.getDispatcher().transportNames());
            payload.put("active", watchdog.getFiredConditions());
            payload.put("recent", watchdog.getDispatcher().recent());
            return Json.write(payload);
        });

        Spark.exception(Exception.class, (exception, req, res) -> {
            Logger.error(exception, "Unhandled error on {} {}", req.requestMethod(), req.pathInfo());
            res.type("application/json");
            res.status(500);
            res.body(Json.write(Map.of("error", String.valueOf(exception.getMessage()))));
        });

        Spark.notFound((req, res) -> {
            res.type("application/json");
            return Json.write(Map.of("error", "no such route"));
        });
    }

    private boolean isAuthorised(Request req) {
        if (expectedToken == null || expectedToken.isBlank()) {
            return false;
        }
        String header = req.headers("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return false;
        }
        String presented = header.substring("Bearer ".length()).trim();
        return constantTimeEquals(presented, expectedToken);
    }

    /** Avoids leaking token length or prefix through response timing. */
    private static boolean constantTimeEquals(String a, String b) {
        byte[] left = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] right = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(left, right);
    }

    private String health(Request req, Response res) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("dockerSocket", socketClient.getSocketPath().toString());
        payload.put("dockerAvailable", socketClient.isAvailable());
        payload.put("watchdogEnabled", watchdog.isEnabled());
        payload.put("watchdogLastPollAt", watchdog.getLastPollAt());
        payload.put("operationInFlight", lifecycle.isBusy());
        return Json.write(payload);
    }

    private String fleet(Request req, Response res) {
        boolean force = "true".equalsIgnoreCase(req.queryParams("refresh"));
        return Json.write(force ? fleetService.refresh() : fleetService.snapshot());
    }

    private String logs(Request req, Response res) {
        String name = req.params("name");
        int tail = parseInt(req.queryParams("tail"), 200);
        LogNoiseFilter.Mode mode = LogNoiseFilter.Mode.parse(req.queryParams("mode"));
        return Json.write(logTailer.tail(name, tail, mode));
    }

    private String startFleet(Request req, Response res) {
        JsonObject body = parseBody(req);
        int stagger = optInt(body, "stagger", 0);
        return Json.write(describeJob(lifecycle.startFleet(stagger, actor(req))));
    }

    private String stopFleet(Request req, Response res) {
        return Json.write(describeJob(lifecycle.stopFleet(actor(req))));
    }

    private String containerAction(Request req, Response res, String action) {
        JsonObject body = parseBody(req);
        List<String> names = readNames(body);
        if (names.isEmpty()) {
            res.status(400);
            return Json.write(Map.of("error", "no container names supplied"));
        }

        int stagger = optInt(body, "stagger", 0);
        OperationJob job = switch (action) {
            case "start" -> lifecycle.startContainers(names, stagger, actor(req));
            case "stop" -> lifecycle.stopContainers(names, actor(req));
            default -> lifecycle.restart(names, stagger, actor(req));
        };
        return Json.write(describeJob(job));
    }

    private String job(Request req, Response res) {
        OperationJob job = lifecycle.getJob(req.params("id"));
        if (job == null) {
            res.status(404);
            return Json.write(Map.of("error", "no such job"));
        }
        return Json.write(describeJob(job));
    }

    /** Flattens a job for the wire; OperationJob itself holds concurrency primitives. */
    private static Map<String, Object> describeJob(OperationJob job) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", job.getId());
        payload.put("operation", job.getOperation());
        payload.put("status", job.getStatus().name());
        payload.put("currentStep", job.getCurrentStep());
        payload.put("requestedBy", String.valueOf(job.getRequestedBy()));
        payload.put("startedAt", job.getStartedAt());
        payload.put("finishedAt", job.getFinishedAt());
        payload.put("steps", job.getSteps());
        return payload;
    }

    /** Identity is established by service.api; the agent records whatever it is told. */
    private String actor(Request req) {
        String actor = req.headers("X-Actor");
        return actor == null || actor.isBlank() ? "unknown" : actor;
    }

    private JsonObject parseBody(Request req) {
        String body = req.body();
        if (body == null || body.isBlank()) {
            return new JsonObject();
        }
        try {
            JsonObject parsed = Json.gson().fromJson(body, JsonObject.class);
            return parsed == null ? new JsonObject() : parsed;
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private List<String> readNames(JsonObject body) {
        if (!body.has("names") || !body.get("names").isJsonArray()) {
            return List.of();
        }
        return body.getAsJsonArray("names").asList().stream()
                .filter(element -> !element.isJsonNull())
                .map(com.google.gson.JsonElement::getAsString)
                .filter(name -> !name.isBlank())
                .toList();
    }

    private static int optInt(JsonObject body, String key, int fallback) {
        if (body == null || !body.has(key) || body.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return body.get(key).getAsInt();
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
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
