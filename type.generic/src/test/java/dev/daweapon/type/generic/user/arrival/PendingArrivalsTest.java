package dev.daweapon.type.generic.user.arrival;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PendingArrivalsTest {
    private final AtomicLong now = new AtomicLong(1_000L);
    private final PendingArrivals arrivals = new PendingArrivals(now::get);
    private final UUID player = UUID.randomUUID();

    @Test
    void expectedArrivalIsConsumedOnce() {
        Pos museum = new Pos(-21.5, 72, 21.5, 90, 0);
        arrivals.expect(player, museum);

        assertEquals(museum, arrivals.consume(player));
        assertNull(arrivals.consume(player));
    }

    @Test
    void laterExpectationReplacesEarlierOne() {
        arrivals.expect(player, new Pos(1, 2, 3));
        arrivals.expect(player, new Pos(4, 5, 6));

        assertEquals(new Pos(4, 5, 6), arrivals.consume(player));
    }

    @Test
    void staleArrivalIsDiscarded() {
        arrivals.expect(player, new Pos(1, 2, 3));
        now.addAndGet(PendingArrivals.EXPIRY_MILLIS + 1);

        assertNull(arrivals.consume(player));
    }

    @Test
    void unknownPlayerHasNoArrival() {
        assertNull(arrivals.consume(UUID.randomUUID()));
    }
}
