package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpiderDenRelicsTest {
    @Test
    void twentyEightDistinctRelicsInsideTheDen() {
        assertEquals(28, SpiderDenRelics.total());
        Set<String> seen = new HashSet<>();
        for (int[] at : SpiderDenRelics.LOCATIONS) {
            assertTrue(seen.add(at[0] + "," + at[1] + "," + at[2]));
            assertTrue(at[0] >= -512 && at[0] <= -17 && at[2] >= -480 && at[2] <= -17 && at[1] >= 20 && at[1] <= 160, java.util.Arrays.toString(at));
        }
    }

    @Test
    void rewardsMatchTheWiki() {
        assertEquals(10_000, SpiderDenRelics.COINS_PER_RELIC);
        assertEquals(30_000, SpiderDenRelics.COMPLETION_COINS);
    }

    @Test
    void shaggyCrystalCost() {
        assertEquals(2, ArachneCrystalTrade.COST.get(ItemType.ARACHNE_FRAGMENT));
        assertEquals(16, ArachneCrystalTrade.COST.get(ItemType.ENCHANTED_SPIDER_EYE));
        assertEquals(16, ArachneCrystalTrade.COST.get(ItemType.ENCHANTED_STRING));
    }

    @Test
    void spiderTamerHasSixFacts() {
        assertEquals(6, SpiderTamerCall.FACTS.size());
    }
}
