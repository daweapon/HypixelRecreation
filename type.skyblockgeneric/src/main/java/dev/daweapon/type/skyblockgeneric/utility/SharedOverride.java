package dev.daweapon.type.skyblockgeneric.utility;

import dev.daweapon.commons.data.SharedData;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

/** A staff on/off switch stored in Redis, so every server picks it up within a couple of seconds. */
public final class SharedOverride {
    private static final long REFRESH_MILLIS = 2_000;

    private final String key;
    private volatile @Nullable Boolean value;
    private volatile boolean polling;

    public SharedOverride(String key) {
        this.key = key;
    }

    public @Nullable Boolean value() {
        if (!polling) startPolling();
        return value;
    }

    public boolean set(boolean active) {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(key, Boolean.toString(active));
        }
        value = active;
        return true;
    }

    public boolean clear() {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.del(key);
        }
        value = null;
        return true;
    }

    private synchronized void startPolling() {
        if (polling) return;
        polling = true;
        Thread.startVirtualThread(() -> {
            while (true) {
                refresh();
                try {
                    Thread.sleep(REFRESH_MILLIS);
                } catch (InterruptedException interrupted) {
                    return;
                }
            }
        });
    }

    private void refresh() {
        if (SharedData.jedisPool() == null) return;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String raw = jedis.get(key);
            value = raw == null ? null : Boolean.parseBoolean(raw);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to read the override {}", key);
        }
    }
}
