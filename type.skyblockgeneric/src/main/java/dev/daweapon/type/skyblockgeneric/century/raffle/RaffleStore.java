package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.data.SharedData;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class RaffleStore {
    private static final String PREFIX = "hsb:century:";
    private static final long DRAW_LOCK_SECONDS = 300;

    private RaffleStore() {
    }

    static boolean available() {
        return SharedData.jedisPool() != null;
    }

    private static String key(String eventId, String suffix) {
        return PREFIX + eventId + ":" + suffix;
    }

    private static String player(String eventId, UUID uuid) {
        return key(eventId, "player:" + uuid);
    }

    static RaffleProfile load(String eventId, UUID uuid) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return RaffleProfile.of(eventId, jedis.hgetAll(player(eventId, uuid)));
        }
    }

    static boolean setTeam(String eventId, UUID uuid, CenturyTeam team) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hsetnx(player(eventId, uuid), RaffleProfile.TEAM, team.name()) == 1;
        }
    }

    static long addTickets(String eventId, UUID uuid, CenturyTeam team, long amount, List<String> raffleKeys) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            for (String raffleKey : raffleKeys) {
                jedis.hincrBy(key(eventId, "entries:" + raffleKey), uuid.toString(), amount);
                jedis.hincrBy(key(eventId, "teams:" + raffleKey), team.name(), amount);
            }
            return jedis.hincrBy(player(eventId, uuid), RaffleProfile.TICKETS, amount);
        }
    }

    static long addOnlineMinute(String eventId, UUID uuid) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hincrBy(player(eventId, uuid), RaffleProfile.ONLINE, 1);
        }
    }

    static boolean claimOnce(String eventId, UUID uuid, String field) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hsetnx(player(eventId, uuid), field, "1") == 1;
        }
    }

    static boolean unclaim(String eventId, UUID uuid, String field) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hdel(player(eventId, uuid), field) == 1;
        }
    }

    static boolean feed(String eventId, UUID uuid, int period, int limit) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String field = RaffleProfile.FED_PREFIX + period;
            if (jedis.hincrBy(player(eventId, uuid), field, 1) <= limit) return true;
            jedis.hincrBy(player(eventId, uuid), field, -1);
            return false;
        }
    }

    static long entered(String eventId, String raffleKey, UUID uuid) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return RaffleProfile.parse(jedis.hget(key(eventId, "entries:" + raffleKey), uuid.toString()));
        }
    }

    static Map<UUID, Long> entries(String eventId, String raffleKey) {
        Map<UUID, Long> entries = new HashMap<>();
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.hgetAll(key(eventId, "entries:" + raffleKey)).forEach((uuid, tickets) -> {
                long count = RaffleProfile.parse(tickets);
                if (count > 0) entries.put(UUID.fromString(uuid), count);
            });
        }
        return entries;
    }

    static Map<CenturyTeam, Long> teamTotals(String eventId, String raffleKey) {
        Map<CenturyTeam, Long> totals = new EnumMap<>(CenturyTeam.class);
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.hgetAll(key(eventId, "teams:" + raffleKey)).forEach((team, tickets) -> {
                CenturyTeam parsed = CenturyTeam.parse(team);
                if (parsed != null) totals.put(parsed, RaffleProfile.parse(tickets));
            });
        }
        return totals;
    }

    static boolean lockDraw(String eventId, String raffleKey) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return "OK".equals(jedis.set(key(eventId, "drawing:" + raffleKey), "1", SetParams.setParams().nx().ex(DRAW_LOCK_SECONDS)));
        }
    }

    static void finishDraw(String eventId, String raffleKey, Map<UUID, PrizeTier> winners, long totalTickets, @Nullable CenturyTeam topTeam) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            winners.forEach((uuid, tier) -> jedis.hset(key(eventId, "results:" + uuid), raffleKey, tier.name()));
            jedis.set(key(eventId, "drawn:" + raffleKey), totalTickets + "|" + (topTeam == null ? "" : topTeam.name()));
        }
    }

    static @Nullable String drawn(String eventId, String raffleKey) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.get(key(eventId, "drawn:" + raffleKey));
        }
    }

    static Map<String, String> results(String eventId, UUID uuid) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hgetAll(key(eventId, "results:" + uuid));
        }
    }

    static Map<String, String> claimed(String eventId, UUID uuid) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hgetAll(key(eventId, "claimed:" + uuid));
        }
    }

    static boolean claimResult(String eventId, UUID uuid, String raffleKey) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.hsetnx(key(eventId, "claimed:" + uuid), raffleKey, "1") == 1;
        }
    }
}
