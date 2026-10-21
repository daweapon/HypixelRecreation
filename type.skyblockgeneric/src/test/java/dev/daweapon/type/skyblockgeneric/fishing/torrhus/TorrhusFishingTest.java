package dev.daweapon.type.skyblockgeneric.fishing.torrhus;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import dev.daweapon.type.skyblockgeneric.fishing.bestiary.FishingBestiaryFamily;
import dev.daweapon.type.skyblockgeneric.fishing.catches.ShardCatches;
import dev.daweapon.type.skyblockgeneric.fishing.hotspot.HotspotDefinition;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TorrhusFishingTest {
    private static final List<String> IDS = List.of("HAGGARD", "BRINELING", "SPRAWL", "TORRID", "SILKBREEZE", "GIANT_ISOPOD");

    @BeforeAll
    static void load() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
    }

    @Test
    void sixCreaturesAreSpawnableAndInTheBestiary() {
        assertEquals(IDS, TorrhusSeaCreatures.PROFILES.stream().map(SeaCreatureProfile::id).toList());
        for (String id : IDS) {
            assertTrue(SeaCreatureSpawner.canSpawn(id), id);
            assertNotNull(FishingRegistry.getSeaCreature(id), id);
            assertTrue(FishingBestiaryFamily.forMobKey(id).isPresent(), id);
        }
    }

    @Test
    void everyDropIsARealItem() {
        for (SeaCreatureProfile profile : TorrhusSeaCreatures.PROFILES) {
            assertTrue(profile.lootTable().getLootTable().size() >= 5, profile.id());
        }
    }

    @Test
    void tableWeightsMatchTheWikiAndSumToTenThousand() {
        FishingTableDefinition table = FishingRegistry.getTable("TORRHUS_CANYON");
        assertNotNull(table);
        assertEquals(List.of(5000D, 2875D, 1500D, 500D, 100D, 25D),
                table.seaCreatures().stream().map(FishingTableDefinition.SeaCreatureRoll::weight).toList());
        assertEquals(10_000D, table.seaCreatures().stream().mapToDouble(FishingTableDefinition.SeaCreatureRoll::weight).sum());
        assertTrue(table.hasQualityTreasure());
        assertEquals(3, table.treasureQualities().size());
    }

    @Test
    void treasureShardsResolveToDefinitions() {
        FishingTableDefinition table = FishingRegistry.getTable("TORRHUS_CANYON");
        table.treasureQualities().forEach(quality -> quality.entries().forEach(loot -> {
            assertNotNull(ItemType.get(loot.itemId()), loot.itemId());
            if (loot.itemId().startsWith("SHARD_")) assertTrue(ShardCatches.definitionOf(loot.itemId()).isPresent(), loot.itemId());
        }));
    }

    @Test
    void hotspotHavenSwapsGreatAndOutstandingForGiantWaterBugShards() {
        HotspotDefinition haven = FishingRegistry.getHotspots().stream()
                .filter(hotspot -> hotspot.id().equals("TORRHUS_HOTSPOT_HAVEN")).findFirst().orElseThrow();
        assertEquals(new HotspotDefinition.TreasureOverride("SHARD_GIANT_WATER_BUG", 1, 1), haven.treasureOverrides().get("GREAT"));
        assertEquals(new HotspotDefinition.TreasureOverride("SHARD_GIANT_WATER_BUG", 4, 8), haven.treasureOverrides().get("OUTSTANDING"));
        assertTrue(ShardCatches.definitionOf("SHARD_GIANT_WATER_BUG").isPresent());
    }
}
