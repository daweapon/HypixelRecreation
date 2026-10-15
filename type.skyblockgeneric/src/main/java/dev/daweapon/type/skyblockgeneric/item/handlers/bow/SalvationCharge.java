package dev.daweapon.type.skyblockgeneric.item.handlers.bow;

/** The Terminator's hit counter: Salvation can be cast once three arrow hits have landed. */
public final class SalvationCharge {
    public static final int HITS_NEEDED = 3;

    private int hits;

    public synchronized void hit() {
        hits = Math.min(HITS_NEEDED, hits + 1);
    }

    public synchronized int hits() {
        return hits;
    }

    public synchronized boolean ready() {
        return hits >= HITS_NEEDED;
    }

    public synchronized boolean consume() {
        if (hits < HITS_NEEDED) return false;
        hits = 0;
        return true;
    }
}
