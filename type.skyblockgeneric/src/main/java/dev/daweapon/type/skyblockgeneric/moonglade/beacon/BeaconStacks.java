package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import java.util.ArrayList;
import java.util.List;

public final class BeaconStacks {
    private final List<Long> expiries = new ArrayList<>();

    public synchronized void add(long expiresAt) {
        expiries.add(expiresAt);
    }

    public synchronized int active(long now) {
        expiries.removeIf(expiry -> expiry <= now);
        return expiries.size();
    }

    public synchronized long millisUntilNextExpiry(long now) {
        active(now);
        return expiries.stream().mapToLong(expiry -> expiry - now).min().orElse(0);
    }

    public synchronized void clear() {
        expiries.clear();
    }
}
