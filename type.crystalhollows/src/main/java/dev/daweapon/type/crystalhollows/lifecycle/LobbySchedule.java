package dev.daweapon.type.crystalhollows.lifecycle;

public final class LobbySchedule {
    public static final long MILLIS_PER_DAY = 20L * 60 * 1000;
    public static final int LOCK_DAY = 19;
    public static final int SHUTDOWN_DAY = 35;
    public static final int MIN_PLAYERS = 4;

    private LobbySchedule() {
    }

    public enum Phase {
        OPEN,
        LOCKED,
        CLOSED
    }

    public static int dayAt(long elapsedMillis) {
        return (int) (Math.max(0, elapsedMillis) / MILLIS_PER_DAY);
    }

    public static Phase phaseAt(long elapsedMillis, int players) {
        int day = dayAt(elapsedMillis);
        if (day >= SHUTDOWN_DAY) return Phase.CLOSED;
        if (day >= LOCK_DAY) return players < MIN_PLAYERS ? Phase.CLOSED : Phase.LOCKED;
        return Phase.OPEN;
    }

    public static boolean acceptsNewPlayers(long elapsedMillis) {
        return dayAt(elapsedMillis) < LOCK_DAY;
    }

    public static long millisUntilShutdown(long elapsedMillis) {
        return Math.max(0, SHUTDOWN_DAY * MILLIS_PER_DAY - elapsedMillis);
    }
}
