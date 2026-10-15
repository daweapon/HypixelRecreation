package dev.daweapon.commons.data;

import redis.clients.jedis.Jedis;

import java.util.UUID;

// Redis-backed lookup mapping lowercase player usernames to their UUID.
public final class NameIndex {
    private static final String KEY = "hsb:name2uuid";

    private NameIndex() {}

    public static void index(String name, UUID uuid) {
        if (SharedData.jedisPool() == null || name == null) return;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.hset(KEY, name.toLowerCase(), uuid.toString());
        }
    }

    public static UUID lookup(String name) {
        if (SharedData.jedisPool() == null || name == null) return null;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String value = jedis.hget(KEY, name.toLowerCase());
            return value == null ? null : UUID.fromString(value);
        }
    }
}
