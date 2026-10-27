package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateShopStock;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShardPurchaseSourcesTest {
    @Test
    void miriaSellsWoodpeckerAtMilestoneTwoAndVultureAtMilestoneFour() {
        StarlynConfig config = StarlynConfig.get(StarlynSister.MIRIA);
        StarlynConfig.Item woodpecker = assertInstanceOf(StarlynConfig.Item.class, config.entry("woodpecker_shard"));
        StarlynConfig.Item vulture = assertInstanceOf(StarlynConfig.Item.class, config.entry("vulture_shard"));
        assertEquals(2, woodpecker.milestone());
        assertEquals("U76", woodpecker.give().shard());
        assertEquals(4, vulture.milestone());
        assertEquals("R19", vulture.give().shard());
        assertNotNull(AttributeRegistry.get("U76"));
        assertNotNull(AttributeRegistry.get("R19"));
    }

    @Test
    void chocolateShopRabbitShardsAreLimitedPerYear() {
        for (String id : new String[]{"C22", "U106", "R94", "E46", "U73"})
            assertNotNull(AttributeRegistry.get(id), id);
        assertEquals(24, ChocolateShopStock.annualStock("SHARD_RABBIT_MAFIOSO"));
        assertEquals(6, ChocolateShopStock.annualStock("SHARD_RABBIT_GODMOTHER"));
    }
}
