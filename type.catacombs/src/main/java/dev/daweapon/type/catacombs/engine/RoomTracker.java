package dev.daweapon.type.catacombs.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Which rooms were entered and how many starred mobs each still has; a room clears when its last starred mob dies. */
public final class RoomTracker {
    private final Set<Integer> discovered = new HashSet<>();
    private final Set<Integer> cleared = new HashSet<>();
    private final Map<Integer, Integer> starredAlive = new HashMap<>();
    private final Set<Integer> spawned = new HashSet<>();

    public synchronized boolean discover(int room) {
        return discovered.add(room);
    }

    public synchronized int discoveredCount() {
        return discovered.size();
    }

    public synchronized boolean isDiscovered(int room) {
        return discovered.contains(room);
    }

    public synchronized void registerStarred(int room, int count) {
        spawned.add(room);
        starredAlive.merge(room, count, Integer::sum);
    }

    public synchronized int starredAlive(int room) {
        return starredAlive.getOrDefault(room, 0);
    }

    /** True exactly once: when the kill leaves a room that had starred mobs with none alive. */
    public synchronized boolean starredKilled(int room) {
        int alive = starredAlive.getOrDefault(room, 0);
        if (alive == 0) return false;
        starredAlive.put(room, alive - 1);
        return alive == 1 && cleared.add(room);
    }

    /** Clears a room without a kill; false if it was already cleared. */
    public synchronized boolean forceClear(int room) {
        starredAlive.put(room, 0);
        return cleared.add(room);
    }

    public synchronized boolean isCleared(int room) {
        return cleared.contains(room);
    }

    public synchronized boolean hasSpawned(int room) {
        return spawned.contains(room);
    }
}
