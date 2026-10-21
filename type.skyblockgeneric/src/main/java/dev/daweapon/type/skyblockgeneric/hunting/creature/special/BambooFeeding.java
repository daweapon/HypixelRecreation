package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import java.util.Random;

final class BambooFeeding {
    private final int target;
    private int fed;

    BambooFeeding(int target) {
        this.target = target;
    }

    static BambooFeeding roll(int min, int max, Random random) {
        return new BambooFeeding(min + random.nextInt(max - min + 1));
    }

    boolean feed() {
        fed++;
        return fed >= target;
    }

    int fed() {
        return fed;
    }

    int target() {
        return target;
    }

    double fraction() {
        return Math.min(1, (double) fed / target);
    }
}
