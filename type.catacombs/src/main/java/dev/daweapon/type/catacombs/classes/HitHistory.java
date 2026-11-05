package dev.daweapon.type.catacombs.classes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Each player's hits over the last minute, for Throwing Axe ("highest hit") and Explosive Shot and Rapid Fire ("highest bow hit"). */
public final class HitHistory {
    public static final long WINDOW_MILLIS = 60_000;

    private record Hit(long at, double damage) {
    }

    private final Map<UUID, Deque<Hit>> hits = new ConcurrentHashMap<>();

    public void record(UUID player, double damage, long now) {
        if (damage <= 0) return;
        Deque<Hit> deque = hits.computeIfAbsent(player, _ -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(new Hit(now, damage));
            prune(deque, now);
        }
    }

    public double highest(UUID player, long now) {
        Deque<Hit> deque = hits.get(player);
        if (deque == null) return 0;
        synchronized (deque) {
            prune(deque, now);
            return deque.stream().mapToDouble(Hit::damage).max().orElse(0);
        }
    }

    private static void prune(Deque<Hit> deque, long now) {
        while (!deque.isEmpty() && now - deque.peekFirst().at() > WINDOW_MILLIS) deque.removeFirst();
    }
}
