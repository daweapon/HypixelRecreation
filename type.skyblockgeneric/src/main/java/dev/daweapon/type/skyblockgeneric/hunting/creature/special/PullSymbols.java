package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class PullSymbols {
    static final int GOAL = 4;
    private static final double PULL_PER_SYMBOL = 25;

    private PullSymbols() {
    }

    static int gained(double pull) {
        return (int) Math.max(1, Math.min(GOAL, Math.ceil(pull / PULL_PER_SYMBOL)));
    }

    static int total(int current, double pull) {
        return Math.min(GOAL, current + gained(pull));
    }
}
