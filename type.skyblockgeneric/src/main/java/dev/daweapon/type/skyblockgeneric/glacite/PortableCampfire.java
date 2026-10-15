package dev.daweapon.type.skyblockgeneric.glacite;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PortableCampfire {
    public static final double COLD_REMOVED = 50;
    public static final long LIFETIME_MILLIS = 5 * 60 * 1000L;
    public static final long PLACE_COOLDOWN_MILLIS = 5 * 60 * 1000L;

    private static final Map<UUID, Long> LAST_PLACED = new ConcurrentHashMap<>();

    private PortableCampfire() {
    }

    public static long cooldownRemaining(long lastPlaced, long now) {
        return Math.max(0, lastPlaced + PLACE_COOLDOWN_MILLIS - now);
    }

    public static boolean expired(long placedAt, long now) {
        return now - placedAt >= LIFETIME_MILLIS;
    }

    public static long remainingCooldown(UUID player, long now) {
        return cooldownRemaining(LAST_PLACED.getOrDefault(player, 0L), now);
    }

    public static void markPlaced(UUID player, long now) {
        LAST_PLACED.put(player, now);
    }
}
