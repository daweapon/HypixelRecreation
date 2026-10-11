package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatState {
    private static final Map<UUID, Long> LAST_COMBAT = new ConcurrentHashMap<>();

    private CombatState() {
    }

    public static void record(UUID player, long now) {
        LAST_COMBAT.put(player, now);
    }

    public static boolean inCombat(UUID player, long now, long windowMs) {
        Long last = LAST_COMBAT.get(player);
        return last != null && now - last < windowMs;
    }

    public static void release(UUID player) {
        LAST_COMBAT.remove(player);
    }
}
