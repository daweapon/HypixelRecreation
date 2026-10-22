package dev.daweapon.type.skyblockgeneric.fishing.resolver;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FishFamilyDropsTest {
    @BeforeAll
    static void load() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
    }

    @Test
    void pityIsDueAfterTwentyFiveHundredTreasureCatches() {
        assertFalse(FishFamilyDrops.pityDue(2499));
        assertTrue(FishFamilyDrops.pityDue(2500));
        assertEquals(0.05, FishFamilyDrops.CHANCE_PERCENT);
    }

    @Test
    void eachTableDropsItsOwnFish() {
        assertEquals(ItemType.SWAMP_THE_FISH, FishFamilyDrops.fishFor("MOONGLADE_MARSH"));
        assertEquals(ItemType.SWAMP_THE_FISH, FishFamilyDrops.fishFor(TombFishing.TABLE_ID));
        assertEquals(ItemType.FISH_THE_FISH, FishFamilyDrops.fishFor("TORRHUS_CANYON"));
        assertNull(FishFamilyDrops.fishFor("CRYSTAL_HOLLOWS_LAVA"));
        assertEquals(FishFamilyDrops.counterKey(ItemType.SWAMP_THE_FISH), FishFamilyDrops.counterKey(FishFamilyDrops.fishFor(TombFishing.TABLE_ID)));
    }

    @Test
    void flexboneAmountsFollowTheTreasureTier() {
        Random random = new Random(7);
        assertEquals(1, TombFishing.flexboneAmount("GOOD", random));
        assertEquals(8, TombFishing.flexboneAmount("OUTSTANDING", random));
        for (int i = 0; i < 200; i++) {
            int amount = TombFishing.flexboneAmount("GREAT", random);
            assertTrue(amount >= 2 && amount <= 4, "amount " + amount);
        }
    }

    @Test
    void flexibleFisherAddsToTheBaseChance() {
        assertEquals(25D, TombFishing.flexbonePercent(0));
        assertEquals(27.5, TombFishing.flexbonePercent(2.5));
        assertEquals(50D, TombFishing.flexbonePercent(25));
        assertEquals(10D, TombFishing.treasureChanceBonus(TombFishing.TABLE_ID));
        assertEquals(0D, TombFishing.treasureChanceBonus("MOONGLADE_MARSH"));
    }

    @Test
    void tombTableComesBeforeTheMarshTableHasNoSeaCreaturesAndTheTombShardRows() {
        List<String> ids = FishingRegistry.getTables().stream().map(FishingTableDefinition::id).toList();
        assertTrue(ids.indexOf(TombFishing.TABLE_ID) >= 0);
        assertTrue(ids.indexOf(TombFishing.TABLE_ID) < ids.indexOf("MOONGLADE_MARSH"));
        FishingTableDefinition tomb = FishingRegistry.getTable(TombFishing.TABLE_ID);
        assertNotNull(tomb);
        assertEquals(List.of("TOMB_FLOODWAY"), tomb.regions());
        assertTrue(tomb.seaCreatures().isEmpty());
        assertTrue(tomb.hasQualityTreasure());
        FishingTableDefinition.TreasureQuality good = tomb.treasureQualities().getFirst();
        assertEquals(4, good.entries().stream().filter(loot -> loot.weight() < 6 && loot.weight() > 0.9).count());
        assertTrue(good.entries().stream().anyMatch(loot -> "SHARD_SALMON".equals(loot.itemId()) && loot.weight() == 0.95));
    }

    @Test
    void fishFamilyItemsExist() {
        assertNotNull(ItemType.get("SWAMP_THE_FISH"));
        assertNotNull(ItemType.get("FISH_THE_FISH"));
        assertNotNull(ItemType.get("MOB_THE_FISH"));
        assertNotNull(ItemType.get("FLEXBONE"));
    }
}
