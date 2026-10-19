package dev.daweapon.type.theend.dragon;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class EyeLedger {
    public static final int FRAMES = 8;
    public static final long WITHDRAW_COOLDOWN_MILLIS = 3_000;

    public record PlacedEye(UUID owner, boolean recombobulated) {
    }

    private final Map<Integer, PlacedEye> placed = new HashMap<>();
    private final Map<UUID, Long> lastWithdraw = new HashMap<>();

    public boolean place(int frame, UUID owner, boolean recombobulated) {
        if (frame < 0 || frame >= FRAMES || placed.containsKey(frame)) return false;
        placed.put(frame, new PlacedEye(owner, recombobulated));
        return true;
    }

    public boolean onCooldown(UUID player, long nowMillis) {
        Long last = lastWithdraw.get(player);
        return last != null && nowMillis - last < WITHDRAW_COOLDOWN_MILLIS;
    }

    public Optional<PlacedEye> withdraw(int frame, UUID player, long nowMillis) {
        PlacedEye eye = placed.get(frame);
        if (eye == null || !eye.owner().equals(player) || onCooldown(player, nowMillis)) return Optional.empty();
        placed.remove(frame);
        lastWithdraw.put(player, nowMillis);
        return Optional.of(eye);
    }

    public Optional<PlacedEye> at(int frame) {
        return Optional.ofNullable(placed.get(frame));
    }

    public int count() {
        return placed.size();
    }

    public int recombobulated() {
        return (int) placed.values().stream().filter(PlacedEye::recombobulated).count();
    }

    public boolean complete() {
        return placed.size() == FRAMES;
    }

    public Map<UUID, Integer> eyesByPlayer() {
        Map<UUID, Integer> counts = new HashMap<>();
        placed.values().forEach(eye -> counts.merge(eye.owner(), 1, Integer::sum));
        return counts;
    }

    public void clear() {
        placed.clear();
    }
}
