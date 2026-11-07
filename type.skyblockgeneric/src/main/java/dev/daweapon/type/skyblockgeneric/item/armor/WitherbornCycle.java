package dev.daweapon.type.skyblockgeneric.item.armor;

/**
 * Witherborn's timer: a wither minion is due every 30 seconds while the set is worn, and the beat passes without one
 * while the last minion is still out (the bonus keeps at most one).
 */
public final class WitherbornCycle {
    public static final long SPAWN_MILLIS = 30_000;
    public static final int MAX_MINIONS = 1;

    private long nextSpawnAt = -1;

    /** Called while the set is worn; true when a minion should spawn now. */
    public boolean tick(long now, int minionsOut) {
        if (nextSpawnAt < 0) nextSpawnAt = now + SPAWN_MILLIS;
        if (now < nextSpawnAt) return false;
        nextSpawnAt = now + SPAWN_MILLIS;
        return minionsOut < MAX_MINIONS;
    }

    public void reset() {
        nextSpawnAt = -1;
    }
}
