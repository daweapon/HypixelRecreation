package dev.daweapon.service.api.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns finished agent jobs that change code or configuration into audit verdicts.
 *
 * A deploy of ServiceAPI.jar restarts this very process, and a restart of the agent kills the
 * job that asked for it, so neither verdict can be written by whoever started it. Instead the
 * next running API reads the agent's job history and records each verdict once.
 */
public final class DeployVerdicts {
    public static final String AUDIT_ACTION = "deploy-verify";

    private static final List<String> VERIFIED_PREFIXES = List.of("deploy:", "reconcile:", "verify:", "config-sync");

    public record Verdict(String jobId, String operation, boolean ok, String detail, List<String> targets) {
    }

    private DeployVerdicts() {
    }

    public static List<Verdict> pending(JsonArray jobs, Set<String> alreadyRecorded, Instant now, Duration window) {
        List<Verdict> verdicts = new ArrayList<>();
        for (JsonElement element : jobs) {
            JsonObject job = element.getAsJsonObject();
            String id = string(job, "id");
            String operation = string(job, "operation");
            String status = string(job, "status");
            if (id == null || operation == null || alreadyRecorded.contains(id)
                    || VERIFIED_PREFIXES.stream().noneMatch(operation::startsWith)
                    || status == null || status.equals("RUNNING") || status.equals("REJECTED")) {
                continue;
            }
            Instant finishedAt = parse(string(job, "finishedAt"));
            if (finishedAt == null || Duration.between(finishedAt, now).compareTo(window) > 0) {
                continue;
            }
            verdicts.add(judge(id, operation, status, job));
        }
        return verdicts;
    }

    private static Verdict judge(String id, String operation, String status, JsonObject job) {
        Set<String> targets = new LinkedHashSet<>();
        targets.add(id);
        targets.add(operation);
        List<String> failures = new ArrayList<>();
        JsonArray steps = job.has("steps") && job.get("steps").isJsonArray() ? job.getAsJsonArray("steps") : new JsonArray();
        for (JsonElement element : steps) {
            JsonObject step = element.getAsJsonObject();
            String container = string(step, "container");
            if (container != null) {
                targets.add(container);
            }
            boolean ok = step.has("ok") && step.get("ok").getAsBoolean();
            if (!ok && failures.size() < 3) {
                failures.add(container + " " + string(step, "action") + ": " + string(step, "message"));
            }
        }

        boolean ok = status.equals("SUCCEEDED");
        String detail = ok ? "OK: " + operation + " finished and every step verified"
                : "FAILED: " + operation + " ended " + status
                + (failures.isEmpty() ? " - " + string(job, "currentStep") : " - " + String.join("; ", failures));
        return new Verdict(id, operation, ok, detail, List.copyOf(targets));
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }

    private static Instant parse(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
