package net.swofty.type.kuudra.fight;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which hits on a stunned Kuudra count toward the hit-based DPS check: a melee swing once per cooldown, and every
 * landed arrow up to a per-tick cap, so a Terminator volley and Hydra Strike's extra arrows each score.
 */
final class StunnedHits {
    private final int meleeCooldownTicks;
    private final int arrowsPerTick;
    private final Map<UUID, Long> meleeReady = new ConcurrentHashMap<>();
    private final Map<UUID, long[]> arrowTick = new ConcurrentHashMap<>();

    StunnedHits(int meleeCooldownTicks, int arrowsPerTick) {
        this.meleeCooldownTicks = meleeCooldownTicks;
        this.arrowsPerTick = arrowsPerTick;
    }

    boolean melee(UUID player, long tick) {
        if (tick < meleeReady.getOrDefault(player, Long.MIN_VALUE)) return false;
        meleeReady.put(player, tick + meleeCooldownTicks);
        return true;
    }

    boolean arrow(UUID player, long tick) {
        long[] counted = arrowTick.computeIfAbsent(player, _ -> new long[]{Long.MIN_VALUE, 0});
        synchronized (counted) {
            if (counted[0] != tick) {
                counted[0] = tick;
                counted[1] = 0;
            }
            if (counted[1] >= arrowsPerTick) return false;
            counted[1]++;
            return true;
        }
    }
}
