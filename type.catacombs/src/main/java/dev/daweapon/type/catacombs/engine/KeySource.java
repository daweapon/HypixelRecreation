package dev.daweapon.type.catacombs.engine;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which kill hands over a key room's key, as in the captured runs and on the wiki: the room's miniboss when it has one,
 * otherwise the last starred mob killed in it, dropped where that mob died. A key room cleared without such a kill (a
 * room with no starred mobs, or a staff clear) gives it when the room completes. Each room gives its key once.
 */
final class KeySource {
    private final KeyDropPlan plan;
    private final Set<Integer> minibossAlive = ConcurrentHashMap.newKeySet();
    private final Set<Integer> given = ConcurrentHashMap.newKeySet();

    KeySource(KeyDropPlan plan) {
        this.plan = plan;
    }

    void minibossSpawned(int room) {
        minibossAlive.add(room);
    }

    /** The key this kill drops at the mob's feet, if any. */
    Optional<KeyType> killed(int room, boolean miniboss, boolean lastStarred) {
        if (miniboss) {
            minibossAlive.remove(room);
            return claim(room);
        }
        if (!lastStarred || minibossAlive.contains(room)) return Optional.empty();
        return claim(room);
    }

    /** The key a completed room still owes, dropped at its stand point. */
    Optional<KeyType> completed(int room) {
        if (minibossAlive.contains(room)) return Optional.empty();
        return claim(room);
    }

    private Optional<KeyType> claim(int room) {
        return plan.keyFor(room).filter(_ -> given.add(room));
    }
}
