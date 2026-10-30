package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.UUID;
import java.util.random.RandomGenerator;

public final class MineshaftPity {
    public static final int START = 2000;
    public static final long NO_INCREASE_MILLIS = 60_000;
    public static final int CORE_BONUS_LEVEL = 9;
    public static final double CORE_BONUS_PERCENT = 10;

    private MineshaftPity() {
    }

    public record Outcome(int counter, boolean spawned) {
    }

    public static double chancePercent(int counter, double bonusPercent) {
        if (counter <= 0) return 100;
        return Math.min(100, 100.0 / counter * (1 + bonusPercent / 100));
    }

    public static int displayed(int counter) {
        return START - Math.max(0, Math.min(START, counter));
    }

    public static int afterMining(int counter, int quality) {
        return Math.max(0, counter - Math.max(0, quality));
    }

    public static boolean frozen(long exitedAt, String exitedServer, String server, long now) {
        return exitedAt > 0 && exitedServer != null && exitedServer.equals(server) && now - exitedAt < NO_INCREASE_MILLIS;
    }

    public static Outcome step(UUID player, int counter, int quality, double bonusPercent, RandomGenerator random) {
        if (LootRoll.roll(player, null, chancePercent(counter, bonusPercent), random)) return new Outcome(START, true);
        return new Outcome(afterMining(counter, quality), false);
    }
}
