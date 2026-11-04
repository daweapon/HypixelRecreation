package dev.daweapon.service.api.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.metrics.ServerMetrics;
import dev.daweapon.commons.metrics.ServerMetricsStore;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.OrchestratorQueueProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.OrchestratorStatusProtocol;
import dev.daweapon.commons.redis.RedisClient;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Reads live game state for the control app: capacity and the waiting queue come from the
 * orchestrator over Redis, per-server metrics from the heartbeats game servers publish.
 */
public final class LiveGameData {
    private static final long TIMEOUT_SECONDS = 5;
    private static final OrchestratorStatusProtocol STATUS = new OrchestratorStatusProtocol();
    private static final OrchestratorQueueProtocol QUEUE = new OrchestratorQueueProtocol();

    private LiveGameData() {
    }

    public static JsonObject capacity() {
        CompletableFuture<OrchestratorStatusProtocol.StatusResponse> status =
                ask(STATUS, new OrchestratorStatusProtocol.StatusRequest());
        CompletableFuture<OrchestratorQueueProtocol.QueueResponse> queue =
                ask(QUEUE, new OrchestratorQueueProtocol.QueueRequest());
        OrchestratorStatusProtocol.StatusResponse statusResponse = await(status);
        OrchestratorQueueProtocol.QueueResponse queueResponse = await(queue);

        JsonObject capacity = new JsonObject();
        capacity.addProperty("takenAt", Instant.now().toString());
        capacity.addProperty("orchestratorOnline", statusResponse != null);
        capacity.add("types", statusResponse == null ? new JsonArray()
                : field(STATUS.translateReturnToString(statusResponse), "types"));
        capacity.add("waiting", queueResponse == null ? new JsonArray()
                : field(QUEUE.translateReturnToString(queueResponse), "waiting"));
        return capacity;
    }

    public static JsonObject metrics() {
        return MetricsReport.build(readMetrics(), System.currentTimeMillis());
    }

    public static List<ServerMetrics> readMetrics() {
        try {
            return ServerMetricsStore.readAll();
        } catch (Exception exception) {
            Logger.warn("Could not read server metrics from Redis: {}", exception.getMessage());
            return List.of();
        }
    }

    private static JsonElement field(String json, String name) {
        JsonElement element = JsonParser.parseString(json).getAsJsonObject().get(name);
        return element == null ? new JsonArray() : element;
    }

    private static <T, R> CompletableFuture<R> ask(RedisProtocol<T, R> protocol, T request) {
        return RedisClient.requestService(ServiceType.ORCHESTRATOR, protocol, request)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static <R> @Nullable R await(CompletableFuture<R> future) {
        try {
            return future.join();
        } catch (Exception exception) {
            Logger.warn("Orchestrator did not answer the control app: {}", exception.getMessage());
            return null;
        }
    }
}
