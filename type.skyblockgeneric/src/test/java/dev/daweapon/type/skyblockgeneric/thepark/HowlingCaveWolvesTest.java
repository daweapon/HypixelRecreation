package dev.daweapon.type.skyblockgeneric.thepark;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.data.regions.RegionCatalog;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark.HowlingCaveMobSpecs;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HowlingCaveWolvesTest {
    private static final BestiaryData BESTIARY = new BestiaryData();

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    private static void assertProfile(CrystalHollowsMobSpec spec, String id, int level, double health, double damage,
                                      long xp, int coins, int orbs) {
        assertEquals(id, spec.id());
        assertEquals(level, spec.level(), id + " level");
        assertEquals(health, spec.health(), id + " health");
        assertEquals(damage, spec.damage(), id + " damage");
        assertEquals(xp, spec.combatXp(), id + " combat xp");
        assertEquals(coins, spec.coins(), id + " coins");
        assertEquals(orbs, spec.orbs(), id + " orbs");
        assertEquals(List.of(MobType.ANIMAL, MobType.ARCANE), spec.types(), id + " types");
    }

    private static Map<ItemType, Double> chances(CrystalHollowsMobSpec spec) {
        return spec.drops().stream().collect(Collectors.toMap(CrystalHollowsMobSpec.Drop::item, CrystalHollowsMobSpec.Drop::chance));
    }

    @Test
    void statsMatchTheWiki() {
        assertProfile(HowlingCaveMobSpecs.PACK_SPIRIT, "PACK_SPIRIT_30", 43, 6_000, 300, 64, 14, 47);
        assertProfile(HowlingCaveMobSpecs.HOWLING_SPIRIT, "HOWLING_SPIRIT_35", 51, 7_000, 500, 76, 17, 56);
        assertProfile(HowlingCaveMobSpecs.SOUL_OF_THE_ALPHA, "SOUL_OF_THE_ALPHA_55", 90, 31_150, 1_425, 135, 30, 99);
    }

    @Test
    void packSpiritDrops() {
        assertEquals(Map.of(ItemType.BONE, 50.0, ItemType.OAK_LOG, 15.0, ItemType.BIRCH_LOG, 15.0,
                ItemType.PARK_CAVE_TRAVEL_SCROLL, 0.02), chances(HowlingCaveMobSpecs.PACK_SPIRIT));
    }

    @Test
    void howlingSpiritDrops() {
        assertEquals(Map.of(ItemType.SPRUCE_LOG, 15.0, ItemType.DARK_OAK_LOG, 15.0, ItemType.ACACIA_LOG, 15.0,
                ItemType.PARK_CAVE_TRAVEL_SCROLL, 0.02), chances(HowlingCaveMobSpecs.HOWLING_SPIRIT));
    }

    @Test
    void soulOfTheAlphaDrops() {
        assertEquals(Map.of(ItemType.JUNGLE_LOG, 100.0, ItemType.WEAK_WOLF_CATALYST, 0.089,
                ItemType.PARK_CAVE_TRAVEL_SCROLL, 0.035, ItemType.PET_ITEM_FORAGING_SKILL_BOOST_EPIC, 0.01),
                chances(HowlingCaveMobSpecs.SOUL_OF_THE_ALPHA));
    }

    @Test
    void houndPetRollsOneRarityPerKill() {
        assertNotNull(ItemType.get(HowlingCaveMobSpecs.HOUND_PET));
        assertEquals(List.of(new HowlingCaveMobSpecs.HoundChance(Rarity.EPIC, 0.003),
                new HowlingCaveMobSpecs.HoundChance(Rarity.LEGENDARY, 0.001)), HowlingCaveMobSpecs.HOUND_CHANCES);
    }

    @Test
    void bestiaryBracketsMatchTheWiki() {
        assertEquals(3, HowlingCaveMobSpecs.PACK_SPIRIT.bracket());
        assertEquals(3, HowlingCaveMobSpecs.HOWLING_SPIRIT.bracket());
        assertEquals(4, HowlingCaveMobSpecs.SOUL_OF_THE_ALPHA.bracket());
        assertEquals(4_000, BESTIARY.getTotalKillsForMaxTier(3, 15));
        assertEquals(1_000, BESTIARY.getTotalKillsForMaxTier(4, 15));
    }

    @Test
    void theParkBestiaryHoldsTheLiveWolves() {
        Map<String, BestiaryMob> mobs = BestiaryCategories.THE_PARK.getAllMobs().stream()
                .collect(Collectors.toMap(BestiaryMob::getMobID, mob -> mob));
        assertEquals(15, mobs.get("PACK_SPIRIT_30").getMaxBestiaryTier());
        assertEquals(4_000, BESTIARY.getTotalKillsForMaxTier(mobs.get("PACK_SPIRIT_30")));
        assertEquals(4_000, BESTIARY.getTotalKillsForMaxTier(mobs.get("HOWLING_SPIRIT_35")));
        assertEquals(1_000, BESTIARY.getTotalKillsForMaxTier(mobs.get("SOUL_OF_THE_ALPHA_55")));
    }

    @Test
    void fourAlphaSpotsWithSpiritsGroupedAroundThem() {
        assertEquals(4, HowlingCavePacks.PACKS.size());
        assertEquals(15_000, HowlingCavePacks.RESPAWN_MILLIS);
        for (HowlingCavePacks.Pack pack : HowlingCavePacks.PACKS) {
            assertTrue(pack.spirits().size() >= 3, pack.name());
            for (HowlingCavePacks.Spirit spirit : pack.spirits()) {
                assertTrue(spirit.spot().distance(pack.alpha()) <= 12, pack.name() + " spirit too far from its alpha");
            }
        }
    }

    @Test
    void everySpawnSpotIsInsideTheHowlingCaveRegion() {
        SkyBlockRegion cave = RegionCatalog.getAllRegions().stream()
                .filter(region -> region.getType() == RegionType.HOWLING_CAVE).findFirst().orElseThrow();
        for (HowlingCavePacks.Pack pack : HowlingCavePacks.PACKS) {
            assertTrue(cave.insideRegion(pack.alpha()), pack.name());
            for (HowlingCavePacks.Spirit spirit : pack.spirits()) assertTrue(cave.insideRegion(spirit.spot()), pack.name());
        }
        Pos centre = HowlingCavePacks.CAVE_CENTRE;
        assertTrue(cave.insideRegion(centre));
    }

    @Test
    void shardsComeFromTheirWolves() {
        Assumptions.assumeTrue(Files.exists(Path.of("../configuration/skyblock/shards.yml"))
                || Files.exists(Path.of("configuration/skyblock/shards.yml")), "the server configuration must be reachable");
        assertTrue(mobSources("C6").containsAll(List.of("PACK_SPIRIT_30", "HOWLING_SPIRIT_35")));
        assertTrue(mobSources("U30").contains("SOUL_OF_THE_ALPHA_55"));
        assertTrue(AttributeRegistry.get("C6").isAvailable());
    }

    private static List<String> mobSources(String shard) {
        return AttributeRegistry.get(shard).sources().stream()
                .filter(source -> source.type() == AttributeDefinition.SourceType.MOB)
                .flatMap(source -> source.mobIds().stream()).toList();
    }
}
