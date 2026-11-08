package dev.daweapon.type.catacombs.run;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/** Ready-up before the run: it starts when everyone present is ready, or after the timeout, behind a short countdown. */
public final class CatacombsReadyCheck {
    public static final int TIMEOUT_SECONDS = 30;
    public static final int COUNTDOWN_SECONDS = 4;

    private final Set<UUID> ready = ConcurrentHashMap.newKeySet();
    private volatile boolean countingDown;
    private volatile boolean forced;
    private volatile int countdown;
    private volatile int waitedSeconds;

    /** Flips the player's ready state. Returns whether they are ready now. */
    public boolean toggle(UUID player) {
        if (ready.contains(player)) return !unready(player);
        return markReady(player) || ready.contains(player);
    }

    /** Takes back a ready; cancels an all-ready countdown, but not the one the timeout forced. */
    public boolean unready(UUID player) {
        if (forced || !ready.remove(player)) return false;
        if (countingDown) {
            countingDown = false;
            countdown++;
        }
        return true;
    }

    public int countdown() {
        return countdown;
    }

    public boolean stillCounting(int id) {
        return countingDown && countdown == id;
    }

    /** Readies the player; false when they already were or the countdown has begun. */
    public boolean markReady(UUID player) {
        return !countingDown && ready.add(player);
    }

    public boolean isReady(UUID player) {
        return ready.contains(player);
    }

    public boolean startCountdownIfAllReady(Collection<UUID> present) {
        if (countingDown || present.isEmpty() || !ready.containsAll(present)) return false;
        countingDown = true;
        return true;
    }

    /** One waiting second; true when the timeout runs out and the countdown must start. */
    public boolean tickSecond(Collection<UUID> present) {
        if (countingDown || present.isEmpty()) return false;
        if (++waitedSeconds < TIMEOUT_SECONDS) return false;
        countingDown = true;
        forced = true;
        return true;
    }

    public boolean countingDown() {
        return countingDown;
    }

    public int secondsLeft() {
        return Math.max(0, TIMEOUT_SECONDS - waitedSeconds);
    }

    public static List<Integer> countdownSeconds() {
        return IntStream.iterate(COUNTDOWN_SECONDS, seconds -> seconds >= 1, seconds -> seconds - 1).boxed().toList();
    }
}
