package dev.daweapon.type.skyblockgeneric.garden.tool;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverclockerPityTest {
    @Test
    void greenhouseStartsAtTheWikiBaseChanceAndGuaranteesEventually() {
        assertEquals(0.0005, OverclockerPity.chance(OverclockerPity.Source.GREENHOUSE, 0), 1e-12);
        assertEquals(0.5, OverclockerPity.chance(OverclockerPity.Source.GREENHOUSE, 2_000), 1e-12);
        assertEquals(1.0, OverclockerPity.chance(OverclockerPity.Source.GREENHOUSE, 4_000), 1e-12);
    }

    @Test
    void sourcesWithoutPublishedRatesNeverDrop() {
        OverclockerPity pity = new OverclockerPity();
        assertFalse(pity.activity(OverclockerPity.Source.PESTS, null, new Random(1)));
        assertEquals(0, pity.counter(OverclockerPity.Source.PESTS));
    }

    @Test
    void counterGrowsUntilTheGuaranteedDropThenResets() {
        OverclockerPity pity = new OverclockerPity();
        Random random = new Random(7);
        int activities = 0;
        boolean dropped = false;
        while (!dropped && activities < 4_001) {
            dropped = pity.activity(OverclockerPity.Source.GREENHOUSE, null, random);
            activities++;
        }
        assertTrue(dropped);
        assertTrue(activities <= 4_000);
        assertEquals(0, pity.counter(OverclockerPity.Source.GREENHOUSE));
    }

    @Test
    void progressSurvivesAJsonRoundTrip() {
        OverclockerPity pity = new OverclockerPity();
        pity.activity(OverclockerPity.Source.GREENHOUSE, null, new Random(3));
        int counter = pity.counter(OverclockerPity.Source.GREENHOUSE);
        JSONObject json = pity.toJson();
        assertEquals(counter, OverclockerPity.fromJson(json).counter(OverclockerPity.Source.GREENHOUSE));
    }
}
