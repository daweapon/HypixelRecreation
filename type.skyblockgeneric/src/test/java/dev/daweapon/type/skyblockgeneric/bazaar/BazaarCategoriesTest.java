package dev.daweapon.type.skyblockgeneric.bazaar;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar.BazaarSetLayout;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarCategoriesTest {
    private static final int CATEGORY_TILE_LIMIT = 24;
    private static final int NESTED_SET_LIMIT = 2 * BazaarSetLayout.PAGE_CAPACITY;

    private static final Map<BazaarCategories, List<String>> REAL_TILE_ORDER = Map.of(
            BazaarCategories.FARMING, List.of("Wheat & Seeds", "Carrot", "Potato", "Pumpkin", "Melon", "Mushrooms",
                    "Cocoa Beans", "Cactus", "Sugar Cane", "Sunflower", "Moonflower", "Wild Rose", "Leather & Beef",
                    "Pork", "Chicken & Feather", "Mutton", "Rabbit", "Nether Warts", "Garden"),
            BazaarCategories.MINING, List.of("Cobblestone", "Coal", "Iron", "Gold", "Diamond", "Lapis", "Emerald",
                    "Redstone", "Obsidian", "End Stone", "Flint & Gravel", "Sand", "Ice", "Snow", "Hard Stone",
                    "Gemstones", "Goblin Eggs", "Dwarven Mines", "Forge", "Crystal Hollows", "Crimson Isle",
                    "Glacite Tunnels", "Fossils"),
            BazaarCategories.COMBAT, List.of("Rotten Flesh", "Bone", "Arachnids", "Gunpowder", "Ender Pearl",
                    "Ghast Tear", "Slimeball", "Magma Cream", "Blaze Rod", "The End", "Crimson Isle", "Slayers",
                    "Catacombs"),
            BazaarCategories.WOODS_AND_FISHES, List.of("Wood", "Fish", "Flowers", "Prismarine", "Clay", "Sponge",
                    "Lily Pad", "Ink Sac", "Baits", "Mob Drops", "Magmafish", "Lava Fishing", "Water Orbs",
                    "Chum Fishing", "Backwater Bayou", "Moonglade Marsh", "Torrhus Canyon", "Lotus Atoll"),
            BazaarCategories.ODDITIES, List.of("Booster Cookie", "Modifiers", "Enchantments", "EXP Bottles", "Shards",
                    "Reforge Stones", "Power Stones", "Stock of Stonks", "Pet Upgrades", "Inferno Minion",
                    "Minion Upgrades", "Fuels", "Essence", "Events", "Consumables", "Cross-Dimensional", "Upgrade Fragments")
    );

    @Test
    void everyItemBelongsToExactlyOneGroup() {
        Map<ItemType, String> owner = new HashMap<>();
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                assertFalse(set.products().isEmpty(), set.name());
                for (ItemType type : set.items) {
                    assertNotNull(type, set.displayName);
                    String where = category + "/" + set.displayName;
                    String previous = owner.put(type, where);
                    assertEquals(null, previous, type.name() + " is in both " + previous + " and " + where);
                }
            }
        }
        assertFalse(owner.isEmpty());
    }

    @Test
    void everyItemTypeExists() {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                for (ItemType type : set.items) {
                    assertSame(type, ItemType.valueOf(type.name()), type.name());
                }
            }
        }
    }

    @Test
    void displayItemIsPartOfItsSet() {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                if (set.displayMaterial == null || set.items.contains(set.displayMaterial)) continue;
                assertNotNull(ItemType.valueOf(set.displayMaterial.name()));
            }
        }
    }

    @Test
    void categoriesFitTheirGrids() {
        for (BazaarCategories category : BazaarCategories.values()) {
            List<BazaarTile> tiles = category.tiles();
            assertTrue(tiles.size() <= CATEGORY_TILE_LIMIT, category + " has " + tiles.size() + " tiles");
            for (BazaarTile tile : tiles) {
                if (tile.nested()) {
                    assertTrue(tile.sets().size() <= NESTED_SET_LIMIT, category + "/" + tile.name() + " has " + tile.sets().size() + " sets");
                }
            }
        }
    }

    @Test
    void tilesFollowTheRealBazaarOrder() {
        for (BazaarCategories category : BazaarCategories.values()) {
            assertEquals(REAL_TILE_ORDER.get(category), category.tiles().stream().map(BazaarTile::name).toList(), category.name());
        }
    }

    @Test
    void groupsListTheProductsOfTheRealBazaarInOrder() {
        expect(BazaarCategories.FARMING, null, "Carrot",
                ItemType.CARROT, ItemType.ENCHANTED_CARROT, ItemType.ENCHANTED_CARROT_ON_A_STICK, ItemType.ENCHANTED_GOLDEN_CARROT);
        expect(BazaarCategories.FARMING, null, "Potato",
                ItemType.POTATO, ItemType.POISONOUS_POTATO, ItemType.ENCHANTED_POTATO, ItemType.ENCHANTED_POISONOUS_POTATO, ItemType.ENCHANTED_BAKED_POTATO);
        expect(BazaarCategories.FARMING, null, "Melon",
                ItemType.MELON_SLICE, ItemType.ENCHANTED_MELON, ItemType.ENCHANTED_GLISTERING_MELON, ItemType.MELON_BLOCK, ItemType.ENCHANTED_MELON_BLOCK);
        expect(BazaarCategories.FARMING, null, "Sugar Cane",
                ItemType.SUGAR_CANE, ItemType.ENCHANTED_SUGAR, ItemType.ENCHANTED_PAPER, ItemType.ENCHANTED_SUGAR_CANE);
        expect(BazaarCategories.FARMING, null, "Moonflower",
                ItemType.MOONFLOWER, ItemType.ENCHANTED_MOONFLOWER, ItemType.COMPACTED_MOONFLOWER);
        expect(BazaarCategories.FARMING, null, "Wild Rose",
                ItemType.WILD_ROSE, ItemType.ENCHANTED_WILD_ROSE, ItemType.COMPACTED_WILD_ROSE);
        expect(BazaarCategories.FARMING, null, "Chicken & Feather",
                ItemType.RAW_CHICKEN, ItemType.ENCHANTED_RAW_CHICKEN, ItemType.EGG, ItemType.ENCHANTED_CAKE, ItemType.ENCHANTED_EGG, ItemType.SUPER_ENCHANTED_EGG, ItemType.OMEGA_ENCHANTED_EGG, ItemType.FEATHER, ItemType.ENCHANTED_FEATHER);
        expect(BazaarCategories.FARMING, null, "Rabbit",
                ItemType.RABBIT_FOOT, ItemType.ENCHANTED_RABBIT_FOOT, ItemType.RABBIT_HIDE, ItemType.ENCHANTED_RABBIT_HIDE, ItemType.RAW_RABBIT, ItemType.ENCHANTED_RAW_RABBIT, ItemType.ENCHANTED_COOKED_RABBIT);
        expect(BazaarCategories.MINING, null, "Coal",
                ItemType.COAL, ItemType.ENCHANTED_COAL, ItemType.ENCHANTED_CHARCOAL, ItemType.ENCHANTED_COAL_BLOCK);
        expect(BazaarCategories.MINING, null, "Gold",
                ItemType.GOLD_INGOT, ItemType.ENCHANTED_GOLD_INGOT, ItemType.ENCHANTED_GOLD_BLOCK);
        expect(BazaarCategories.MINING, null, "Emerald",
                ItemType.EMERALD, ItemType.ENCHANTED_EMERALD, ItemType.ENCHANTED_EMERALD_BLOCK);
        expect(BazaarCategories.MINING, null, "Obsidian",
                ItemType.OBSIDIAN, ItemType.ENCHANTED_OBSIDIAN);
        expect(BazaarCategories.MINING, null, "Sand",
                ItemType.SAND, ItemType.ENCHANTED_SAND);
        expect(BazaarCategories.MINING, null, "Snow",
                ItemType.SNOWBALL, ItemType.SNOW_BLOCK, ItemType.ENCHANTED_SNOW_BLOCK);
        expect(BazaarCategories.MINING, null, "Hard Stone",
                ItemType.HARD_STONE, ItemType.ENCHANTED_HARD_STONE, ItemType.CONCENTRATED_STONE);
        expect(BazaarCategories.MINING, null, "Goblin Eggs",
                ItemType.GOBLIN_EGG, ItemType.RED_GOBLIN_EGG, ItemType.BLUE_GOBLIN_EGG, ItemType.YELLOW_GOBLIN_EGG, ItemType.GREEN_GOBLIN_EGG);
        expect(BazaarCategories.MINING, null, "Dwarven Mines",
                ItemType.MITHRIL, ItemType.ENCHANTED_MITHRIL, ItemType.REFINED_MITHRIL, ItemType.TITANIUM, ItemType.ENCHANTED_TITANIUM, ItemType.REFINED_TITANIUM, ItemType.STARFALL, ItemType.TREASURITE, ItemType.SORROW);
        expect(BazaarCategories.MINING, null, "Forge",
                ItemType.PLASMA, ItemType.GLACITE_JEWEL, ItemType.FUEL_TANK, ItemType.BEJEWELED_HANDLE, ItemType.GEMSTONE_MIXTURE, ItemType.MITHRIL_PLATE, ItemType.DRILL_ENGINE, ItemType.GOLDEN_PLATE);
        expect(BazaarCategories.MINING, null, "Glacite Tunnels",
                ItemType.UMBER, ItemType.ENCHANTED_UMBER, ItemType.REFINED_UMBER, ItemType.UMBER_PLATE, ItemType.UMBER_KEY, ItemType.TUNGSTEN, ItemType.ENCHANTED_TUNGSTEN, ItemType.REFINED_TUNGSTEN, ItemType.TUNGSTEN_PLATE, ItemType.TUNGSTEN_KEY, ItemType.GLACITE, ItemType.ENCHANTED_GLACITE, ItemType.GLACITE_AMALGAMATION, ItemType.PERFECT_PLATE, ItemType.SKELETON_KEY, ItemType.SUSPICIOUS_SCRAP);
        expect(BazaarCategories.MINING, null, "Fossils",
                ItemType.FOOTPRINT_FOSSIL, ItemType.WEBBED_FOSSIL, ItemType.UGLY_FOSSIL, ItemType.TUSK_FOSSIL, ItemType.SPINE_FOSSIL, ItemType.CLUBBED_FOSSIL, ItemType.CLAW_FOSSIL, ItemType.HELIX);
        expect(BazaarCategories.COMBAT, null, "Rotten Flesh",
                ItemType.ROTTEN_FLESH, ItemType.ENCHANTED_ROTTEN_FLESH);
        expect(BazaarCategories.COMBAT, null, "Arachnids",
                ItemType.STRING, ItemType.ENCHANTED_STRING, ItemType.SPIDER_EYE, ItemType.ENCHANTED_SPIDER_EYE, ItemType.ENCHANTED_FERMENTED_SPIDER_EYE, ItemType.SOUL_STRING, ItemType.ARACHNE_KEEPER_FRAGMENT, ItemType.ARACHNE_FRAGMENT, ItemType.ARACHNE_FANG);
        expect(BazaarCategories.COMBAT, null, "Ender Pearl",
                ItemType.ENDER_PEARL, ItemType.ENCHANTED_ENDER_PEARL, ItemType.ENCHANTED_EYE_OF_ENDER, ItemType.ABSOLUTE_ENDER_PEARL, ItemType.TESSELLATED_ENDER_PEARL);
        expect(BazaarCategories.COMBAT, null, "Magma Cream",
                ItemType.MAGMA_CREAM, ItemType.ENCHANTED_MAGMA_CREAM, ItemType.WHIPPED_MAGMA_CREAM);
        expect(BazaarCategories.COMBAT, "Catacombs", "Special Fragments",
                ItemType.GIANT_FRAGMENT_BOULDER, ItemType.GIANT_FRAGMENT_LASER, ItemType.GIANT_FRAGMENT_BIGFOOT, ItemType.GIANT_FRAGMENT_DIAMOND);
        expect(BazaarCategories.COMBAT, "Catacombs", "Mob Drops",
                ItemType.MIMIC_FRAGMENT, ItemType.FEL_PEARL, ItemType.ANCIENT_ROSE, ItemType.HEALING_TISSUE);
        expect(BazaarCategories.COMBAT, "Catacombs", "Secret Drops",
                ItemType.DUNGEON_CHEST_KEY, ItemType.SUPERBOOM_TNT, ItemType.DUNGEON_TRAP, ItemType.SPIRIT_LEAP, ItemType.INFLATABLE_JERRY, ItemType.DUNGEON_DECOY, ItemType.ARCHITECTS_FIRST_DRAFT);
        expect(BazaarCategories.COMBAT, "Catacombs", "Catacombs Misc.",
                ItemType.KISMET_FEATHER, ItemType.WITHER_CATALYST, ItemType.SPIRIT_WING, ItemType.SPIRIT_BONE);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Clay",
                ItemType.CLAY_BALL, ItemType.ENCHANTED_CLAY_BALL, ItemType.ENCHANTED_CLAY_BLOCK);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Ink Sac",
                ItemType.INK_SAC, ItemType.ENCHANTED_INK_SAC);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Baits",
                ItemType.CARROT_BAIT, ItemType.MINNOW_BAIT, ItemType.FISH_BAIT, ItemType.LIGHT_BAIT, ItemType.DARK_BAIT, ItemType.SPOOKY_BAIT, ItemType.SPIKED_BAIT, ItemType.BLESSED_BAIT, ItemType.ICE_BAIT, ItemType.FROZEN_BAIT, ItemType.WHALE_BAIT, ItemType.SHARK_BAIT, ItemType.CORRUPTED_BAIT, ItemType.HOT_BAIT, ItemType.GLOWY_CHUM_BAIT, ItemType.WORM_BAIT, ItemType.GOLDEN_BAIT, ItemType.TREASURE_BAIT, ItemType.HOTSPOT_BAIT, ItemType.WOODEN_BAIT);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Mob Drops",
                ItemType.WORM_MEMBRANE, ItemType.ICE_HUNK, ItemType.BLUE_ICE_HUNK, ItemType.EMPERORS_SKULL, ItemType.BOBBIN_SCRIPTURES, ItemType.MAGMA_CORE, ItemType.HALF_EATEN_MUSHROOM, ItemType.SINGED_POWDER, ItemType.FRIED_FEATHER, ItemType.SCORCHED_CRAB_STICK, ItemType.BRIMSTONE_HANDLE, ItemType.BROKEN_RADAR, ItemType.BLUE_RING, ItemType.INK_SPLAT, ItemType.SEARED_ESCARGOT);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Lava Fishing",
                ItemType.LAVA_SHELL, ItemType.LUMP_OF_MAGMA, ItemType.MOOGMA_PELT, ItemType.CUP_OF_BLOOD, ItemType.PYROCLASTIC_SCALE, ItemType.FLAMING_HEART, ItemType.ORB_OF_ENERGY, ItemType.HORN_OF_TAURUS, ItemType.THUNDER_SHARDS, ItemType.MAGMA_LORD_FRAGMENT);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Water Orbs",
                ItemType.WATER_ORB, ItemType.LAVA_WATER_ORB, ItemType.SPOOKY_WATER_ORB, ItemType.SHARK_WATER_ORB, ItemType.WINTER_WATER_ORB, ItemType.BAYOU_WATER_ORB, ItemType.HOTSPOT_WATER_ORB, ItemType.LOTUS_WATER_ORB);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Backwater Bayou",
                ItemType.RUSTY_COIN, ItemType.BUSTED_BELT_BUCKLE, ItemType.OLD_LEATHER_BOOT, ItemType.BRONZE_BOWL, ItemType.TORN_CLOTH, ItemType.ALLIGATOR_SKIN, ItemType.TITANOBOA_SHED, ItemType.CAN_OF_WORMS);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Torrhus Canyon",
                ItemType.SUBLIME_SILK, ItemType.REINFORCED_NETTING, ItemType.RUBY_VEILSHROOM, ItemType.ENCHANTED_RUBY_VEILSHROOM, ItemType.VEILSHROOM_BUNCH, ItemType.MIRIA_PRIZE, ItemType.HONEYCOMB, ItemType.ENCHANTED_HONEYCOMB, ItemType.ENCHANTED_HONEYCOMB_BLOCK, ItemType.BOTTLED_MISCHIEF, ItemType.SEA_BRINE, ItemType.FUN_SIZED_POT_OF_HONEYCOMB, ItemType.FAMILY_SIZED_POT_OF_HONEYCOMB, ItemType.JUMBO_POT_OF_HONEYCOMB, ItemType.BEHEMOTH_POT_OF_HONEYCOMB);
        expect(BazaarCategories.WOODS_AND_FISHES, null, "Lotus Atoll",
                ItemType.LOTUS, ItemType.LOTUS_SILVER, ItemType.LOTUS_GOLD, ItemType.LOTUS_DIAMOND, ItemType.UNLUCKY_DOUBLOON, ItemType.REVERSE_REFORGE_STONE_CORE, ItemType.FROGCOIN);
        expect(BazaarCategories.ODDITIES, "Modifiers", "Item Upgrades",
                ItemType.RECOMBOBULATOR_3000, ItemType.WOOD_SINGULARITY, ItemType.SIL_EX, ItemType.TRANSMISSION_TUNER, ItemType.DIVAN_POWDER_COATING, ItemType.GOLDEN_BOUNTY, ItemType.MANA_DISINTEGRATOR, ItemType.PESTHUNTING_GUIDE, ItemType.GOLD_BOTTLE_CAP, ItemType.TROUBLED_BUBBLE, ItemType.SEVERED_PINCER, ItemType.OCTOPUS_TENDRIL, ItemType.CHAIN_OF_THE_END_TIMES, ItemType.SEVERED_HAND, ItemType.ENDSTONE_IDOL, ItemType.ENSNARED_SNAIL, ItemType.FATEFUL_STINGER, ItemType.TURBO_GOURD, ItemType.ENCHANTED_TURBO_GOURD, ItemType.OVERCLOCKER_3000, ItemType.PRICKLY_CREEPER, ItemType.VIBRANT_CORAL, ItemType.WATER_HYACINTH, ItemType.DISTANT_ECHO, ItemType.FORESTS_FAVOR);
        expect(BazaarCategories.ODDITIES, "Modifiers", "Ability Scrolls",
                ItemType.IMPLOSION_SCROLL, ItemType.WITHER_SHIELD_SCROLL, ItemType.SHADOW_WARP_SCROLL, ItemType.FROGS_SPIRIT_SCROLL);
        expect(BazaarCategories.ODDITIES, "Modifiers", "Master Stars",
                ItemType.FIRST_MASTER_STAR, ItemType.SECOND_MASTER_STAR, ItemType.THIRD_MASTER_STAR, ItemType.FOURTH_MASTER_STAR, ItemType.FIFTH_MASTER_STAR);
        expect(BazaarCategories.ODDITIES, "Modifiers", "Foraging Boosters",
                ItemType.FIGHTING_BOOSTER, ItemType.FIGHTING_BOOSTER_UNCOMMON, ItemType.FORAGING_FORTUNE_BOOSTER, ItemType.FORAGING_FORTUNE_BOOSTER_UNCOMMON, ItemType.FORAGING_WISDOM_BOOSTER, ItemType.FORAGING_WISDOM_BOOSTER_UNCOMMON, ItemType.SWEEP_BOOSTER, ItemType.SWEEP_BOOSTER_UNCOMMON, ItemType.HUNTING_WISDOM_BOOSTER_COMMON, ItemType.HUNTING_WISDOM_BOOSTER_UNCOMMON);
        expect(BazaarCategories.ODDITIES, null, "Inferno Minion",
                ItemType.CRUDE_GABAGOOL, ItemType.VERY_CRUDE_GABAGOOL, ItemType.FUEL_GABAGOOL, ItemType.HEAVY_GABAGOOL, ItemType.HYPERGOLIC_GABAGOOL, ItemType.INFERNO_VERTEX, ItemType.INFERNO_APEX, ItemType.REAPER_PEPPER, ItemType.CHILI_PEPPER, ItemType.STUFFED_CHILI_PEPPER, ItemType.INFERNO_FUEL_BLOCK, ItemType.HYPERGOLIC_IONIZED_CERAMICS, ItemType.SULPHURIC_COAL, ItemType.REHEATED_GUMMY_POLAR_BEAR, ItemType.CAPSAICIN_EYEDROPS_NO_CHARGES);
        expect(BazaarCategories.ODDITIES, null, "Fuels",
                ItemType.ENCHANTED_LAVA_BUCKET, ItemType.MAGMA_BUCKET, ItemType.PLASMA_BUCKET, ItemType.HAMSTER_WHEEL, ItemType.FOUL_FLESH, ItemType.POWER_CRYSTAL, ItemType.CATALYST, ItemType.HYPER_CATALYST, ItemType.SOLAR_PANEL, ItemType.OIL_BARREL, ItemType.SCORCHED_POWER_CRYSTAL, ItemType.VOLTA, ItemType.DAYSWITCH, ItemType.NIGHTSWITCH, ItemType.THORNY_VINES, ItemType.SUNFLOWER_OIL);
        expect(BazaarCategories.ODDITIES, null, "Consumables",
                ItemType.MAGIC_MUSHROOM_SOUP, ItemType.SUPER_MAGIC_MUSHROOM_SOUP, ItemType.METAPHYSICAL_SERUM, ItemType.DWARVEN_OS_ORE_OATS, ItemType.DWARVEN_OS_BLOCK_BRAN, ItemType.DWARVEN_OS_GEMSTONE_GRAHAMS, ItemType.DWARVEN_OS_METALLIC_MINIS, ItemType.SPOTLITE, ItemType.MOBY_DUCK_COLLECTOR_EDITION, ItemType.VIAL_OF_VENOM, ItemType.FESTERING_MAGGOT, ItemType.BRAIN_FOOD, ItemType.CELESTIAL_MASON_JAR, ItemType.VERY_MOLDY_BREAD, ItemType.FEAST_BURGER, ItemType.MOUND_OF_SEAGRASS, ItemType.ISOPOD_HUSK, ItemType.BEE_SALIVA);
        expect(BazaarCategories.ODDITIES, null, "Cross-Dimensional",
                ItemType.LUSH_BERBERIS, ItemType.ENCHANTED_LUSH_BERBERIS, ItemType.AGARIMOO_TONGUE, ItemType.EXPORTABLE_CARROTS, ItemType.HEMOVIBE, ItemType.HEMOGLASS, ItemType.HEMOBOMB, ItemType.HIGHLITE, ItemType.FAKE_SHURIKEN);
        expect(BazaarCategories.ODDITIES, null, "Upgrade Fragments",
                ItemType.BONZO_FRAGMENT, ItemType.DARK_QUEENS_SOUL_DROP, ItemType.GOLDEN_FRAGMENT, ItemType.LIVID_FRAGMENT, ItemType.SCARF_FRAGMENT, ItemType.THORN_FRAGMENT, ItemType.WINTER_FRAGMENT);
        expect(BazaarCategories.ODDITIES, null, "EXP Bottles",
                ItemType.EXPERIENCE_BOTTLE, ItemType.GRAND_EXP_BOTTLE, ItemType.TITANIC_EXP_BOTTLE, ItemType.COLOSSAL_EXP_BOTTLE);
    }

    @Test
    void realCountsMatch() {
        assertEquals(5, setsOf(BazaarCategories.FARMING, "Garden").size());
        assertEquals(6, setsOf(BazaarCategories.MINING, "Crimson Isle").size());
        assertEquals(5, setsOf(BazaarCategories.COMBAT, "Slayers").size());
        assertEquals(4, setsOf(BazaarCategories.COMBAT, "Catacombs").size());
        assertEquals(4, setsOf(BazaarCategories.COMBAT, "The End").size());
        assertEquals(5, setsOf(BazaarCategories.ODDITIES, "Modifiers").size());
        assertEquals(29, setsOf(BazaarCategories.ODDITIES, "Pet Upgrades").size());
        assertEquals(27, setsOf(BazaarCategories.ODDITIES, "Minion Upgrades").size());
        assertEquals(9, setsOf(BazaarCategories.WOODS_AND_FISHES, "Wood").size());
        assertEquals(1, tile(BazaarCategories.ODDITIES, "Booster Cookie").productCount());
        assertEquals(25, tile(BazaarCategories.ODDITIES, "Modifiers").sets().getFirst().items.size());
    }

    @Test
    void descriptionTilesAreTheShardsAndReforgeStones() {
        assertEquals("gui_bazaar.tile.shards", tile(BazaarCategories.ODDITIES, "Shards").descriptionKey());
        assertEquals("gui_bazaar.tile.reforge_stones", tile(BazaarCategories.ODDITIES, "Reforge Stones").descriptionKey());
        assertNull(tile(BazaarCategories.ODDITIES, "Fuels").descriptionKey());
    }

    @Test
    void categoryNamesReadLikeTheRealTabs() {
        assertEquals("Farming", BazaarCategories.FARMING.displayName());
        assertEquals("Mining", BazaarCategories.MINING.displayName());
        assertEquals("Combat", BazaarCategories.COMBAT.displayName());
        assertEquals("Woods & Fishes", BazaarCategories.WOODS_AND_FISHES.displayName());
        assertEquals("Oddities", BazaarCategories.ODDITIES.displayName());
    }

    @Test
    void miningUsesLightBluePanes() {
        assertEquals(Material.LIGHT_BLUE_STAINED_GLASS_PANE, BazaarCategories.MINING.getGlassItem());
        assertEquals(Material.YELLOW_STAINED_GLASS_PANE, BazaarCategories.FARMING.getGlassItem());
        assertEquals(Material.RED_STAINED_GLASS_PANE, BazaarCategories.COMBAT.getGlassItem());
        assertEquals(Material.ORANGE_STAINED_GLASS_PANE, BazaarCategories.WOODS_AND_FISHES.getGlassItem());
        assertEquals(Material.PINK_STAINED_GLASS_PANE, BazaarCategories.ODDITIES.getGlassItem());
    }

    private static BazaarTile tile(BazaarCategories category, String name) {
        return category.tiles().stream().filter(tile -> tile.name().equals(name)).findFirst().orElseThrow();
    }

    private static List<BazaarItemSet> setsOf(BazaarCategories category, String name) {
        return tile(category, name).sets();
    }

    private static void expect(BazaarCategories category, String nested, String name, ItemType... products) {
        BazaarItemSet found = category.getItems().stream()
                .filter(set -> name.equals(set.name()) && java.util.Objects.equals(nested, set.nestedGroup))
                .findFirst()
                .orElseThrow(() -> new AssertionError(category + "/" + nested + "/" + name + " is missing"));
        assertEquals(List.of(products), found.items, category + "/" + name);
    }

    @Test
    void lookupFindsEveryItem() {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                for (ItemType type : set.items) {
                    Map.Entry<BazaarCategories, BazaarItemSet> found = BazaarCategories.getFromItem(type);
                    assertNotNull(found, type.name());
                    assertSame(category, found.getKey());
                    assertSame(set, found.getValue());
                }
            }
        }
    }
}
