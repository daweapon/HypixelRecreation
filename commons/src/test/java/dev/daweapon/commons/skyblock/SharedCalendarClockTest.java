package dev.daweapon.commons.skyblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SharedCalendarClockTest {
    @Test
    void advancesOneUnitPerFiftyMilliseconds() {
        assertEquals(0L, SharedCalendarClock.elapsedAt(1_000L, 1_000L));
        assertEquals(20L, SharedCalendarClock.elapsedAt(1_000L, 2_000L));
        assertEquals(24_000L, SharedCalendarClock.elapsedAt(0L, 20L * 60L * 1000L));
    }

    @Test
    void neverReportsTimeBeforeTheEpoch() {
        assertEquals(0L, SharedCalendarClock.elapsedAt(5_000L, 1_000L));
    }

    @Test
    void epochForRoundTripsThroughElapsedAt() {
        long now = 1_762_000_000_000L;
        long elapsed = 8_928_000L * 3 + 744_000L * 5 + 12_345L;
        long epoch = SharedCalendarClock.epochFor(elapsed, now);
        assertEquals(elapsed, SharedCalendarClock.elapsedAt(epoch, now));
        assertEquals(elapsed + 20, SharedCalendarClock.elapsedAt(epoch, now + 1_000L));
    }

    @Test
    void twoServersReadingTheSameEpochAgree() {
        long epoch = SharedCalendarClock.epochFor(400_000L, 10_000_000L);
        assertEquals(SharedCalendarClock.elapsedAt(epoch, 10_500_000L),
                SharedCalendarClock.elapsedAt(epoch, 10_500_000L));
    }

    @Test
    void parsesOnlyNumbers() {
        assertEquals(42L, SharedCalendarClock.parse(" 42 "));
        assertNull(SharedCalendarClock.parse(null));
        assertNull(SharedCalendarClock.parse(""));
        assertNull(SharedCalendarClock.parse("soon"));
    }
}
