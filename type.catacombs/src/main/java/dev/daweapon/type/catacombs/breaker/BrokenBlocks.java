package dev.daweapon.type.catacombs.breaker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The blocks Dungeonbreakers have taken out, waiting to come back: each returns 10 seconds after it was broken, and a
 * player who has 20 out at once brings their oldest back early when they break a 21st (0.23.5 Alpha notes).
 */
public final class BrokenBlocks<B> {
    public static final long RESTORE_MILLIS = 10_000;
    public static final int MAX_PER_PLAYER = 20;

    public record Broken<B>(UUID owner, int x, int y, int z, B original, long restoreAt) {
        long key() {
            return pack(x, y, z);
        }
    }

    private final Map<UUID, Deque<Broken<B>>> byOwner = new HashMap<>();
    private final Map<Long, Broken<B>> byPosition = new HashMap<>();

    /** Records a break and returns the blocks that must come back now to keep the owner at twenty. */
    public synchronized List<Broken<B>> record(UUID owner, int x, int y, int z, B original, long now) {
        Broken<B> broken = new Broken<>(owner, x, y, z, original, now + RESTORE_MILLIS);
        Deque<Broken<B>> queue = byOwner.computeIfAbsent(owner, _ -> new ArrayDeque<>());
        queue.addLast(broken);
        byPosition.put(broken.key(), broken);
        List<Broken<B>> evicted = new ArrayList<>();
        while (queue.size() > MAX_PER_PLAYER) {
            Broken<B> oldest = queue.pollFirst();
            byPosition.remove(oldest.key());
            evicted.add(oldest);
        }
        return evicted;
    }

    public synchronized List<Broken<B>> due(long now) {
        List<Broken<B>> due = new ArrayList<>();
        for (Iterator<Deque<Broken<B>>> owners = byOwner.values().iterator(); owners.hasNext(); ) {
            Deque<Broken<B>> queue = owners.next();
            while (!queue.isEmpty() && queue.peekFirst().restoreAt() <= now) {
                Broken<B> next = queue.pollFirst();
                byPosition.remove(next.key());
                due.add(next);
            }
            if (queue.isEmpty()) owners.remove();
        }
        return due;
    }

    public synchronized List<Broken<B>> drain() {
        List<Broken<B>> all = new ArrayList<>(byPosition.values());
        byOwner.clear();
        byPosition.clear();
        return all;
    }

    public synchronized boolean isBroken(int x, int y, int z) {
        return byPosition.containsKey(pack(x, y, z));
    }

    public synchronized int brokenBy(UUID owner) {
        Deque<Broken<B>> queue = byOwner.get(owner);
        return queue == null ? 0 : queue.size();
    }

    static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
