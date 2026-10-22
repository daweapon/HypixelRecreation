package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeaconCooldowns {
    public static final long TUNE_COOLDOWN_MILLIS = 10L * 60 * 1000;
    private static final Map<UUID, Long> LAST_TUNED = new ConcurrentHashMap<>();

    private BeaconCooldowns() {
    }

    public static long remainingMillis(UUID player, long now) {
        Long last = LAST_TUNED.get(player);
        return last == null ? 0 : Math.max(0, last + TUNE_COOLDOWN_MILLIS - now);
    }

    public static void mark(UUID player, long now) {
        LAST_TUNED.put(player, now);
    }

    public static void clear() {
        LAST_TUNED.clear();
    }
}
