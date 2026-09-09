package net.swofty.type.skyblockgeneric.data;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatapointMinionDataTest {

    @Test
    void countsCraftedTiersRatherThanOnlyMinionTypes() {
        DatapointMinionData.ProfileMinionData data = dataWith(5, 1);

        assertEquals(5, data.getUniqueMinions());
        assertEquals(6, data.getSlots());
    }

    @Test
    void ignoresDuplicateCraftsWhenCountingSlots() {
        DatapointMinionData.ProfileMinionData data = new DatapointMinionData.ProfileMinionData(List.of(
                Map.entry("COBBLESTONE", List.of(1, 1, 2)),
                Map.entry("COBBLESTONE", List.of(2, 3)),
                Map.entry("COAL", List.of(1, 2))
        ));

        assertEquals(5, data.getUniqueMinions());
        assertEquals(6, data.getSlots());
    }

    @Test
    void followsTheFullMinionSlotThresholdLadder() {
        assertEquals(5, dataWith(0, 1).getSlots());
        assertEquals(6, dataWith(5, 1).getSlots());
        assertEquals(7, dataWith(15, 1).getSlots());
        assertEquals(8, dataWith(30, 1).getSlots());
        assertEquals(27, dataWith(700, 1).getSlots());
        assertEquals(27, dataWith(750, 1).getSlots());
    }

    private static DatapointMinionData.ProfileMinionData dataWith(int count, int firstTier) {
        return new DatapointMinionData.ProfileMinionData(IntStream.range(0, count)
                .mapToObj(index -> Map.entry("MINION_" + index, List.of(firstTier)))
                .toList());
    }
}
