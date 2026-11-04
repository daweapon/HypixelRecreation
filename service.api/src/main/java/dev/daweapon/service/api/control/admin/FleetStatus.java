package dev.daweapon.service.api.control.admin;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.OnlinePlayer;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.StatusRequest;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.StatusResponse;
import dev.daweapon.commons.redis.RedisClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Asks every game server over Redis who is online and what date it shows. There is no other
// cross-server roster: the proxy and friend service only answer per-player questions.
public final class FleetStatus {
    public record ServerReport(UUID serverId, StatusResponse status) {
        public boolean isSkyBlock() {
            return status.calendar() != null;
        }
    }

    public record Located(ServerReport server, OnlinePlayer player) {}

    private static final AdminServerStatusProtocol PROTOCOL = new AdminServerStatusProtocol();
    private static final int BROADCAST_TIMEOUT_MS = 1500;
    private static final long CACHE_MILLIS = 3000;

    private static volatile List<ServerReport> cached;
    private static volatile long cachedAt;

    private FleetStatus() {}

    public static List<ServerReport> snapshot(boolean fresh) throws Exception {
        List<ServerReport> current = cached;
        if (!fresh && current != null && System.currentTimeMillis() - cachedAt < CACHE_MILLIS) return current;

        Map<UUID, StatusResponse> responses = broadcast(PROTOCOL, new StatusRequest(), BROADCAST_TIMEOUT_MS);
        List<ServerReport> reports = new ArrayList<>();
        responses.forEach((server, status) -> {
            if (status != null) reports.add(new ServerReport(server, status));
        });
        reports.sort(Comparator.comparing((ServerReport report) -> report.status().serverType())
                .thenComparing(report -> String.valueOf(report.status().serverName())));

        cached = List.copyOf(reports);
        cachedAt = System.currentTimeMillis();
        return cached;
    }

    public static Optional<Located> locate(UUID player) throws Exception {
        for (ServerReport report : snapshot(true)) {
            for (OnlinePlayer online : report.status().players()) {
                if (online.uuid().equals(player)) return Optional.of(new Located(report, online));
            }
        }
        return Optional.empty();
    }

    public static Optional<ServerReport> pickSkyBlockServer() throws Exception {
        List<ServerReport> skyblock = snapshot(false).stream().filter(ServerReport::isSkyBlock).toList();
        return skyblock.stream().filter(report -> report.status().serverType().equals("SKYBLOCK_HUB")).findFirst()
                .or(() -> skyblock.stream().findFirst());
    }

    public static <T, R> Map<UUID, R> broadcast(RedisProtocol<T, R> protocol, T message, int timeoutMs) throws Exception {
        return RedisClient.requestAllServersFromService(protocol, message, timeoutMs)
                .get(timeoutMs + 3000L, TimeUnit.MILLISECONDS);
    }

    public static <T, R> R ask(UUID server, RedisProtocol<T, R> protocol, T message, long timeoutMs) throws Exception {
        return RedisClient.requestServerFromService(server, protocol, message).get(timeoutMs, TimeUnit.MILLISECONDS);
    }
}
