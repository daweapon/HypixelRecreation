package dev.daweapon.service.api.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The control-plane checks that need service.api's Redis and Mongo, run on a timer.
 *
 * 1. Proxy registration: asks the proxy which servers it holds, merges that with the agent's
 *    game servers, and pushes the DELISTED set to the agent so its watchdog alerts on it.
 * 2. Post-restart verdicts: records every finished deploy, reconcile, config sync and agent
 *    self-restart in the audit log, including the ones that restarted this process.
 * 3. Agent outage: records the agent becoming unreachable, which is the one failure the
 *    agent cannot record about itself.
 */
public final class ControlIntegrityMonitor {
    private static final Duration POLL = Duration.ofSeconds(30);
    private static final Duration VERDICT_WINDOW = Duration.ofHours(6);
    private static final Duration UNREACHABLE_GRACE = Duration.ofMinutes(3);
    private static final String ACTOR = "control-plane";

    private static final ControlAgentClient AGENT = new ControlAgentClient();
    private static final Set<String> recordedJobs = ConcurrentHashMap.newKeySet();

    private static volatile boolean verdictsLoaded;
    private static volatile Instant agentDownSince;
    private static volatile boolean agentOutageRecorded;
    private static ScheduledExecutorService scheduler;

    private ControlIntegrityMonitor() {
    }

    public static synchronized void start() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "control-integrity-monitor");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(ControlIntegrityMonitor::safeTick, 45, POLL.toSeconds(), TimeUnit.SECONDS);
    }

    private static void safeTick() {
        try {
            recordVerdicts();
        } catch (Exception e) {
            Logger.warn("Deploy verdict check failed: {}", e.getMessage());
        }
        try {
            evaluateRegistrations(ACTOR);
        } catch (Exception e) {
            Logger.warn("Proxy registration check failed: {}", e.getMessage());
        }
    }

    /** Fetches both sides, merges them, and hands the DELISTED set to the agent's watchdog. */
    public static ProxyRegistrations.Result evaluateRegistrations(String actor) {
        ControlAgentClient.AgentResponse response = AGENT.get("/control/integrity/servers", actor);
        if (!response.isSuccess()) {
            throw new IllegalStateException("agent returned HTTP " + response.status() + ": " + response.body());
        }
        List<ProxyRegistrations.Server> servers = parseServers(JsonParser.parseString(response.body()).getAsJsonArray());

        List<ProxyRegistrations.Registration> registrations = null;
        String proxyError = null;
        try {
            registrations = ProxyRegistrationProbe.fetch();
        } catch (Exception e) {
            proxyError = e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName();
        }

        ProxyRegistrations.Result result = ProxyRegistrations.match(servers, registrations, proxyError, Instant.now());

        JsonObject report = new JsonObject();
        report.addProperty("proxyReachable", result.proxyReachable());
        JsonArray delisted = new JsonArray();
        result.delisted().forEach(delisted::add);
        report.add("delisted", delisted);
        AGENT.post("/control/integrity/registrations", report.toString(), actor);
        return result;
    }

    static List<ProxyRegistrations.Server> parseServers(JsonArray array) {
        List<ProxyRegistrations.Server> servers = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject server = element.getAsJsonObject();
            servers.add(new ProxyRegistrations.Server(
                    string(server, "name"),
                    string(server, "state"),
                    instant(string(server, "startedAt")),
                    string(server, "id") == null ? "" : string(server, "id"),
                    string(server, "hostname"),
                    string(server, "serverType")));
        }
        return servers;
    }

    private static void recordVerdicts() {
        ControlAgentClient.AgentResponse response = AGENT.get("/control/jobs", ACTOR);
        if (!response.isSuccess()) {
            noteAgentDown(response);
            return;
        }
        noteAgentUp();

        if (!verdictsLoaded) {
            AuditDatabase.recent(500, DeployVerdicts.AUDIT_ACTION, null).forEach(entry -> {
                if (!entry.targets().isEmpty()) {
                    recordedJobs.add(entry.targets().getFirst());
                }
            });
            verdictsLoaded = true;
        }

        JsonArray jobs = JsonParser.parseString(response.body()).getAsJsonArray();
        for (DeployVerdicts.Verdict verdict : DeployVerdicts.pending(jobs, recordedJobs, Instant.now(), VERDICT_WINDOW)) {
            AuditDatabase.record(AuditEntry.of(ACTOR, "system", "service.api", null, DeployVerdicts.AUDIT_ACTION,
                    verdict.targets(), verdict.ok() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                    verdict.detail()));
            recordedJobs.add(verdict.jobId());
        }
    }

    private static void noteAgentDown(ControlAgentClient.AgentResponse response) {
        if (agentDownSince == null) {
            agentDownSince = Instant.now();
        }
        if (!agentOutageRecorded && Duration.between(agentDownSince, Instant.now()).compareTo(UNREACHABLE_GRACE) >= 0) {
            AuditDatabase.record(AuditEntry.of(ACTOR, "system", "service.api", null, "control-agent-unreachable",
                    List.of("service_control"), AuditEntry.Outcome.FAILED,
                    "unreachable since " + agentDownSince + ": HTTP " + response.status() + " " + response.body()));
            agentOutageRecorded = true;
        }
    }

    private static void noteAgentUp() {
        if (agentOutageRecorded) {
            AuditDatabase.record(AuditEntry.of(ACTOR, "system", "service.api", null, "control-agent-recovered",
                    List.of("service_control"), AuditEntry.Outcome.ACCEPTED,
                    "reachable again after an outage that began " + agentDownSince));
        }
        agentDownSince = null;
        agentOutageRecorded = false;
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }

    private static Instant instant(String value) {
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
