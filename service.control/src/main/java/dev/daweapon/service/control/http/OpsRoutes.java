package dev.daweapon.service.control.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.daweapon.service.control.backup.BackupRecord;
import dev.daweapon.service.control.backup.BackupSchedule;
import dev.daweapon.service.control.backup.BackupService;
import dev.daweapon.service.control.backup.RestoreService;
import dev.daweapon.service.control.fleet.OperationJob;
import dev.daweapon.service.control.triage.TriageService;
import dev.daweapon.service.control.watchdog.Alert;
import dev.daweapon.service.control.watchdog.Watchdog;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Backups, test alerts and exception triage.
 *
 * Registered after {@link ControlRoutes}, whose global bearer-token filter covers these routes
 * too. Role checks and the audit trail live in service.api, as for every other agent route.
 */
public final class OpsRoutes {
    private final BackupService backups;
    private final RestoreService restores;
    private final Watchdog watchdog;
    private final TriageService triage;

    public OpsRoutes(BackupService backups, RestoreService restores, Watchdog watchdog, TriageService triage) {
        this.backups = backups;
        this.restores = restores;
        this.watchdog = watchdog;
        this.triage = triage;
    }

    public void register() {
        Spark.get("/control/backups", (req, res) -> Json.write(backups.status()));
        Spark.post("/control/backups/run", this::runBackup);
        Spark.post("/control/backups/settings", this::updateSettings);
        Spark.post("/control/backups/restore/preview", (req, res) -> restore(req, res, true));
        Spark.post("/control/backups/restore", (req, res) -> restore(req, res, false));

        Spark.post("/control/alerts/test", this::testAlert);
        Spark.get("/control/exceptions", this::exceptions);
    }

    private String runBackup(Request req, Response res) {
        try {
            BackupRecord started = backups.startBackup(BackupRecord.Trigger.MANUAL, actor(req));
            return Json.write(started);
        } catch (IllegalStateException e) {
            res.status(409);
            return Json.write(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    private String updateSettings(Request req, Response res) {
        JsonObject body = parseBody(req);
        try {
            String spec = optString(body, "schedule", null);
            BackupSchedule schedule = spec == null ? backups.schedule() : BackupSchedule.parse(spec);
            int retain = body.has("retain") ? body.get("retain").getAsInt() : backups.retain();
            backups.updateSettings(schedule, retain);
            return Json.write(backups.status());
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            res.status(400);
            return Json.write(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    private String restore(Request req, Response res, boolean preview) {
        JsonObject body = parseBody(req);
        String id = optString(body, "id", null);
        List<String> parts = readStrings(body, "parts");
        try {
            RestoreService.Mode mode = RestoreService.Mode.parse(optString(body, "mode", null));
            if (preview) {
                return Json.write(restores.preview(id, parts, mode));
            }
            OperationJob job = restores.restore(id, parts, mode, actor(req));
            return Json.write(describeJob(job));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return Json.write(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    private String testAlert(Request req, Response res) {
        JsonObject body = parseBody(req);
        String message = optString(body, "message", null);
        Alert alert = Alert.info("test:" + System.currentTimeMillis(), "Test alert from the control app",
                message == null || message.isBlank()
                        ? "Sent by " + actor(req) + ". If this reached your phone, alerting works."
                        : message);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("alert", alert);
        payload.put("results", watchdog.getDispatcher().dispatchReporting(alert));
        payload.put("suppressedNow", watchdog.isSuppressed());
        return Json.write(payload);
    }

    private String exceptions(Request req, Response res) {
        int tail = parseInt(req.queryParams("tail"), TriageService.DEFAULT_TAIL);
        boolean noise = "true".equalsIgnoreCase(req.queryParams("noise"));
        boolean refresh = "true".equalsIgnoreCase(req.queryParams("refresh"));
        return Json.write(triage.scan(tail, noise, refresh));
    }

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

    private static String actor(Request req) {
        String actor = req.headers("X-Actor");
        return actor == null || actor.isBlank() ? "unknown" : actor;
    }

    private static JsonObject parseBody(Request req) {
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

    private static String optString(JsonObject body, String key, String fallback) {
        if (!body.has(key) || body.get(key).isJsonNull()) {
            return fallback;
        }
        return body.get(key).getAsString();
    }

    private static List<String> readStrings(JsonObject body, String key) {
        if (!body.has(key) || !body.get(key).isJsonArray()) {
            return List.of();
        }
        return body.getAsJsonArray(key).asList().stream()
                .filter(element -> !element.isJsonNull())
                .map(JsonElement::getAsString)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
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
