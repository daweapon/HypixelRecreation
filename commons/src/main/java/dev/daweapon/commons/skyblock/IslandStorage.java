package dev.daweapon.commons.skyblock;

import dev.daweapon.commons.data.SharedData;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Redis key helpers for checking existence and deleting a private island's stored data.
public final class IslandStorage {
    public static final String ISLAND_PREFIX = "hsb:island:";

    private IslandStorage() {}

    public static byte[] key(String islandId) {
        return (ISLAND_PREFIX + islandId).getBytes(StandardCharsets.UTF_8);
    }

    public static boolean delete(String islandId) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.del(GardenStorage.key(islandId));
            return jedis.del(key(islandId)) > 0;
        }
    }

    public static boolean delete(UUID islandId) {
        return delete(islandId.toString());
    }

    public static boolean exists(String islandId) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.exists(key(islandId));
        }
    }
}
