package net.swofty.type.skyblockgeneric.hunting;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeLevelAwardsTest {
    @Test
    void oneXpPerLevel() {
        DatapointHunting.HuntingData data = new DatapointHunting.HuntingData();
        AttributeDefinition first = AttributeRegistry.values().iterator().next();
        data.addShards(first.id().toString(), 1);
        data.syphon(first.id().toString(), 1);
        assertEquals(1, AttributeLevelAwards.skyBlockXp(data));
    }

    @Test
    void everyAttributeMaxedStaysWithinTheSourceCap() {
        DatapointHunting.HuntingData data = new DatapointHunting.HuntingData();
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            String id = definition.id().toString();
            data.addShards(id, definition.rarity().cumulativeForLevel(10));
            data.syphon(id, definition.rarity().cumulativeForLevel(10));
        }
        int total = AttributeLevelAwards.skyBlockXp(data);
        assertEquals(Math.min(AttributeRegistry.values().size() * 10, SkyBlockXPSource.ATTRIBUTE_LEVELS.getMaxXP()), total);
        assertTrue(total <= SkyBlockXPSource.ATTRIBUTE_LEVELS.getMaxXP());
    }
}
