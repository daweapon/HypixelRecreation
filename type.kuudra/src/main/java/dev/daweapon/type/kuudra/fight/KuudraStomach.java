package dev.daweapon.type.kuudra.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kuudra's stomach on Burning and up (research K1 3.4): who is inside, and the three pods. Each pickaxe hit takes
 * a share off a pod; the first pod broken stuns Kuudra. The pods grow back when the stun cycle starts again.
 */
public final class KuudraStomach {
    public enum Hit {NONE, DAMAGED, DESTROYED}

    public static final double POD_HEALTH = 100;

    private final Map<String, Double> pods = new LinkedHashMap<>();
    private final Map<UUID, Long> inside = new ConcurrentHashMap<>();
    private final Map<UUID, Long> nextHit = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> zoneTicks = new ConcurrentHashMap<>();
    private boolean open;

    public KuudraStomach(Collection<String> podNames) {
        if (podNames.isEmpty()) throw new IllegalArgumentException("Kuudra's stomach needs pods");
        podNames.forEach(name -> pods.put(name, POD_HEALTH));
    }

    /** Mining speed shortens the gap between counted hits; Mining Frenzy's first number (research K1 4). */
    public static int hitCooldown(int baseTicks, double speedFraction) {
        return Math.max(1, (int) Math.round(baseTicks / (1 + Math.max(0, speedFraction))));
    }

    /** Pod damage of one hit; Mining Frenzy's second number multiplies it (1x to 1.5x). */
    public static double podDamage(double basePercent, double multiplier) {
        return basePercent * Math.max(1, multiplier);
    }

    public void setOpen(boolean open) {
        this.open = open;
        if (!open) zoneTicks.clear();
    }

    public boolean isOpen() {
        return open;
    }

    public boolean eat(UUID player, long tick) {
        if (!open || inside.containsKey(player)) return false;
        inside.put(player, tick);
        zoneTicks.remove(player);
        return true;
    }

    public boolean isInside(UUID player) {
        return inside.containsKey(player);
    }

    public Set<UUID> inside() {
        return Set.copyOf(inside.keySet());
    }

    public boolean leave(UUID player) {
        nextHit.remove(player);
        return inside.remove(player) != null;
    }

    /** Everyone is flushed out at once (he submerged, or the stun ended). */
    public List<UUID> flush() {
        List<UUID> out = new ArrayList<>(inside.keySet());
        inside.clear();
        nextHit.clear();
        return out;
    }

    /** The tick the longest-staying player went in, or -1 when it is empty. */
    public long occupiedSince() {
        return inside.values().stream().mapToLong(Long::longValue).min().orElse(-1);
    }

    public Hit hit(UUID player, String pod, long tick, double percent, int cooldownTicks) {
        Double health = pods.get(pod);
        if (health == null || health <= 0 || !inside.containsKey(player)) return Hit.NONE;
        if (tick < nextHit.getOrDefault(player, Long.MIN_VALUE)) return Hit.NONE;
        nextHit.put(player, tick + cooldownTicks);
        double left = Math.max(0, health - percent);
        pods.put(pod, left);
        return left <= 0 ? Hit.DESTROYED : Hit.DAMAGED;
    }

    public double health(String pod) {
        return pods.getOrDefault(pod, 0D);
    }

    public Set<String> pods() {
        return pods.keySet();
    }

    public void restore() {
        pods.replaceAll((name, health) -> POD_HEALTH);
    }

    public void destroy(String pod) {
        if (pods.containsKey(pod)) pods.put(pod, 0D);
    }

    /**
     * A player standing in the Consumption Zone is swallowed after {@code neededTicks} in it; leaving the zone
     * starts the count again. True on the tick they are swallowed.
     */
    public boolean standInZone(UUID player, boolean inZone, int neededTicks) {
        if (!open || !inZone || inside.containsKey(player)) {
            zoneTicks.remove(player);
            return false;
        }
        int ticks = zoneTicks.merge(player, 1, Integer::sum);
        if (ticks < neededTicks) return false;
        zoneTicks.remove(player);
        return true;
    }

    public int zoneTicks(UUID player) {
        return zoneTicks.getOrDefault(player, 0);
    }
}
