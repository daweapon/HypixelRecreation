package net.swofty.type.kuudra.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deaths in the run (research K1 3.9): an Auto Revive brings a player straight back at half health; otherwise
 * they become a ghost until the respawn timer (shortened by Faster Respawn) runs out. Final kills (Chaosmites)
 * wait for a Support revive. Everyone dead at once is a wipe.
 */
public final class KuudraLives {
    public enum Death {AUTO_REVIVED, GHOST, FINAL}

    private record Ghost(long respawnTick, boolean finalKill) {
    }

    private final Map<UUID, Ghost> ghosts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> invulnerableUntil = new ConcurrentHashMap<>();
    private final int respawnTicks;
    private final int invulnerableTicks;

    public KuudraLives(int respawnSeconds, int invulnerableSeconds) {
        this.respawnTicks = respawnSeconds * 20;
        this.invulnerableTicks = invulnerableSeconds * 20;
    }

    public Death die(UUID player, long tick, boolean finalKill, boolean autoRevive, double fasterRespawn) {
        if (ghosts.containsKey(player)) return ghosts.get(player).finalKill() ? Death.FINAL : Death.GHOST;
        if (autoRevive && !finalKill) {
            invulnerableUntil.put(player, tick + invulnerableTicks);
            return Death.AUTO_REVIVED;
        }
        long respawn = finalKill ? Long.MAX_VALUE : tick + respawnTicks(fasterRespawn);
        ghosts.put(player, new Ghost(respawn, finalKill));
        return finalKill ? Death.FINAL : Death.GHOST;
    }

    public int respawnTicks(double fasterRespawn) {
        return (int) Math.ceil(respawnTicks * (1 - Math.min(1, Math.max(0, fasterRespawn))));
    }

    /** Players whose respawn timer ran out this tick; they are alive again and briefly invulnerable. */
    public List<UUID> tick(long tick) {
        List<UUID> back = new ArrayList<>();
        ghosts.forEach((player, ghost) -> {
            if (ghost.respawnTick() <= tick) back.add(player);
        });
        back.forEach(player -> revive(player, tick));
        return back;
    }

    public boolean revive(UUID player, long tick) {
        if (ghosts.remove(player) == null) return false;
        invulnerableUntil.put(player, tick + invulnerableTicks);
        return true;
    }

    public boolean isGhost(UUID player) {
        return ghosts.containsKey(player);
    }

    public boolean isFinal(UUID player) {
        Ghost ghost = ghosts.get(player);
        return ghost != null && ghost.finalKill();
    }

    public boolean isInvulnerable(UUID player, long tick) {
        return ghosts.containsKey(player) || invulnerableUntil.getOrDefault(player, Long.MIN_VALUE) > tick;
    }

    public int secondsToRespawn(UUID player, long tick) {
        Ghost ghost = ghosts.get(player);
        if (ghost == null || ghost.finalKill()) return -1;
        return (int) Math.max(0, (ghost.respawnTick() - tick + 19) / 20);
    }

    public boolean wiped(Collection<UUID> present) {
        return !present.isEmpty() && present.stream().allMatch(ghosts::containsKey);
    }

    public List<UUID> ghosts() {
        return List.copyOf(ghosts.keySet());
    }
}
