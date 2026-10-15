package dev.daweapon.type.generic.user.arrival;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class PendingArrivals {
    public static final long EXPIRY_MILLIS = 60_000L;
    private static final PendingArrivals INSTANCE = new PendingArrivals(System::currentTimeMillis);

    private final Map<UUID, Arrival> arrivals = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    PendingArrivals(LongSupplier clock) {
        this.clock = clock;
    }

    public static PendingArrivals get() {
        return INSTANCE;
    }

    public void expect(UUID player, Pos position) {
        arrivals.put(player, new Arrival(position, clock.getAsLong() + EXPIRY_MILLIS));
    }

    public @Nullable Pos consume(UUID player) {
        Arrival arrival = arrivals.remove(player);
        if (arrival == null || arrival.expiresAt() < clock.getAsLong()) return null;
        return arrival.position();
    }

    private record Arrival(Pos position, long expiresAt) {}
}
