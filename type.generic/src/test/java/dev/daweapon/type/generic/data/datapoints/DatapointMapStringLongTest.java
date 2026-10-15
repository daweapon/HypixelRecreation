package dev.daweapon.type.generic.data.datapoints;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatapointMapStringLongTest {
    @Test
    void smallSavedValuesLoadAsLongs() {
        DatapointMapStringLong datapoint = new DatapointMapStringLong("essence_perks");
        datapoint.deserializeValue("{\"ICE:FROZEN_SKIN\":3,\"CRIMSON:HEADSTART\":5000000000}");

        Map<String, Long> copy = new HashMap<>();
        datapoint.getValue().forEach((key, value) -> copy.put(key, value + 1));

        assertEquals(4L, copy.get("ICE:FROZEN_SKIN"));
        assertEquals(5_000_000_001L, copy.get("CRIMSON:HEADSTART"));
    }

    @Test
    void deepCloneKeepsLongs() {
        DatapointMapStringLong datapoint = new DatapointMapStringLong("essence", new HashMap<>(Map.of("ICE", 7L)));
        Long cloned = ((DatapointMapStringLong) datapoint.deepClone()).getValue().get("ICE");
        assertEquals(7L, cloned);
    }
}
