package dev.daweapon.type.skyblockgeneric.century.raffle;

import java.util.ArrayList;
import java.util.List;

public final class RaffleClock {
    public static final long TASK_PERIOD_MILLIS = RaffleType.SPEED.periodMillis();

    private RaffleClock() {
    }

    public static int index(CenturyEvent.Window window, RaffleType type, long now) {
        if (!type.periodic()) return 0;
        return (int) (Math.max(0, now - window.start()) / type.periodMillis());
    }

    public static int count(CenturyEvent.Window window, RaffleType type) {
        if (!type.periodic()) return 1;
        long length = window.end() - window.start();
        return (int) ((length + type.periodMillis() - 1) / type.periodMillis());
    }

    public static long end(CenturyEvent.Window window, RaffleType type, int index) {
        if (!type.periodic()) return window.end();
        return Math.min(window.start() + (index + 1L) * type.periodMillis(), window.end());
    }

    public static int taskPeriod(CenturyEvent.Window window, long now) {
        return index(window, RaffleType.SPEED, now);
    }

    public static List<String> currentKeys(CenturyEvent.Window window, long now) {
        List<String> keys = new ArrayList<>();
        for (RaffleType type : RaffleType.values()) keys.add(type.key(index(window, type, now)));
        return keys;
    }

    public static RaffleType typeOf(String raffleKey) {
        return RaffleType.valueOf(raffleKey.split(":")[0].toUpperCase());
    }

    public static int numberOf(String raffleKey) {
        String[] parts = raffleKey.split(":");
        return parts.length < 2 ? 1 : Integer.parseInt(parts[1]) + 1;
    }

    public static String label(String raffleKey) {
        return typeOf(raffleKey).label(numberOf(raffleKey));
    }
}
