package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import java.util.random.RandomGenerator;

final class TriggerMath {
    private TriggerMath() {
    }

    static double withLurker(double chancePercent, double lurkerPercent) {
        return chancePercent * (1 + Math.max(0, lurkerPercent) / 100D);
    }

    static long delayMillis(int minMinutes, int maxMinutes, RandomGenerator random) {
        int high = Math.max(minMinutes, maxMinutes);
        long minutes = minMinutes + (long) Math.floor(random.nextDouble() * (high - minMinutes + 1));
        return Math.min(minutes, high) * 60_000L;
    }

    static boolean hourChanged(int previous, int current) {
        return previous >= 0 && previous != current;
    }

    static int antCount(int min, int max, RandomGenerator random) {
        return min + (int) Math.floor(random.nextDouble() * (Math.max(min, max) - min + 1));
    }
}
