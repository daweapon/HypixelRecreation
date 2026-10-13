package net.swofty.commons.skyblock;

import net.swofty.commons.data.SwoftyData;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class GardenStorage {
    public static final String GARDEN_PREFIX = "hsb:garden:";

    private GardenStorage() {}

    public static byte[] key(String gardenId) {
        return (GARDEN_PREFIX + gardenId).getBytes(StandardCharsets.UTF_8);
    }

    public static boolean delete(String gardenId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            return jedis.del(key(gardenId)) > 0;
        }
    }

    public static boolean delete(UUID gardenId) {
        return delete(gardenId.toString());
    }

    public static boolean exists(String gardenId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            return jedis.exists(key(gardenId));
        }
    }
}
