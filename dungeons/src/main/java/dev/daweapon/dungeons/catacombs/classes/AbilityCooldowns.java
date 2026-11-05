package dev.daweapon.dungeons.catacombs.classes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** When each member's class and ghost abilities are ready again, in milliseconds of whatever clock the caller uses. */
public final class AbilityCooldowns {
    private final Map<UUID, Map<String, Long>> readyAt = new HashMap<>();

    public synchronized boolean tryUse(UUID member, String abilityId, double cooldownSeconds, long nowMillis) {
        if (remainingMillis(member, abilityId, nowMillis) > 0) return false;
        readyAt.computeIfAbsent(member, _ -> new HashMap<>()).put(abilityId, nowMillis + Math.round(cooldownSeconds * 1000));
        return true;
    }

    public synchronized long remainingMillis(UUID member, String abilityId, long nowMillis) {
        Long ready = readyAt.getOrDefault(member, Map.of()).get(abilityId);
        return ready == null ? 0 : Math.max(0, ready - nowMillis);
    }

    public synchronized void reduce(UUID member, String abilityId, long millis) {
        Map<String, Long> abilities = readyAt.get(member);
        if (abilities != null) abilities.computeIfPresent(abilityId, (_, ready) -> ready - millis);
    }

    public synchronized void reset(UUID member) {
        readyAt.remove(member);
    }

    public static int displaySeconds(long remainingMillis) {
        return (int) Math.ceil(remainingMillis / 1000.0);
    }
}
