package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class HopCounter {
    private final int limit;
    private int hops;

    HopCounter(int limit) {
        this.limit = limit;
    }

    boolean hop() {
        hops++;
        return hops >= limit;
    }

    int hops() {
        return hops;
    }

    boolean exhausted() {
        return hops >= limit;
    }
}
