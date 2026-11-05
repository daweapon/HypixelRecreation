package dev.daweapon.type.catacombs.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Who is a ghost and how long until each revives on their own: after the mode's automatic revive when it has one, or
 * after a Healer's Revive Self, whichever comes first.
 */
public final class GhostRoster {
    private static final int NO_TIMER = Integer.MAX_VALUE;

    private final boolean autoRevive;
    private final int reviveSeconds;
    private final Map<UUID, Integer> ghosts = new HashMap<>();

    public GhostRoster(boolean autoRevive, int reviveSeconds) {
        this.autoRevive = autoRevive;
        this.reviveSeconds = reviveSeconds;
    }

    public synchronized boolean die(UUID player) {
        return ghosts.putIfAbsent(player, autoRevive ? reviveSeconds : NO_TIMER) == null;
    }

    public synchronized boolean revive(UUID player) {
        return ghosts.remove(player) != null;
    }

    public synchronized boolean isGhost(UUID player) {
        return ghosts.containsKey(player);
    }

    /** Starts (or shortens) a ghost's own revive timer; false when the player isn't a ghost. */
    public synchronized boolean reviveAfter(UUID player, int seconds) {
        Integer left = ghosts.get(player);
        if (left == null) return false;
        ghosts.put(player, Math.min(left, Math.max(1, seconds)));
        return true;
    }

    public synchronized int secondsToRevive(UUID player) {
        int left = ghosts.getOrDefault(player, 0);
        return left == NO_TIMER ? 0 : left;
    }

    public synchronized int ghostCount() {
        return ghosts.size();
    }

    /** One second: the ghosts whose timer ran out, already revived here. Ghosts without a timer never revive here. */
    public synchronized List<UUID> tickSecond() {
        List<UUID> due = new ArrayList<>();
        ghosts.replaceAll((_, seconds) -> seconds == NO_TIMER ? NO_TIMER : seconds - 1);
        ghosts.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue() <= 0;
            if (expired) due.add(entry.getKey());
            return expired;
        });
        return due;
    }

    /** Everyone still in the run is a ghost. */
    public synchronized boolean wiped(Collection<UUID> present) {
        return !present.isEmpty() && ghosts.keySet().containsAll(present);
    }
}
