package dev.daweapon.type.skyblockgeneric.data.monogdb;

import com.mongodb.client.MongoClient;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.IslandStorage;
import dev.daweapon.type.generic.data.mongodb.MongoDB;
import org.bson.Document;
import org.bson.types.Binary;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.List;

// Redis-backed key/value store for a single island's profile data, keyed by profile UUID.
public record IslandDatabase(String profileUuid) implements MongoDB {

    public static void connect(MongoClient client) {
    }

    private byte[] islandKey() {
        return IslandStorage.key(profileUuid);
    }

    @Override
    public void set(String key, Object value) {
        insertOrUpdate(key, value);
    }

    public void insertOrUpdate(String key, Object value) {
        byte[] bytes = value instanceof Binary binary
                ? binary.getData()
                : String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.hset(islandKey(), key.getBytes(StandardCharsets.UTF_8), bytes);
        }
    }

    @Override
    public Object get(String key, Object def) {
        byte[] raw;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            raw = jedis.hget(islandKey(), key.getBytes(StandardCharsets.UTF_8));
        }
        if (raw == null) return def;
        if (def == Binary.class) return new Binary(raw);
        String value = new String(raw, StandardCharsets.UTF_8);
        if (def == Integer.class) return Integer.parseInt(value);
        if (def == Long.class) return Long.parseLong(value);
        return value;
    }

    public boolean has(String key) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hexists(islandKey(), key.getBytes(StandardCharsets.UTF_8));
        }
    }

    public boolean exists() {
        return IslandStorage.exists(profileUuid);
    }

    @Override
    public boolean remove(String id) {
        return IslandStorage.delete(id);
    }

    public List<Document> getAll() {
        return List.of();
    }

    public Document getDocument() {
        return null;
    }
}
