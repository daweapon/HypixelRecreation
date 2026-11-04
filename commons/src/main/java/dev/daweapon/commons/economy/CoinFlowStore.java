package dev.daweapon.commons.economy;

import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.redis.RedisConnectionPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CoinFlowStore {
    public static final String FLOW_PREFIX = "economy:flow:";
    public static final String COUNT_PREFIX = "economy:flowcount:";
    public static final String SUPPLY_KEY = "economy:supply";
    public static final String RECEIVED = " (received)";
    public static final String PAID = " (paid)";
    private static final long RETENTION_SECONDS = Duration.ofDays(400).toSeconds();
    private static final RedisConnectionPool.Settings POOL = new RedisConnectionPool.Settings(2, 2, 0, Duration.ofSeconds(2));
    private static JedisPool pool;

    public enum Kind { SOURCE, SINK, TRANSFER, STAFF }

    public record Delta(double amount, long count) {}

    public record Day(LocalDate date, Map<String, Delta> flows) {}

    public record Supply(LocalDate date, double purse, double bank, int profiles) {
        public double total() {
            return purse + bank;
        }
    }

    private CoinFlowStore() {
    }

    public static String field(Kind kind, String category) {
        return kind.name() + "|" + category;
    }

    public static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    public static void add(LocalDate date, Map<String, Delta> deltas) {
        if (deltas.isEmpty()) return;
        String flowKey = FLOW_PREFIX + date;
        String countKey = COUNT_PREFIX + date;
        try (Jedis jedis = pool().getResource()) {
            Pipeline pipeline = jedis.pipelined();
            deltas.forEach((field, delta) -> {
                pipeline.hincrByFloat(flowKey, field, delta.amount());
                pipeline.hincrBy(countKey, field, delta.count());
            });
            pipeline.expire(flowKey, RETENTION_SECONDS);
            pipeline.expire(countKey, RETENTION_SECONDS);
            pipeline.sync();
        }
    }

    public static List<Day> readDays(int days) {
        List<Day> found = new ArrayList<>();
        LocalDate today = today();
        try (Jedis jedis = pool().getResource()) {
            for (int offset = days - 1; offset >= 0; offset--) {
                LocalDate date = today.minusDays(offset);
                Map<String, String> amounts = jedis.hgetAll(FLOW_PREFIX + date);
                Map<String, String> counts = jedis.hgetAll(COUNT_PREFIX + date);
                Map<String, Delta> flows = new LinkedHashMap<>();
                amounts.forEach((field, amount) -> flows.put(field, new Delta(Double.parseDouble(amount),
                        Long.parseLong(counts.getOrDefault(field, "0")))));
                found.add(new Day(date, flows));
            }
        }
        return found;
    }

    public static void recordSupply(Supply supply) {
        try (Jedis jedis = pool().getResource()) {
            jedis.hset(SUPPLY_KEY, supply.date().toString(), supply.purse() + "," + supply.bank() + "," + supply.profiles());
        }
    }

    public static Map<LocalDate, Supply> readSupply() {
        Map<LocalDate, Supply> found = new HashMap<>();
        try (Jedis jedis = pool().getResource()) {
            jedis.hgetAll(SUPPLY_KEY).forEach((date, value) -> {
                String[] parts = value.split(",");
                if (parts.length != 3) return;
                LocalDate day = LocalDate.parse(date);
                found.put(day, new Supply(day, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Integer.parseInt(parts[2])));
            });
        }
        return found;
    }

    private static synchronized JedisPool pool() {
        if (pool == null) pool = RedisConnectionPool.connect(ConfigProvider.settings().getRedisUri(), POOL);
        return pool;
    }
}
