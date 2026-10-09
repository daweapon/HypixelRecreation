package net.swofty.type.kuudra.fight;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The run's Kuudra Tokens (research K1 4). Tokens are shared: whenever anyone earns some, every player gets the
 * same amount, then spends their own balance in the Perk Shop. Vulcan's Headstart gives a player a head start.
 * Unlocking a route costs tokens too (the Burning capture: "Cost to unlock: 50 Tokens").
 */
public final class KuudraTokens {
    private final Map<UUID, Integer> balance = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> earned = new ConcurrentHashMap<>();

    public void join(UUID player, int headstart) {
        if (balance.containsKey(player)) return;
        balance.put(player, Math.max(0, headstart));
        earned.put(player, Math.max(0, headstart));
    }

    public void gain(int amount) {
        if (amount <= 0) return;
        balance.replaceAll((_, value) -> value + amount);
        earned.replaceAll((_, value) -> value + amount);
    }

    public boolean spend(UUID player, int cost) {
        int current = balance.getOrDefault(player, 0);
        if (cost < 0 || current < cost) return false;
        balance.put(player, current - cost);
        return true;
    }

    public int balance(UUID player) {
        return balance.getOrDefault(player, 0);
    }

    /** Every token the player gained this run, Headstart included (the "Tokens Earned" line at the end). */
    public int earned(UUID player) {
        return earned.getOrDefault(player, 0);
    }
}
