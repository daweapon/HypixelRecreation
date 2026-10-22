package dev.daweapon.type.skyblockgeneric.consumable;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MushroomSoupFlightTest {
    @Test
    void warnsWhenCrossingEachThreshold() {
        assertEquals(List.of(60), MushroomSoupFlight.crossedWarnings(60_400, 59_900));
        assertEquals(List.of(10), MushroomSoupFlight.crossedWarnings(10_300, 9_800));
        assertEquals(List.of(), MushroomSoupFlight.crossedWarnings(59_000, 58_500));
    }

    @Test
    void largeGapsReportEveryCrossedThreshold() {
        assertEquals(List.of(5, 4, 3), MushroomSoupFlight.crossedWarnings(5_500, 2_900));
    }

    @Test
    void soupDurationsMatchTheWiki() {
        assertEquals(120_000, MushroomSoupFlight.MAGICAL_MILLIS);
        assertEquals(12_000_000, MushroomSoupFlight.MYSTICAL_MILLIS);
    }
}
