package dev.daweapon.type.skyblockgeneric.fishing.galatea;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import dev.daweapon.type.skyblockgeneric.fishing.bestiary.FishingBestiaryFamily;
import dev.daweapon.type.skyblockgeneric.fishing.bestiary.FishingBestiarySection;
import dev.daweapon.type.skyblockgeneric.fishing.catches.ShardCatches;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GalateaFishingTest {
    private static final List<String> IDS = List.of("BOGGED", "WETWING", "TADGANG", "ENT", "THE_LOCH_EMPEROR", "NESSIE", "STRIDERSURFER");

    @BeforeAll
    static void load() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
    }

    @Test
    void sevenCreaturesAreSpawnableAndInTheBestiary() {
        assertEquals(IDS, GalateaSeaCreatures.PROFILES.stream().map(SeaCreatureProfile::id).toList());
        for (String id : IDS) {
            assertTrue(SeaCreatureSpawner.canSpawn(id), id);
            assertNotNull(FishingRegistry.getSeaCreature(id), id);
            assertEquals(FishingBestiarySection.MOONGLADE_MARSH, FishingBestiaryFamily.forMobKey(id).orElseThrow().getSection(), id);
        }
    }

    @Test
    void everyCreatureHasDrops() {
        for (SeaCreatureProfile profile : GalateaSeaCreatures.PROFILES) {
            assertTrue(profile.lootTable().getLootTable().size() >= 3, profile.id());
        }
    }

    @Test
    void waterTableWeightsMatchTheWikiAndSumToTenThousand() {
        FishingTableDefinition table = FishingRegistry.getTable("MOONGLADE_MARSH");
        assertNotNull(table);
        assertEquals(List.of(5000D, 2875D, 1500D, 500D, 100D, 25D),
                table.seaCreatures().stream().map(FishingTableDefinition.SeaCreatureRoll::weight).toList());
        assertEquals(10_000D, table.seaCreatures().stream().mapToDouble(FishingTableDefinition.SeaCreatureRoll::weight).sum());
        assertTrue(table.hasQualityTreasure());
        assertEquals(3, table.treasureQualities().size());
    }

    @Test
    void lavaTableOnlyCatchesTheStridersurfer() {
        FishingTableDefinition table = FishingRegistry.getTable("MOONGLADE_MARSH_LAVA");
        assertNotNull(table);
        assertEquals(List.of("STRIDERSURFER"),
                table.seaCreatures().stream().map(FishingTableDefinition.SeaCreatureRoll::seaCreatureId).toList());
    }

    @Test
    void treasureItemsExistAndShardsResolveToDefinitions() {
        FishingTableDefinition table = FishingRegistry.getTable("MOONGLADE_MARSH");
        table.treasureQualities().forEach(quality -> quality.entries().forEach(loot -> {
            if (FishingTableDefinition.COINS.equals(loot.itemId())) return;
            assertNotNull(ItemType.get(loot.itemId()), loot.itemId());
            if (loot.itemId().startsWith("SHARD_")) assertTrue(ShardCatches.definitionOf(loot.itemId()).isPresent(), loot.itemId());
        }));
    }
}
