package dev.daweapon.commons.skyblock;

import dev.daweapon.commons.data.SharedData;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

// One wall-clock epoch in Redis that every server derives SkyBlock time from, so two servers can
// never disagree about the date. Elapsed advances one unit per 50 ms, the same rate as one unit per
// game tick at 20 TPS, but without drifting when a server lags.
public final class SharedCalendarClock {
    public static final String EPOCH_KEY = "hsb:calendar:epoch";
    public static final long MILLIS_PER_UNIT = 50L;

    private SharedCalendarClock() {}

    public static long elapsedAt(long epochMillis, long nowMillis) {
        return Math.max(0L, (nowMillis - epochMillis) / MILLIS_PER_UNIT);
    }

    public static long epochFor(long elapsed, long nowMillis) {
        return nowMillis - elapsed * MILLIS_PER_UNIT;
    }

    public static @Nullable Long parse(@Nullable String stored) {
        if (stored == null || stored.isBlank()) return null;
        try {
            return Long.parseLong(stored.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable Long read() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return parse(jedis.get(EPOCH_KEY));
        }
    }

    public static long claim(long proposedEpochMillis) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(EPOCH_KEY, Long.toString(proposedEpochMillis), SetParams.setParams().nx());
            Long stored = parse(jedis.get(EPOCH_KEY));
            return stored == null ? proposedEpochMillis : stored;
        }
    }

    public static void publish(long epochMillis) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(EPOCH_KEY, Long.toString(epochMillis));
        }
    }
}
