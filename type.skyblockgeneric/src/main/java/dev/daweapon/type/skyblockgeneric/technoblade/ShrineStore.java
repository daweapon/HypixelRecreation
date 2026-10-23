package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.data.SharedData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.Map;

final class ShrineStore {
    private static final String RELICS = "hsb:technoblade:relics";
    private static final String BUILT = "hsb:technoblade:built";
    private static final String COMPLETED_AT = "hsb:technoblade:completed_at";
    private static final String EFFECT = "hsb:technoblade:effect";
    private static final String ROLL_LOCK = "hsb:technoblade:roll_lock";

    private ShrineStore() {
    }

    static boolean available() {
        return SharedData.jedisPool() != null;
    }

    static ShrineSnapshot read() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            Map<Integer, String> relics = new HashMap<>();
            jedis.hgetAll(RELICS).forEach((index, finder) -> relics.put(Integer.parseInt(index), finder));
            String completed = jedis.get(COMPLETED_AT);
            return new ShrineSnapshot(Map.copyOf(relics), jedis.get(BUILT) != null,
                    completed == null ? 0 : Long.parseLong(completed),
                    ShrineSnapshot.ActiveEffect.parse(jedis.get(EFFECT)));
        }
    }

    static void markBuilt() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(BUILT, "1");
        }
    }

    static boolean claimRelic(int index, String finder, int relicCount) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            if (jedis.hsetnx(RELICS, String.valueOf(index), finder) == 0) return false;
            jedis.set(BUILT, "1");
            if (jedis.hlen(RELICS) >= relicCount) {
                jedis.set(COMPLETED_AT, String.valueOf(System.currentTimeMillis()), SetParams.setParams().nx());
            }
            return true;
        }
    }

    static boolean tryLockRoll(long intervalMillis) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.set(ROLL_LOCK, "1", SetParams.setParams().nx().px(Math.max(1000, intervalMillis - 1000))) != null;
        }
    }

    static void writeEffect(ShrineSnapshot.ActiveEffect effect, long intervalMillis) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(EFFECT, effect.serialize());
            jedis.set(ROLL_LOCK, "1", SetParams.setParams().px(Math.max(1000, intervalMillis - 1000)));
        }
    }

    static void reset() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.del(RELICS, BUILT, COMPLETED_AT, EFFECT, ROLL_LOCK);
        }
    }
}
