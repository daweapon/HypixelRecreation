package dev.daweapon.type.skyblockgeneric.island;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandUtilityBlocksTest {
    @Test
    void placedBlocksSurviveASerializeParseRoundTrip() {
        Map<String, String> placed = new LinkedHashMap<>();
        placed.put("1,64,-3", "REPELLING_CANDLE");
        placed.put("-8,70,12", "SILENCE_BLOCK");
        assertEquals(placed, IslandUtilityBlocks.parse(IslandUtilityBlocks.serialize(placed)));
    }

    @Test
    void blankOrMalformedStorageParsesToNothing() {
        assertTrue(IslandUtilityBlocks.parse("").isEmpty());
        assertTrue(IslandUtilityBlocks.parse(null).isEmpty());
        assertTrue(IslandUtilityBlocks.parse("1,2,3").isEmpty());
    }

    @Test
    void mithrilCrystalGivesTenPercentOnlyToMiningMinions() {
        assertEquals(10, IslandUtilityBlocks.MITHRIL_CRYSTAL_MINION_SPEED_PERCENT, 1e-9);
        assertEquals(0, IslandUtilityBlocks.minionSpeedPercent(null, false), 1e-9);
    }
}
