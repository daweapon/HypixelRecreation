package dev.daweapon.type.skyblockgeneric.minion;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinionOfflineProductionTest {
    private static final long MINUTE = 60_000L;

    @Test
    void oneItemPerTwoActions() {
        double interval = MinionOfflineProduction.actionIntervalMillis(14, 1, 1.0);
        assertEquals(21, MinionOfflineProduction.maxCycles(10 * MINUTE, interval, 2));
    }

    @Test
    void speedBoostShortensTheInterval() {
        double interval = MinionOfflineProduction.actionIntervalMillis(14, 1, 2.0);
        assertEquals(7000.0, interval, 1e-9);
        assertEquals(42, MinionOfflineProduction.maxCycles(10 * MINUTE, interval, 2));
    }

    @Test
    void fishingProducesOnEveryAction() {
        double interval = MinionOfflineProduction.actionIntervalMillis(75, 1, 1.0);
        assertEquals(8, MinionOfflineProduction.maxCycles(10 * MINUTE, interval, 1));
    }

    @Test
    void noElapsedTimeProducesNothing() {
        assertEquals(0, MinionOfflineProduction.maxCycles(0, 14000, 2));
        assertEquals(0, MinionOfflineProduction.maxCycles(-5, 14000, 2));
    }

    @Test
    void storageCapStopsTheCatchUp() {
        AtomicInteger stored = new AtomicInteger();
        long completed = MinionOfflineProduction.simulate(24 * 60 * MINUTE, 14000, 2,
                () -> 64 - stored.get(), stored::incrementAndGet);
        assertEquals(64, completed);
        assertEquals(64, stored.get());
    }

    @Test
    void unlimitedCapacityRunsEveryCycle() {
        AtomicInteger runs = new AtomicInteger();
        long completed = MinionOfflineProduction.simulate(MINUTE, 1000, 2,
                () -> Integer.MAX_VALUE, runs::incrementAndGet);
        assertEquals(30, completed);
        assertEquals(30, runs.get());
    }

    @Test
    void catchUpIsCappedAtThirtyDays() {
        long thirtyDays = 30L * 24 * 60 * MINUTE;
        long atCap = MinionOfflineProduction.maxCycles(thirtyDays, 10_000, 2);
        assertEquals(thirtyDays / 20_000, atCap);
        assertEquals(atCap, MinionOfflineProduction.maxCycles(thirtyDays * 3, 10_000, 2));
    }

    @Test
    void fractionalTierTimesAreNotTruncated() {
        assertEquals(9500.0, MinionOfflineProduction.actionIntervalMillis(9.5f, 1, 1.0), 1e-9);
        assertEquals(62500.0, MinionOfflineProduction.actionIntervalMillis(62.5f, 1, 1.0), 1e-9);
        assertEquals(5800.0, MinionOfflineProduction.actionIntervalMillis(5.8f, 1, 1.0), 1.0);
    }
}
