package dev.daweapon.type.catacombs.engine;

import java.util.EnumMap;
import java.util.Map;

/** The keys the whole party holds: a pickup is shared, and opening a door spends one. */
public final class KeyRing {
    private final Map<KeyType, Integer> held = new EnumMap<>(KeyType.class);

    public synchronized void grant(KeyType key) {
        held.merge(key, 1, Integer::sum);
    }

    public synchronized boolean has(KeyType key) {
        return count(key) > 0;
    }

    public synchronized int count(KeyType key) {
        return held.getOrDefault(key, 0);
    }

    public synchronized boolean consume(KeyType key) {
        int count = count(key);
        if (count == 0) return false;
        held.put(key, count - 1);
        return true;
    }
}
