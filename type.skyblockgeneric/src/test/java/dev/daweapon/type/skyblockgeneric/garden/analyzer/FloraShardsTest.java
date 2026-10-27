package dev.daweapon.type.skyblockgeneric.garden.analyzer;

import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloraShardsTest {
    @Test
    void oneShardPerThousandThreeHundredThirtyThreeCopper() {
        GardenProfile profile = new GardenProfile();
        assertEquals(0, profile.takeFloraShards(3333, FloraShards.COPPER_PER_SHARD));
        assertEquals(1, profile.takeFloraShards(1, FloraShards.COPPER_PER_SHARD));
    }

    @Test
    void threePercentPerHundredCopperAddsUpAcrossAnalyses() {
        GardenProfile profile = new GardenProfile();
        int total = 0;
        for (int i = 0; i < 100; i++) total += profile.takeFloraShards(100, FloraShards.COPPER_PER_SHARD);
        assertEquals(3, total);
    }

    @Test
    void oneBigAnalysisCanYieldSeveralShards() {
        GardenProfile profile = new GardenProfile();
        assertEquals(24, profile.takeFloraShards(80_000, FloraShards.COPPER_PER_SHARD));
    }

    @Test
    void remainderSurvivesSerialisation() {
        GardenProfile profile = new GardenProfile();
        profile.takeFloraShards(3000, FloraShards.COPPER_PER_SHARD);
        GardenProfile loaded = GardenProfile.deserialize(profile.serialize());
        assertEquals(1, loaded.takeFloraShards(334, FloraShards.COPPER_PER_SHARD));
    }
}
