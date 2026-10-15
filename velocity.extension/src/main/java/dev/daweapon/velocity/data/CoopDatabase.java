package dev.daweapon.velocity.data;

import dev.daweapon.commons.data.SharedData;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.UUID;

// Redis-backed lookup for coop group documents, addressable by coop id or by a member profile's UUID.
public record CoopDatabase(UUID id) {
    private static final String COOP_PREFIX = "hsb:coop:";
    private static final String BY_PROFILE = "hsb:coop:byprofile";

    public static void connect(String connectionString) {
    }

    public Document getDocument() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String json = jedis.get(COOP_PREFIX + id);
            return json == null ? null : Document.parse(json);
        }
    }

    public static Document getFromMemberProfile(UUID memberProfile) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String coopId = jedis.hget(BY_PROFILE, memberProfile.toString());
            if (coopId == null) return null;
            String json = jedis.get(COOP_PREFIX + coopId);
            return json == null ? null : Document.parse(json);
        }
    }
}
