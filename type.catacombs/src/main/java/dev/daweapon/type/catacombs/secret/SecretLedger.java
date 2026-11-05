package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Which secrets the party has found: each one counts once per run, and every room keeps its own found/total. */
public final class SecretLedger {
    public record Key(int room, int anchor) {
    }

    private final Map<Integer, Integer> totals;
    private final Set<Key> found = new HashSet<>();
    private final Map<Integer, Integer> foundPerRoom = new HashMap<>();

    public SecretLedger(Map<Integer, Integer> totals) {
        this.totals = Map.copyOf(totals);
    }

    public static SecretLedger of(FloorLayout layout) {
        Map<Integer, Integer> totals = new HashMap<>();
        for (PlacedRoom room : layout.rooms()) totals.put(room.index(), room.secretCount());
        return new SecretLedger(totals);
    }

    /** True only the first time a secret is found. */
    public synchronized boolean find(Key key) {
        if (!found.add(key)) return false;
        foundPerRoom.merge(key.room(), 1, Integer::sum);
        return true;
    }

    public synchronized boolean isFound(Key key) {
        return found.contains(key);
    }

    public synchronized int foundIn(int room) {
        return Math.min(foundPerRoom.getOrDefault(room, 0), totalIn(room));
    }

    public int totalIn(int room) {
        return totals.getOrDefault(room, 0);
    }

    public synchronized boolean allFound(int room) {
        return foundIn(room) >= totalIn(room);
    }

    public synchronized int found() {
        return found.size();
    }
}
