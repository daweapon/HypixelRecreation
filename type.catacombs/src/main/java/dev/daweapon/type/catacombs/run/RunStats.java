package dev.daweapon.type.catacombs.run;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each member's numbers for EXTRA STATS: damage dealt to dungeon mobs that died (and to the boss), and the kills they
 * landed. Hypixel also counts healing; there is no ally healing to count here yet.
 */
public final class RunStats {
    private final Map<UUID, Double> damage = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> kills = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> deaths = new ConcurrentHashMap<>();

    public void damage(UUID player, double amount) {
        if (amount > 0) damage.merge(player, amount, Double::sum);
    }

    public void kill(UUID player) {
        kills.merge(player, 1, Integer::sum);
    }

    public void death(UUID player) {
        deaths.merge(player, 1, Integer::sum);
    }

    public int deathsOf(UUID player) {
        return deaths.getOrDefault(player, 0);
    }

    public long damageOf(UUID player) {
        return Math.round(damage.getOrDefault(player, 0D));
    }

    public int killsOf(UUID player) {
        return kills.getOrDefault(player, 0);
    }
}
