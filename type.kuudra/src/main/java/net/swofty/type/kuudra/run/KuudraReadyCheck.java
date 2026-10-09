package net.swofty.type.kuudra.run;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The pre-start ready check (the Burning and Infernal captures): each player talks to Elle, or readies up on arrival
 * with Auto Ready Up, and once everyone in the Hollow is ready a 4 second countdown starts the fight.
 */
public final class KuudraReadyCheck {
    public static final int COUNTDOWN_SECONDS = 4;

    private final Set<UUID> ready = ConcurrentHashMap.newKeySet();
    private volatile boolean countingDown;

    /** Marks the player ready; false if they already were or the countdown has begun. */
    public boolean ready(UUID player) {
        return !countingDown && ready.add(player);
    }

    public boolean isReady(UUID player) {
        return ready.contains(player);
    }

    /** Whether everyone present is ready, which starts the countdown (once). */
    public boolean startCountdown(Collection<UUID> present) {
        if (countingDown || present.isEmpty() || !ready.containsAll(present)) return false;
        countingDown = true;
        return true;
    }

    public boolean countingDown() {
        return countingDown;
    }

    /** "Starting in 4 seconds." down to "Starting in 1 second.", one per second. */
    public static List<String> countdown() {
        return java.util.stream.IntStream.iterate(COUNTDOWN_SECONDS, seconds -> seconds >= 1, seconds -> seconds - 1)
                .mapToObj(seconds -> "<a>Starting in " + seconds + (seconds == 1 ? " second." : " seconds."))
                .toList();
    }
}
