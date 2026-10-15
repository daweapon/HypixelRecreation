package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MobAggroLog {
    public static final long MEMORY_MS = 60_000;
    private static final Map<UUID, Map<UUID, Long>> ATTACKED = new ConcurrentHashMap<>();

    private MobAggroLog() {
    }

    public static void record(UUID player, UUID mob, long now) {
        Map<UUID, Long> mobs = ATTACKED.computeIfAbsent(player, _ -> new ConcurrentHashMap<>());
        mobs.values().removeIf(until -> until <= now);
        mobs.put(mob, now + MEMORY_MS);
    }

    public static boolean hasAttacked(UUID player, UUID mob, long now) {
        Map<UUID, Long> mobs = ATTACKED.get(player);
        if (mobs == null) return false;
        Long until = mobs.get(mob);
        return until != null && now < until;
    }

    public static void clear(UUID player) {
        ATTACKED.remove(player);
    }
}
