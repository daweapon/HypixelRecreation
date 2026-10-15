package dev.daweapon.type.catacombs.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Who is a ghost and, when the mode allows it, how long until each revives on their own. */
public final class GhostRoster {
    private final boolean autoRevive;
    private final int reviveSeconds;
    private final Map<UUID, Integer> ghosts = new HashMap<>();

    public GhostRoster(boolean autoRevive, int reviveSeconds) {
        this.autoRevive = autoRevive;
        this.reviveSeconds = reviveSeconds;
    }

    public synchronized boolean die(UUID player) {
        return ghosts.putIfAbsent(player, reviveSeconds) == null;
    }

    public synchronized boolean revive(UUID player) {
        return ghosts.remove(player) != null;
    }

    public synchronized boolean isGhost(UUID player) {
        return ghosts.containsKey(player);
    }

    public synchronized int secondsToRevive(UUID player) {
        return ghosts.getOrDefault(player, 0);
    }

    public synchronized int ghostCount() {
        return ghosts.size();
    }

    /** One second: the ghosts whose timer ran out, already revived here. Always empty when auto revive is off. */
    public synchronized List<UUID> tickSecond() {
        if (!autoRevive) return List.of();
        List<UUID> due = new ArrayList<>();
        ghosts.replaceAll((_, seconds) -> seconds - 1);
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
