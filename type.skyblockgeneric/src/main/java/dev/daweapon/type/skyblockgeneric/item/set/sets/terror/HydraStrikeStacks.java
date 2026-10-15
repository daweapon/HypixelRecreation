package dev.daweapon.type.skyblockgeneric.item.set.sets.terror;

/**
 * One player's Hydra Strike stacks: at most one stack per 0.2s of arrow hits, up to 10, and one stack lost for every
 * decay window that passes without gaining one.
 */
public final class HydraStrikeStacks {
    public static final int MAX_STACKS = 10;
    public static final long GAIN_INTERVAL_MILLIS = 200;

    private int stacks;
    private long lastGain = Long.MIN_VALUE / 2;
    private long decayFrom;

    public synchronized boolean gain(long now, long decayMillis) {
        settle(now, decayMillis);
        if (now - lastGain < GAIN_INTERVAL_MILLIS) return false;
        lastGain = now;
        decayFrom = now;
        stacks = Math.min(MAX_STACKS, stacks + 1);
        return true;
    }

    public synchronized int stacks(long now, long decayMillis) {
        settle(now, decayMillis);
        return stacks;
    }

    private void settle(long now, long decayMillis) {
        if (stacks == 0) return;
        long since = now - decayFrom;
        if (since < decayMillis) return;
        long lost = since / decayMillis;
        stacks = (int) Math.max(0, stacks - lost);
        decayFrom += lost * decayMillis;
    }
}
