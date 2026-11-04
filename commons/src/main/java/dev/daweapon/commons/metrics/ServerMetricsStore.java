package dev.daweapon.commons.metrics;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.redis.RedisConnectionPool;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ServerMetricsStore {
    public static final String KEY_PREFIX = "control:server-metrics:";
    public static final int TTL_SECONDS = 30;
    private static final JacksonSerializer<ServerMetrics> SERIALIZER = new JacksonSerializer<>(ServerMetrics.class);
    private static final RedisConnectionPool.Settings POOL = new RedisConnectionPool.Settings(2, 2, 0, Duration.ofSeconds(2));
    private static JedisPool pool;

    private ServerMetricsStore() {
    }

    public static void publish(ServerMetrics metrics) {
        try (Jedis jedis = pool().getResource()) {
            jedis.setex(KEY_PREFIX + metrics.serverUuid(), TTL_SECONDS, SERIALIZER.serialize(metrics));
        }
    }

    public static List<ServerMetrics> readAll() {
        List<ServerMetrics> found = new ArrayList<>();
        try (Jedis jedis = pool().getResource()) {
            ScanParams params = new ScanParams().match(KEY_PREFIX + "*").count(200);
            String cursor = ScanParams.SCAN_POINTER_START;
            List<String> keys = new ArrayList<>();
            do {
                ScanResult<String> scan = jedis.scan(cursor, params);
                keys.addAll(scan.getResult());
                cursor = scan.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
            if (keys.isEmpty()) return List.of();
            for (String value : jedis.mget(keys.toArray(new String[0]))) {
                if (value == null) continue;
                try {
                    found.add(SERIALIZER.deserialize(value));
                } catch (RuntimeException exception) {
                    Logger.warn("Skipping an unreadable server metrics entry: {}", exception.getMessage());
                }
            }
        }
        found.sort(Comparator.comparing(ServerMetrics::type, Comparator.nullsLast(Comparator.<ServerType>naturalOrder()))
                .thenComparing(ServerMetrics::serverName, Comparator.nullsLast(Comparator.<String>naturalOrder())));
        return found;
    }

    private static synchronized JedisPool pool() {
        if (pool == null) pool = RedisConnectionPool.connect(ConfigProvider.settings().getRedisUri(), POOL);
        return pool;
    }
}
