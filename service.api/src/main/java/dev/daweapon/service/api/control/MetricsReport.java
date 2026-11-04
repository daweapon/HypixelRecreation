package dev.daweapon.service.api.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.daweapon.commons.metrics.ServerMetrics;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public final class MetricsReport {
    private MetricsReport() {
    }

    public static JsonObject build(List<ServerMetrics> servers, long now) {
        JsonArray rows = new JsonArray();
        servers.forEach(server -> rows.add(row(server, now)));

        JsonObject report = new JsonObject();
        report.addProperty("takenAt", Instant.ofEpochMilli(now).toString());
        report.add("servers", rows);
        report.add("summary", summary(servers, now));
        return report;
    }

    static JsonObject row(ServerMetrics server, long now) {
        JsonObject row = new JsonObject();
        row.addProperty("serverUuid", server.serverUuid());
        row.addProperty("serverName", server.serverName());
        row.addProperty("shortName", server.shortName());
        row.addProperty("type", server.type() == null ? null : server.type().name());
        row.addProperty("host", server.host());
        row.addProperty("tps", server.tps());
        row.addProperty("mspt", server.mspt());
        row.addProperty("heapUsedMb", server.heapUsedMb());
        row.addProperty("heapMaxMb", server.heapMaxMb());
        row.addProperty("players", server.players());
        row.addProperty("maxPlayers", server.maxPlayers());
        row.addProperty("publishedAt", Instant.ofEpochMilli(server.publishedAt()).toString());
        row.addProperty("ageMillis", server.ageMillis(now));
        row.addProperty("health", server.health(now).name());
        return row;
    }

    static JsonObject summary(List<ServerMetrics> servers, long now) {
        JsonObject summary = new JsonObject();
        summary.addProperty("servers", servers.size());
        summary.addProperty("players", servers.stream().mapToInt(ServerMetrics::players).sum());
        summary.addProperty("heapUsedMb", servers.stream().mapToLong(ServerMetrics::heapUsedMb).sum());
        summary.addProperty("heapMaxMb", servers.stream().mapToLong(ServerMetrics::heapMaxMb).sum());
        summary.addProperty("loaded", servers.stream().filter(s -> s.health(now) == ServerMetrics.Health.LOADED).count());
        summary.addProperty("failing", servers.stream().filter(s -> s.health(now) == ServerMetrics.Health.FAILING).count());
        summary.addProperty("worstHealth", servers.stream().map(s -> s.health(now))
                .max(Comparator.naturalOrder()).orElse(ServerMetrics.Health.NOMINAL).name());
        servers.stream().min(Comparator.comparingDouble(ServerMetrics::tps)).ifPresent(lowest -> {
            summary.addProperty("lowestTps", lowest.tps());
            summary.addProperty("lowestTpsServer", lowest.serverName());
        });
        servers.stream().mapToDouble(ServerMetrics::mspt).max()
                .ifPresent(highest -> summary.addProperty("highestMspt", highest));
        return summary;
    }
}
