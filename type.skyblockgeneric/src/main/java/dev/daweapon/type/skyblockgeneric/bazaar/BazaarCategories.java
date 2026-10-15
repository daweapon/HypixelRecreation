package dev.daweapon.type.skyblockgeneric.bazaar;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// The top-level bazaar tabs (Farming, Mining, Combat, etc.), each grouping the item sets shown under it.
@Getter
public enum BazaarCategories {
    FARMING(Material.GOLDEN_HOE, Material.YELLOW_STAINED_GLASS_PANE, "<e>",
            new BazaarItemSet(ItemType.WHEAT, "Wheat & Seeds",
                    ItemType.WHEAT, ItemType.ENCHANTED_BREAD, ItemType.HAY_BALE, ItemType.ENCHANTED_WHEAT, ItemType.ENCHANTED_HAY_BALE, ItemType.SEEDS,
                    ItemType.ENCHANTED_SEEDS, ItemType.BOX_OF_SEEDS, ItemType.TIGHTLY_TIED_HAY_BALE),
            new BazaarItemSet(ItemType.CARROT, "Carrot",
                    ItemType.CARROT, ItemType.ENCHANTED_CARROT, ItemType.ENCHANTED_CARROT_ON_A_STICK, ItemType.ENCHANTED_GOLDEN_CARROT),
            new BazaarItemSet(ItemType.POTATO, "Potato",
                    ItemType.POTATO, ItemType.POISONOUS_POTATO, ItemType.ENCHANTED_POTATO, ItemType.ENCHANTED_POISONOUS_POTATO, ItemType.ENCHANTED_BAKED_POTATO),
            new BazaarItemSet(ItemType.PUMPKIN, "Pumpkin",
                    ItemType.PUMPKIN, ItemType.ENCHANTED_PUMPKIN, ItemType.POLISHED_PUMPKIN),
            new BazaarItemSet(ItemType.MELON_SLICE, "Melon",
                    ItemType.MELON_SLICE, ItemType.ENCHANTED_MELON, ItemType.ENCHANTED_GLISTERING_MELON, ItemType.MELON_BLOCK, ItemType.ENCHANTED_MELON_BLOCK),
            new BazaarItemSet(ItemType.MUSHROOM, "Mushrooms",
                    ItemType.RED_MUSHROOM, ItemType.ENCHANTED_RED_MUSHROOM, ItemType.BROWN_MUSHROOM, ItemType.RED_MUSHROOM_BLOCK, ItemType.ENCHANTED_BROWN_MUSHROOM, ItemType.BROWN_MUSHROOM_BLOCK,
                    ItemType.ENCHANTED_RED_MUSHROOM_BLOCK, ItemType.ENCHANTED_BROWN_MUSHROOM_BLOCK),
            new BazaarItemSet(ItemType.COCOA_BEANS, "Cocoa Beans",
                    ItemType.COCOA_BEANS, ItemType.ENCHANTED_COCOA_BEANS, ItemType.ENCHANTED_COOKIE),
            new BazaarItemSet(ItemType.CACTUS_GREEN, "Cactus",
                    ItemType.CACTUS, ItemType.ENCHANTED_CACTUS_GREEN, ItemType.ENCHANTED_CACTUS, ItemType.POTTED_CACTUS),
            new BazaarItemSet(ItemType.SUGAR_CANE, "Sugar Cane",
                    ItemType.SUGAR_CANE, ItemType.ENCHANTED_SUGAR, ItemType.ENCHANTED_PAPER, ItemType.ENCHANTED_SUGAR_CANE),
            new BazaarItemSet(ItemType.ENCHANTED_SUNFLOWER, "Sunflower",
                    ItemType.ENCHANTED_SUNFLOWER, ItemType.COMPACTED_SUNFLOWER, ItemType.HELIANTHUS, ItemType.CONDENSED_HELIANTHUS, ItemType.SUNFLOWER),
            new BazaarItemSet(ItemType.MOONFLOWER, "Moonflower",
                    ItemType.MOONFLOWER, ItemType.ENCHANTED_MOONFLOWER, ItemType.COMPACTED_MOONFLOWER),
            new BazaarItemSet(ItemType.WILD_ROSE, "Wild Rose",
                    ItemType.WILD_ROSE, ItemType.ENCHANTED_WILD_ROSE, ItemType.COMPACTED_WILD_ROSE),
            new BazaarItemSet(ItemType.LEATHER, "Leather & Beef",
                    ItemType.LEATHER, ItemType.ENCHANTED_LEATHER, ItemType.RAW_BEEF, ItemType.ENCHANTED_RAW_BEEF),
            new BazaarItemSet(ItemType.RAW_PORKCHOP, "Pork",
                    ItemType.RAW_PORKCHOP, ItemType.ENCHANTED_PORK, ItemType.ENCHANTED_GRILLED_PORK),
            new BazaarItemSet(ItemType.RAW_CHICKEN, "Chicken & Feather",
                    ItemType.RAW_CHICKEN, ItemType.ENCHANTED_RAW_CHICKEN, ItemType.EGG, ItemType.ENCHANTED_CAKE, ItemType.ENCHANTED_EGG, ItemType.SUPER_ENCHANTED_EGG,
                    ItemType.OMEGA_ENCHANTED_EGG, ItemType.FEATHER, ItemType.ENCHANTED_FEATHER),
            new BazaarItemSet(ItemType.MUTTON, "Mutton",
                    ItemType.MUTTON, ItemType.ENCHANTED_MUTTON, ItemType.ENCHANTED_COOKED_MUTTON, ItemType.WHITE_WOOL, ItemType.ENCHANTED_WOOL),
            new BazaarItemSet(ItemType.RAW_RABBIT, "Rabbit",
                    ItemType.RABBIT_FOOT, ItemType.ENCHANTED_RABBIT_FOOT, ItemType.RABBIT_HIDE, ItemType.ENCHANTED_RABBIT_HIDE, ItemType.RAW_RABBIT, ItemType.ENCHANTED_RAW_RABBIT,
                    ItemType.ENCHANTED_COOKED_RABBIT),
            new BazaarItemSet(ItemType.NETHER_WART, "Nether Warts",
                    ItemType.NETHER_WART, ItemType.ENCHANTED_NETHER_WART, ItemType.MUTANT_NETHER_WART, ItemType.WARTY),
            new BazaarItemSet(ItemType.CROPIE, "Rare Crops", "Garden",
                    ItemType.CROPIE, ItemType.SQUASH, ItemType.FERMENTO, ItemType.CONDENSED_FERMENTO),
            new BazaarItemSet(ItemType.COMPOST, "Sprayonator", "Garden",
                    ItemType.COMPOST, ItemType.DUNG, ItemType.HONEY_JAR, ItemType.PLANT_MATTER, ItemType.TASTY_CHEESE, ItemType.JELLY),
            new BazaarItemSet(ItemType.COMPOST_BUNDLE, "Other", "Garden",
                    ItemType.COMPOST_BUNDLE, ItemType.WRIGGLING_LARVA, ItemType.FINE_FLOUR, ItemType.VISITORS_GRATITUDE, ItemType.PLOT_ERASER, ItemType.BACHELOR_ROSE,
                    ItemType.ETHEREAL_VINE, ItemType.LOCUST_LARVA, ItemType.DEAD_PLANT, ItemType.FRAGRANCED_BROWN_MUSHROOM_PASTE, ItemType.FIRE_IN_A_BOTTLE, ItemType.MOONDEW,
                    ItemType.PRICKLY_KISS),
            new BazaarItemSet(ItemType.CROPSHOT_GARDEN_CHIP, "Garden Chips", "Garden",
                    ItemType.CROPSHOT_GARDEN_CHIP, ItemType.EVERGREEN_GARDEN_CHIP, ItemType.HYPERCHARGE_GARDEN_CHIP, ItemType.MECHAMIND_GARDEN_CHIP, ItemType.OVERDRIVE_GARDEN_CHIP, ItemType.QUICKDRAW_GARDEN_CHIP,
                    ItemType.RAREFINDER_GARDEN_CHIP, ItemType.SOWLEDGE_GARDEN_CHIP, ItemType.SYNTHESIS_GARDEN_CHIP, ItemType.VERMIN_VAPORIZER_GARDEN_CHIP),
            new BazaarItemSet(ItemType.ALL_IN_ALOE, "Mutations", "Garden",
                    ItemType.ALL_IN_ALOE, ItemType.ASHWREATH, ItemType.BLASTBERRY, ItemType.CHEESEBITE, ItemType.CHLORONITE, ItemType.CHOCOBERRY,
                    ItemType.CHOCONUT, ItemType.CHORUS_FRUIT, ItemType.CINDERSHADE, ItemType.CREAMBLOOM, ItemType.DEVOURER, ItemType.DO_NOT_EAT_SHROOM,
                    ItemType.DUSKBLOOM, ItemType.DUSTGRAIN, ItemType.FLESHTRAP, ItemType.GLASSCORN, ItemType.GLOOMGOURD, ItemType.GODSEED,
                    ItemType.LONELILY, ItemType.MAGIC_JELLYBEAN, ItemType.NOCTILUME, ItemType.PHANTOMLEAF, ItemType.PLANTBOY_ADVANCE, ItemType.PUFFERCLOUD,
                    ItemType.SCOURROOT, ItemType.SHADEVINE, ItemType.SHELLFRUIT, ItemType.SNOOZLING, ItemType.SOGGYBUD, ItemType.STARTLEVINE,
                    ItemType.STOPLIGHT_PETAL, ItemType.THORNSHADE, ItemType.THUNDERLING, ItemType.TIMESTALK, ItemType.TURTLELLINI, ItemType.VEILSHROOM,
                    ItemType.WITHERBLOOM, ItemType.ZOMBUD)
    ),
    MINING(Material.DIAMOND_PICKAXE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<b>",
            new BazaarItemSet(ItemType.COBBLESTONE, "Cobblestone",
                    ItemType.COBBLESTONE, ItemType.ENCHANTED_COBBLESTONE),
            new BazaarItemSet(ItemType.COAL, "Coal",
                    ItemType.COAL, ItemType.ENCHANTED_COAL, ItemType.ENCHANTED_CHARCOAL, ItemType.ENCHANTED_COAL_BLOCK),
            new BazaarItemSet(ItemType.IRON_INGOT, "Iron",
                    ItemType.IRON_INGOT, ItemType.ENCHANTED_IRON_INGOT, ItemType.ENCHANTED_IRON_BLOCK),
            new BazaarItemSet(ItemType.GOLD_INGOT, "Gold",
                    ItemType.GOLD_INGOT, ItemType.ENCHANTED_GOLD_INGOT, ItemType.ENCHANTED_GOLD_BLOCK),
            new BazaarItemSet(ItemType.DIAMOND, "Diamond",
                    ItemType.DIAMOND, ItemType.ENCHANTED_DIAMOND, ItemType.ENCHANTED_DIAMOND_BLOCK, ItemType.REFINED_DIAMOND),
            new BazaarItemSet(ItemType.LAPIS_LAZULI, "Lapis",
                    ItemType.LAPIS_LAZULI, ItemType.ENCHANTED_LAPIS_LAZULI, ItemType.ENCHANTED_LAPIS_LAZULI_BLOCK),
            new BazaarItemSet(ItemType.EMERALD, "Emerald",
                    ItemType.EMERALD, ItemType.ENCHANTED_EMERALD, ItemType.ENCHANTED_EMERALD_BLOCK),
            new BazaarItemSet(ItemType.REDSTONE, "Redstone",
                    ItemType.REDSTONE, ItemType.ENCHANTED_REDSTONE, ItemType.ENCHANTED_REDSTONE_BLOCK),
            new BazaarItemSet(ItemType.OBSIDIAN, "Obsidian",
                    ItemType.OBSIDIAN, ItemType.ENCHANTED_OBSIDIAN),
            new BazaarItemSet(ItemType.END_STONE, "End Stone",
                    ItemType.END_STONE, ItemType.ENCHANTED_END_STONE),
            new BazaarItemSet(ItemType.FLINT, "Flint & Gravel",
                    ItemType.FLINT, ItemType.ENCHANTED_FLINT, ItemType.GRAVEL),
            new BazaarItemSet(ItemType.SAND, "Sand",
                    ItemType.SAND, ItemType.ENCHANTED_SAND),
            new BazaarItemSet(ItemType.ICE, "Ice",
                    ItemType.ICE, ItemType.PACKED_ICE, ItemType.ENCHANTED_ICE, ItemType.ENCHANTED_PACKED_ICE, ItemType.GLACIAL_FRAGMENT),
            new BazaarItemSet(ItemType.SNOW_BLOCK, "Snow",
                    ItemType.SNOWBALL, ItemType.SNOW_BLOCK, ItemType.ENCHANTED_SNOW_BLOCK),
            new BazaarItemSet(ItemType.HARD_STONE, "Hard Stone",
                    ItemType.HARD_STONE, ItemType.ENCHANTED_HARD_STONE, ItemType.CONCENTRATED_STONE),
            new BazaarItemSet(ItemType.ROUGH_JADE_GEM, "Jade Gemstones", "Gemstones",
                    ItemType.ROUGH_JADE_GEM, ItemType.FLAWED_JADE_GEM, ItemType.FINE_JADE_GEM, ItemType.FLAWLESS_JADE_GEM, ItemType.PERFECT_JADE_GEM),
            new BazaarItemSet(ItemType.ROUGH_AMBER_GEM, "Amber Gemstones", "Gemstones",
                    ItemType.ROUGH_AMBER_GEM, ItemType.FLAWED_AMBER_GEM, ItemType.FINE_AMBER_GEM, ItemType.FLAWLESS_AMBER_GEM, ItemType.PERFECT_AMBER_GEM),
            new BazaarItemSet(ItemType.ROUGH_TOPAZ_GEM, "Topaz Gemstones", "Gemstones",
                    ItemType.ROUGH_TOPAZ_GEM, ItemType.FLAWED_TOPAZ_GEM, ItemType.FINE_TOPAZ_GEM, ItemType.FLAWLESS_TOPAZ_GEM, ItemType.PERFECT_TOPAZ_GEM),
            new BazaarItemSet(ItemType.ROUGH_SAPPHIRE_GEM, "Sapphire Gemstones", "Gemstones",
                    ItemType.ROUGH_SAPPHIRE_GEM, ItemType.FLAWED_SAPPHIRE_GEM, ItemType.FINE_SAPPHIRE_GEM, ItemType.FLAWLESS_SAPPHIRE_GEM, ItemType.PERFECT_SAPPHIRE_GEM),
            new BazaarItemSet(ItemType.ROUGH_AMETHYST_GEM, "Amethyst Gemstones", "Gemstones",
                    ItemType.ROUGH_AMETHYST_GEM, ItemType.FLAWED_AMETHYST_GEM, ItemType.FINE_AMETHYST_GEM, ItemType.FLAWLESS_AMETHYST_GEM, ItemType.PERFECT_AMETHYST_GEM),
            new BazaarItemSet(ItemType.ROUGH_JASPER_GEM, "Jasper Gemstones", "Gemstones",
                    ItemType.ROUGH_JASPER_GEM, ItemType.FLAWED_JASPER_GEM, ItemType.FINE_JASPER_GEM, ItemType.FLAWLESS_JASPER_GEM, ItemType.PERFECT_JASPER_GEM),
            new BazaarItemSet(ItemType.ROUGH_RUBY_GEM, "Ruby Gemstones", "Gemstones",
                    ItemType.ROUGH_RUBY_GEM, ItemType.FLAWED_RUBY_GEM, ItemType.FINE_RUBY_GEM, ItemType.FLAWLESS_RUBY_GEM, ItemType.PERFECT_RUBY_GEM),
            new BazaarItemSet(ItemType.ROUGH_OPAL_GEM, "Opal Gemstones", "Gemstones",
                    ItemType.ROUGH_OPAL_GEM, ItemType.FLAWED_OPAL_GEM, ItemType.FINE_OPAL_GEM, ItemType.FLAWLESS_OPAL_GEM, ItemType.PERFECT_OPAL_GEM),
            new BazaarItemSet(ItemType.ROUGH_ONYX_GEM, "Onyx Gemstones", "Gemstones",
                    ItemType.ROUGH_ONYX_GEM, ItemType.FLAWED_ONYX_GEM, ItemType.FINE_ONYX_GEM, ItemType.FLAWLESS_ONYX_GEM, ItemType.PERFECT_ONYX_GEM),
            new BazaarItemSet(ItemType.ROUGH_AQUAMARINE_GEM, "Aquamarine Gemstones", "Gemstones",
                    ItemType.ROUGH_AQUAMARINE_GEM, ItemType.FLAWED_AQUAMARINE_GEM, ItemType.FINE_AQUAMARINE_GEM, ItemType.FLAWLESS_AQUAMARINE_GEM, ItemType.PERFECT_AQUAMARINE_GEM),
            new BazaarItemSet(ItemType.ROUGH_CITRINE_GEM, "Citrine Gemstones", "Gemstones",
                    ItemType.ROUGH_CITRINE_GEM, ItemType.FLAWED_CITRINE_GEM, ItemType.FINE_CITRINE_GEM, ItemType.FLAWLESS_CITRINE_GEM, ItemType.PERFECT_CITRINE_GEM),
            new BazaarItemSet(ItemType.ROUGH_PERIDOT_GEM, "Peridot Gemstones", "Gemstones",
                    ItemType.ROUGH_PERIDOT_GEM, ItemType.FLAWED_PERIDOT_GEM, ItemType.FINE_PERIDOT_GEM, ItemType.FLAWLESS_PERIDOT_GEM, ItemType.PERFECT_PERIDOT_GEM),
            new BazaarItemSet(ItemType.GOBLIN_EGG, "Goblin Eggs",
                    ItemType.GOBLIN_EGG, ItemType.RED_GOBLIN_EGG, ItemType.BLUE_GOBLIN_EGG, ItemType.YELLOW_GOBLIN_EGG, ItemType.GREEN_GOBLIN_EGG),
            new BazaarItemSet(ItemType.MITHRIL, "Dwarven Mines",
                    ItemType.MITHRIL, ItemType.ENCHANTED_MITHRIL, ItemType.REFINED_MITHRIL, ItemType.TITANIUM, ItemType.ENCHANTED_TITANIUM, ItemType.REFINED_TITANIUM,
                    ItemType.STARFALL, ItemType.TREASURITE, ItemType.SORROW),
            new BazaarItemSet(ItemType.DRILL_ENGINE, "Forge",
                    ItemType.PLASMA, ItemType.GLACITE_JEWEL, ItemType.FUEL_TANK, ItemType.BEJEWELED_HANDLE, ItemType.GEMSTONE_MIXTURE, ItemType.MITHRIL_PLATE,
                    ItemType.DRILL_ENGINE, ItemType.GOLDEN_PLATE),
            new BazaarItemSet(ItemType.PRECURSOR_APPARATUS, "Crystal Hollows",
                    ItemType.PRECURSOR_APPARATUS, ItemType.ELECTRON_TRANSMITTER, ItemType.FTX_3070, ItemType.ROBOTRON_REFLECTOR, ItemType.SUPERLITE_MOTOR, ItemType.CONTROL_SWITCH,
                    ItemType.SYNTHETIC_HEART, ItemType.JUNGLE_KEY, ItemType.JUNGLE_HEART, ItemType.DIVAN_FRAGMENT, ItemType.CORLEONITE, ItemType.SLUDGE_JUICE,
                    ItemType.YOGGIE, ItemType.ASCENSION_ROPE, ItemType.WISHING_COMPASS, ItemType.BOB_OMB),
            new BazaarItemSet(ItemType.GLOWSTONE_DUST, "Glowstone", "Crimson Isle",
                    ItemType.GLOWSTONE_DUST, ItemType.ENCHANTED_GLOWSTONE_DUST, ItemType.ENCHANTED_GLOWSTONE, ItemType.ENCHANTED_REDSTONE_LAMP),
            new BazaarItemSet(ItemType.NETHERRACK, "Netherrack", "Crimson Isle",
                    ItemType.NETHERRACK, ItemType.ENCHANTED_NETHERRACK),
            new BazaarItemSet(ItemType.QUARTZ, "Quartz", "Crimson Isle",
                    ItemType.QUARTZ, ItemType.ENCHANTED_QUARTZ, ItemType.ENCHANTED_QUARTZ_BLOCK),
            new BazaarItemSet(ItemType.RED_SAND, "Red Sand", "Crimson Isle",
                    ItemType.RED_SAND, ItemType.ENCHANTED_RED_SAND, ItemType.ENCHANTED_RED_SAND_CUBE),
            new BazaarItemSet(ItemType.SULPHUR, "Sulphur", "Crimson Isle",
                    ItemType.SULPHUR, ItemType.ENCHANTED_SULPHUR, ItemType.ENCHANTED_SULPHUR_CUBE),
            new BazaarItemSet(ItemType.MYCELIUM, "Mycelium", "Crimson Isle",
                    ItemType.MYCELIUM, ItemType.ENCHANTED_MYCELIUM, ItemType.ENCHANTED_MYCELIUM_CUBE),
            new BazaarItemSet(ItemType.UMBER, "Glacite Tunnels",
                    ItemType.UMBER, ItemType.ENCHANTED_UMBER, ItemType.REFINED_UMBER, ItemType.UMBER_PLATE, ItemType.UMBER_KEY, ItemType.TUNGSTEN,
                    ItemType.ENCHANTED_TUNGSTEN, ItemType.REFINED_TUNGSTEN, ItemType.TUNGSTEN_PLATE, ItemType.TUNGSTEN_KEY, ItemType.GLACITE, ItemType.ENCHANTED_GLACITE,
                    ItemType.GLACITE_AMALGAMATION, ItemType.PERFECT_PLATE, ItemType.SKELETON_KEY, ItemType.SUSPICIOUS_SCRAP),
            new BazaarItemSet(ItemType.CLAW_FOSSIL, "Fossils",
                    ItemType.FOOTPRINT_FOSSIL, ItemType.WEBBED_FOSSIL, ItemType.UGLY_FOSSIL, ItemType.TUSK_FOSSIL, ItemType.SPINE_FOSSIL, ItemType.CLUBBED_FOSSIL,
                    ItemType.CLAW_FOSSIL, ItemType.HELIX)
    ),
    COMBAT(Material.IRON_SWORD, Material.RED_STAINED_GLASS_PANE, "<c>",
            new BazaarItemSet(ItemType.ROTTEN_FLESH, "Rotten Flesh",
                    ItemType.ROTTEN_FLESH, ItemType.ENCHANTED_ROTTEN_FLESH),
            new BazaarItemSet(ItemType.BONE, "Bone",
                    ItemType.BONE, ItemType.ENCHANTED_BONE, ItemType.ENCHANTED_BONE_BLOCK, ItemType.ENCHANTED_BONE_MEAL),
            new BazaarItemSet(ItemType.STRING, "Arachnids",
                    ItemType.STRING, ItemType.ENCHANTED_STRING, ItemType.SPIDER_EYE, ItemType.ENCHANTED_SPIDER_EYE, ItemType.ENCHANTED_FERMENTED_SPIDER_EYE, ItemType.SOUL_STRING,
                    ItemType.ARACHNE_KEEPER_FRAGMENT, ItemType.ARACHNE_FRAGMENT, ItemType.ARACHNE_FANG),
            new BazaarItemSet(ItemType.GUNPOWDER, "Gunpowder",
                    ItemType.GUNPOWDER, ItemType.ENCHANTED_GUNPOWDER, ItemType.ENCHANTED_FIREWORK_ROCKET),
            new BazaarItemSet(ItemType.ENDER_PEARL, "Ender Pearl",
                    ItemType.ENDER_PEARL, ItemType.ENCHANTED_ENDER_PEARL, ItemType.ENCHANTED_EYE_OF_ENDER, ItemType.ABSOLUTE_ENDER_PEARL, ItemType.TESSELLATED_ENDER_PEARL),
            new BazaarItemSet(ItemType.GHAST_TEAR, "Ghast Tear",
                    ItemType.GHAST_TEAR, ItemType.ENCHANTED_GHAST_TEAR, ItemType.SILVER_FANG),
            new BazaarItemSet(ItemType.SLIME_BALL, "Slimeball",
                    ItemType.SLIME_BALL, ItemType.ENCHANTED_SLIME_BALL, ItemType.ENCHANTED_SLIME_BLOCK, ItemType.POISON_SAMPLE),
            new BazaarItemSet(ItemType.MAGMA_CREAM, "Magma Cream",
                    ItemType.MAGMA_CREAM, ItemType.ENCHANTED_MAGMA_CREAM, ItemType.WHIPPED_MAGMA_CREAM),
            new BazaarItemSet(ItemType.BLAZE_ROD, "Blaze Rod",
                    ItemType.BLAZE_ROD, ItemType.ENCHANTED_BLAZE_POWDER, ItemType.ENCHANTED_BLAZE_ROD),
            new BazaarItemSet(ItemType.SUMMONING_EYE, "Summoning Eye", "The End",
                    ItemType.SUMMONING_EYE),
            new BazaarItemSet(ItemType.CRYSTAL_FRAGMENT, "Crystal Fragment", "The End",
                    ItemType.CRYSTAL_FRAGMENT),
            new BazaarItemSet(ItemType.PROTECTOR_FRAGMENT, "Dragon Fragments", "The End",
                    ItemType.PROTECTOR_FRAGMENT, ItemType.OLD_FRAGMENT, ItemType.UNSTABLE_FRAGMENT, ItemType.STRONG_FRAGMENT, ItemType.YOUNG_FRAGMENT, ItemType.WISE_FRAGMENT,
                    ItemType.SUPERIOR_FRAGMENT, ItemType.HOLY_FRAGMENT),
            new BazaarItemSet(ItemType.RITUAL_RESIDUE, "Draconic Sacrifice", "The End",
                    ItemType.RITUAL_RESIDUE, ItemType.MITE_GEL),
            new BazaarItemSet(ItemType.BLAZE_ASHES, "Mob Drops", "Crimson Isle",
                    ItemType.BLAZE_ASHES, ItemType.MUTATED_BLAZE_ASHES, ItemType.MILLENIA_OLD_BLAZE_ASHES, ItemType.SPECTRE_DUST, ItemType.WITHER_SOUL, ItemType.BEZOS,
                    ItemType.MAGMAG, ItemType.DIGESTED_MUSHROOMS, ItemType.BURNING_EYE, ItemType.MAGMA_CHUNK, ItemType.GAZING_PEARL, ItemType.FLAMES,
                    ItemType.KADA_LEAD, ItemType.MATCH_STICKS, ItemType.TENTACLE_MEAT, ItemType.CORRUPTED_FRAGMENT),
            new BazaarItemSet(ItemType.HALLOWED_SKULL, "Mini Bosses", "Crimson Isle",
                    ItemType.HALLOWED_SKULL, ItemType.SPELL_POWDER, ItemType.LEATHER_CLOTH, ItemType.LUMINO_FIBER, ItemType.COMPACT_OOZE, ItemType.REKINDLED_EMBER_FRAGMENT),
            new BazaarItemSet(ItemType.CORRUPTED_NETHER_STAR, "Special", "Crimson Isle",
                    ItemType.CORRUPTED_NETHER_STAR, ItemType.HEAVY_PEARL, ItemType.KUUDRA_TEETH, ItemType.X, ItemType.Y, ItemType.Z,
                    ItemType.KUUDRA_TENTACLE),
            new BazaarItemSet(ItemType.REVENANT_FLESH, "Revenant Horror", "Slayers",
                    ItemType.REVENANT_FLESH, ItemType.REVENANT_VISCERA, ItemType.GOLDEN_POWDER, ItemType.UNDEAD_CATALYST, ItemType.REVENANT_CATALYST),
            new BazaarItemSet(ItemType.TARANTULA_WEB, "Tarantula Broodfather", "Slayers",
                    ItemType.TARANTULA_WEB, ItemType.TARANTULA_SILK, ItemType.SPIDER_CATALYST, ItemType.TARANTULA_CATALYST, ItemType.FLY_SWATTER, ItemType.TOXIC_ARROW_POISON),
            new BazaarItemSet(ItemType.WOLF_TOOTH, "Sven Packmaster", "Slayers",
                    ItemType.WOLF_TOOTH, ItemType.GOLDEN_TOOTH, ItemType.WEAK_WOLF_CATALYST),
            new BazaarItemSet(ItemType.NULL_SPHERE, "Voidgloom Seraph", "Slayers",
                    ItemType.NULL_SPHERE, ItemType.NULL_OVOID, ItemType.NULL_ATOM, ItemType.RAW_SOULFLOW, ItemType.SOULFLOW, ItemType.BRAIDED_GRIFFIN_FEATHER,
                    ItemType.TWILIGHT_ARROW_POISON, ItemType.NULL_BLADE, ItemType.NULL_EDGE),
            new BazaarItemSet(ItemType.DERELICT_ASHE, "Inferno Demonlord", "Slayers",
                    ItemType.DERELICT_ASHE, ItemType.AMALGAMATED_CRIMSONITE_NEW, ItemType.AMALGAMATED_CRIMSONITE, ItemType.MOLTEN_POWDER, ItemType.BLAZE_ROD_DISTILLATE, ItemType.GABAGOOL_DISTILLATE,
                    ItemType.GLOWSTONE_DISTILLATE, ItemType.MAGMA_CREAM_DISTILLATE, ItemType.NETHER_WART_DISTILLATE, ItemType.BUNDLE_OF_MAGMA_ARROWS, ItemType.KELVIN_INVERTER, ItemType.SUBZERO_INVERTER),
            new BazaarItemSet(ItemType.GIANT_FRAGMENT_BOULDER, "Special Fragments", "Catacombs",
                    ItemType.GIANT_FRAGMENT_BOULDER, ItemType.GIANT_FRAGMENT_LASER, ItemType.GIANT_FRAGMENT_BIGFOOT, ItemType.GIANT_FRAGMENT_DIAMOND),
            new BazaarItemSet(ItemType.MIMIC_FRAGMENT, "Mob Drops", "Catacombs",
                    ItemType.MIMIC_FRAGMENT, ItemType.FEL_PEARL, ItemType.ANCIENT_ROSE, ItemType.HEALING_TISSUE),
            new BazaarItemSet(ItemType.DUNGEON_CHEST_KEY, "Secret Drops", "Catacombs",
                    ItemType.DUNGEON_CHEST_KEY, ItemType.SUPERBOOM_TNT, ItemType.DUNGEON_TRAP, ItemType.SPIRIT_LEAP, ItemType.INFLATABLE_JERRY, ItemType.DUNGEON_DECOY,
                    ItemType.ARCHITECTS_FIRST_DRAFT),
            new BazaarItemSet(ItemType.KISMET_FEATHER, "Catacombs Misc.", "Catacombs",
                    ItemType.KISMET_FEATHER, ItemType.WITHER_CATALYST, ItemType.SPIRIT_WING, ItemType.SPIRIT_BONE)
    ),
    WOODS_AND_FISHES(Material.FISHING_ROD, Material.ORANGE_STAINED_GLASS_PANE, "<6>",
            new BazaarItemSet(ItemType.OAK_LOG, "Oak", "Wood",
                    ItemType.OAK_LOG, ItemType.ENCHANTED_OAK_LOG),
            new BazaarItemSet(ItemType.SPRUCE_LOG, "Spruce", "Wood",
                    ItemType.SPRUCE_LOG, ItemType.ENCHANTED_SPRUCE_LOG),
            new BazaarItemSet(ItemType.BIRCH_LOG, "Birch", "Wood",
                    ItemType.BIRCH_LOG, ItemType.ENCHANTED_BIRCH_LOG),
            new BazaarItemSet(ItemType.DARK_OAK_LOG, "Dark Oak", "Wood",
                    ItemType.DARK_OAK_LOG, ItemType.ENCHANTED_DARK_OAK_LOG),
            new BazaarItemSet(ItemType.ACACIA_LOG, "Acacia", "Wood",
                    ItemType.ACACIA_LOG, ItemType.ENCHANTED_ACACIA_LOG),
            new BazaarItemSet(ItemType.JUNGLE_LOG, "Jungle", "Wood",
                    ItemType.JUNGLE_LOG, ItemType.ENCHANTED_JUNGLE_LOG),
            new BazaarItemSet(ItemType.FIG_LOG, "Fig", "Wood",
                    ItemType.FIG_LOG, ItemType.ENCHANTED_FIG_LOG, ItemType.FIGSTONE),
            new BazaarItemSet(ItemType.MANGROVE_LOG, "Mangrove", "Wood",
                    ItemType.MANGROVE_LOG, ItemType.ENCHANTED_MANGROVE_LOG, ItemType.MANGCORE),
            new BazaarItemSet(ItemType.HELIX_LOG, "Helix", "Wood",
                    ItemType.HELIX_LOG, ItemType.ENCHANTED_HELIX_LOG, ItemType.HELIXIS),
            new BazaarItemSet(ItemType.RAW_FISH, "Raw Cod", "Fish",
                    ItemType.RAW_FISH, ItemType.ENCHANTED_RAW_FISH, ItemType.ENCHANTED_COOKED_FISH),
            new BazaarItemSet(ItemType.RAW_SALMON, "Salmon", "Fish",
                    ItemType.RAW_SALMON, ItemType.ENCHANTED_RAW_SALMON, ItemType.ENCHANTED_COOKED_SALMON),
            new BazaarItemSet(ItemType.TROPICAL_FISH, "Tropical Fish", "Fish",
                    ItemType.TROPICAL_FISH, ItemType.ENCHANTED_TROPICAL_FISH),
            new BazaarItemSet(ItemType.PUFFERFISH, "Pufferfish", "Fish",
                    ItemType.PUFFERFISH, ItemType.ENCHANTED_PUFFERFISH),
            new BazaarItemSet(ItemType.POPPY, "Flowers",
                    ItemType.POPPY, ItemType.ENCHANTED_POPPY, ItemType.DANDELION, ItemType.ENCHANTED_DANDELION),
            new BazaarItemSet(ItemType.PRISMARINE_SHARD, "Prismarine",
                    ItemType.PRISMARINE_SHARD, ItemType.ENCHANTED_PRISMARINE_SHARD, ItemType.PRISMARINE_CRYSTALS, ItemType.ENCHANTED_PRISMARINE_CRYSTALS),
            new BazaarItemSet(ItemType.CLAY_BALL, "Clay",
                    ItemType.CLAY_BALL, ItemType.ENCHANTED_CLAY_BALL, ItemType.ENCHANTED_CLAY_BLOCK),
            new BazaarItemSet(ItemType.SPONGE, "Sponge",
                    ItemType.SPONGE, ItemType.ENCHANTED_SPONGE, ItemType.ENCHANTED_WET_SPONGE),
            new BazaarItemSet(ItemType.LILY_PAD, "Lily Pad",
                    ItemType.LILY_PAD, ItemType.ENCHANTED_LILY_PAD, ItemType.CONDENSED_WATER_LILY),
            new BazaarItemSet(ItemType.INK_SAC, "Ink Sac",
                    ItemType.INK_SAC, ItemType.ENCHANTED_INK_SAC),
            new BazaarItemSet(ItemType.CARROT_BAIT, "Baits",
                    ItemType.CARROT_BAIT, ItemType.MINNOW_BAIT, ItemType.FISH_BAIT, ItemType.LIGHT_BAIT, ItemType.DARK_BAIT, ItemType.SPOOKY_BAIT,
                    ItemType.SPIKED_BAIT, ItemType.BLESSED_BAIT, ItemType.ICE_BAIT, ItemType.FROZEN_BAIT, ItemType.WHALE_BAIT, ItemType.SHARK_BAIT,
                    ItemType.CORRUPTED_BAIT, ItemType.HOT_BAIT, ItemType.GLOWY_CHUM_BAIT, ItemType.WORM_BAIT, ItemType.GOLDEN_BAIT, ItemType.TREASURE_BAIT,
                    ItemType.HOTSPOT_BAIT, ItemType.WOODEN_BAIT),
            new BazaarItemSet(ItemType.WORM_MEMBRANE, "Mob Drops",
                    ItemType.WORM_MEMBRANE, ItemType.ICE_HUNK, ItemType.BLUE_ICE_HUNK, ItemType.EMPERORS_SKULL, ItemType.BOBBIN_SCRIPTURES, ItemType.MAGMA_CORE,
                    ItemType.HALF_EATEN_MUSHROOM, ItemType.SINGED_POWDER, ItemType.FRIED_FEATHER, ItemType.SCORCHED_CRAB_STICK, ItemType.BRIMSTONE_HANDLE, ItemType.BROKEN_RADAR,
                    ItemType.BLUE_RING, ItemType.INK_SPLAT, ItemType.SEARED_ESCARGOT),
            new BazaarItemSet(ItemType.MAGMAFISH, "Magmafish",
                    ItemType.MAGMAFISH, ItemType.SILVER_MAGMAFISH, ItemType.GOLD_MAGMAFISH, ItemType.DIAMOND_MAGMAFISH),
            new BazaarItemSet(ItemType.LAVA_SHELL, "Lava Fishing",
                    ItemType.LAVA_SHELL, ItemType.LUMP_OF_MAGMA, ItemType.MOOGMA_PELT, ItemType.CUP_OF_BLOOD, ItemType.PYROCLASTIC_SCALE, ItemType.FLAMING_HEART,
                    ItemType.ORB_OF_ENERGY, ItemType.HORN_OF_TAURUS, ItemType.THUNDER_SHARDS, ItemType.MAGMA_LORD_FRAGMENT),
            new BazaarItemSet(ItemType.WATER_ORB, "Water Orbs",
                    ItemType.WATER_ORB, ItemType.LAVA_WATER_ORB, ItemType.SPOOKY_WATER_ORB, ItemType.SHARK_WATER_ORB, ItemType.WINTER_WATER_ORB, ItemType.BAYOU_WATER_ORB,
                    ItemType.HOTSPOT_WATER_ORB, ItemType.LOTUS_WATER_ORB),
            new BazaarItemSet(ItemType.CHUM, "Chum Fishing",
                    ItemType.CHUM, ItemType.GLOWING_MUSHROOM, ItemType.EMPTY_CHUM_BUCKET, ItemType.FULL_CHUM_BUCKET),
            new BazaarItemSet(ItemType.RUSTY_COIN, "Backwater Bayou",
                    ItemType.RUSTY_COIN, ItemType.BUSTED_BELT_BUCKLE, ItemType.OLD_LEATHER_BOOT, ItemType.BRONZE_BOWL, ItemType.TORN_CLOTH, ItemType.ALLIGATOR_SKIN,
                    ItemType.TITANOBOA_SHED, ItemType.CAN_OF_WORMS),
            new BazaarItemSet(ItemType.VINESAP, "Moonglade Marsh",
                    ItemType.VINESAP, ItemType.ENCHANTED_VINESAP, ItemType.TENDER_WOOD, ItemType.ENCHANTED_TENDER_WOOD, ItemType.SEA_LUMIES, ItemType.ENCHANTED_SEA_LUMIES,
                    ItemType.FLEXBONE, ItemType.LUSHLILAC, ItemType.LUSHLILAC_BONBON, ItemType.EXALTED_LUSHLILAC_BONBON, ItemType.PRIME_LUSHLILAC_BONBON, ItemType.CANDYCOMB,
                    ItemType.OCEANDY, ItemType.WET_WATER, ItemType.GILL_MEMBRANE, ItemType.STURDY_BONE, ItemType.STARLYN_PRIZE, ItemType.DEEP_ROOT),
            new BazaarItemSet(ItemType.HONEYCOMB, "Torrhus Canyon",
                    ItemType.SUBLIME_SILK, ItemType.REINFORCED_NETTING, ItemType.RUBY_VEILSHROOM, ItemType.ENCHANTED_RUBY_VEILSHROOM, ItemType.VEILSHROOM_BUNCH, ItemType.MIRIA_PRIZE,
                    ItemType.HONEYCOMB, ItemType.ENCHANTED_HONEYCOMB, ItemType.ENCHANTED_HONEYCOMB_BLOCK, ItemType.BOTTLED_MISCHIEF, ItemType.SEA_BRINE, ItemType.FUN_SIZED_POT_OF_HONEYCOMB,
                    ItemType.FAMILY_SIZED_POT_OF_HONEYCOMB, ItemType.JUMBO_POT_OF_HONEYCOMB, ItemType.BEHEMOTH_POT_OF_HONEYCOMB),
            new BazaarItemSet(ItemType.LOTUS, "Lotus Atoll",
                    ItemType.LOTUS, ItemType.LOTUS_SILVER, ItemType.LOTUS_GOLD, ItemType.LOTUS_DIAMOND, ItemType.UNLUCKY_DOUBLOON, ItemType.REVERSE_REFORGE_STONE_CORE,
                    ItemType.FROGCOIN)
    ),
    ODDITIES(Material.ENCHANTING_TABLE, Material.PINK_STAINED_GLASS_PANE, "<d>",
            new BazaarItemSet(ItemType.BOOSTER_COOKIE, "Booster Cookie",
                    ItemType.BOOSTER_COOKIE),
            new BazaarItemSet(ItemType.RECOMBOBULATOR_3000, "Item Upgrades", "Modifiers",
                    ItemType.RECOMBOBULATOR_3000, ItemType.WOOD_SINGULARITY, ItemType.SIL_EX, ItemType.TRANSMISSION_TUNER, ItemType.DIVAN_POWDER_COATING, ItemType.GOLDEN_BOUNTY,
                    ItemType.MANA_DISINTEGRATOR, ItemType.PESTHUNTING_GUIDE, ItemType.GOLD_BOTTLE_CAP, ItemType.TROUBLED_BUBBLE, ItemType.SEVERED_PINCER, ItemType.OCTOPUS_TENDRIL,
                    ItemType.CHAIN_OF_THE_END_TIMES, ItemType.SEVERED_HAND, ItemType.ENDSTONE_IDOL, ItemType.ENSNARED_SNAIL, ItemType.FATEFUL_STINGER, ItemType.TURBO_GOURD,
                    ItemType.ENCHANTED_TURBO_GOURD, ItemType.OVERCLOCKER_3000, ItemType.PRICKLY_CREEPER, ItemType.VIBRANT_CORAL, ItemType.WATER_HYACINTH, ItemType.DISTANT_ECHO,
                    ItemType.FORESTS_FAVOR),
            new BazaarItemSet(ItemType.IMPLOSION_SCROLL, "Ability Scrolls", "Modifiers",
                    ItemType.IMPLOSION_SCROLL, ItemType.WITHER_SHIELD_SCROLL, ItemType.SHADOW_WARP_SCROLL, ItemType.FROGS_SPIRIT_SCROLL),
            new BazaarItemSet(ItemType.FIRST_MASTER_STAR, "Master Stars", "Modifiers",
                    ItemType.FIRST_MASTER_STAR, ItemType.SECOND_MASTER_STAR, ItemType.THIRD_MASTER_STAR, ItemType.FOURTH_MASTER_STAR, ItemType.FIFTH_MASTER_STAR),
            new BazaarItemSet(ItemType.HOT_POTATO_BOOK, "Book Modifiers", "Modifiers",
                    ItemType.HOT_POTATO_BOOK, ItemType.FUMING_POTATO_BOOK, ItemType.BOOK_OF_STATS, ItemType.THE_ART_OF_WAR, ItemType.FARMING_FOR_DUMMIES, ItemType.THE_ART_OF_PEACE,
                    ItemType.JALAPENO_BOOK, ItemType.BOOKWORM_BOOK, ItemType.POLARVOID_BOOK, ItemType.WET_BOOK),
            new BazaarItemSet(ItemType.FIGHTING_BOOSTER, "Foraging Boosters", "Modifiers",
                    ItemType.FIGHTING_BOOSTER, ItemType.FIGHTING_BOOSTER_UNCOMMON, ItemType.FORAGING_FORTUNE_BOOSTER, ItemType.FORAGING_FORTUNE_BOOSTER_UNCOMMON, ItemType.FORAGING_WISDOM_BOOSTER, ItemType.FORAGING_WISDOM_BOOSTER_UNCOMMON,
                    ItemType.SWEEP_BOOSTER, ItemType.SWEEP_BOOSTER_UNCOMMON, ItemType.HUNTING_WISDOM_BOOSTER_COMMON, ItemType.HUNTING_WISDOM_BOOSTER_UNCOMMON),
            BazaarItemSet.ofGroups(BazaarProducts.of(ItemType.ENCHANTED_BOOK), "Normal Enchantments", "Enchantments",
                            () -> BazaarProducts.enchantmentGroups(false))
                    .described("gui_bazaar.tile.normal_enchantments")
                    .arrow(Rarity.LEGENDARY)
                    .at(2),
            BazaarItemSet.ofGroups(BazaarProducts.of(ItemType.ENCHANTED_BOOK), "Ultimate Enchantments", "Enchantments",
                            () -> BazaarProducts.enchantmentGroups(true))
                    .described("gui_bazaar.tile.ultimate_enchantments")
                    .arrow(Rarity.MYTHIC),
            new BazaarItemSet(ItemType.EXPERIENCE_BOTTLE, "EXP Bottles",
                    ItemType.EXPERIENCE_BOTTLE, ItemType.GRAND_EXP_BOTTLE, ItemType.TITANIC_EXP_BOTTLE, ItemType.COLOSSAL_EXP_BOTTLE)
                    .at(3),
            new BazaarItemSet(ItemType.ATTRIBUTE_SHARD, "Shards", AttributeBazaarItems.values())
                    .described("gui_bazaar.tile.shards"),
            new BazaarItemSet(ItemType.BLAZE_WAX, "Reforge Stones",
                    ItemType.HARDENED_WOOD, ItemType.OVERFLOWING_TRASH_CAN, ItemType.SALT_CUBE, ItemType.AOTE_STONE, ItemType.BLAZEN_SPHERE, ItemType.BLAZE_WAX,
                    ItemType.BURROWING_SPORES, ItemType.CALCIFIED_HEART, ItemType.CANDY_CORN, ItemType.DIAMOND_ATOM, ItemType.DIAMONITE, ItemType.DRAGON_CLAW,
                    ItemType.DRAGON_SCALE, ItemType.DWARVEN_TREASURE, ItemType.ENDSTONE_GEODE, ItemType.ENTROPY_SUPPRESSOR, ItemType.FLOWERING_BOUQUET, ItemType.FULL_JAW_FANGING_KIT,
                    ItemType.JADERALD, ItemType.JERRY_STONE, ItemType.LAPIS_CRYSTAL, ItemType.LARGE_WALNUT, ItemType.MANGROVE_GEM, ItemType.METEOR_SHARD,
                    ItemType.MIDAS_JEWEL, ItemType.MOLTEN_CUBE, ItemType.MOONGLADE_JEWEL, ItemType.NECROMANCER_BROOCH, ItemType.ONYX, ItemType.OPTICAL_LENS,
                    ItemType.OVERGROWN_GRASS, ItemType.PITCHIN_KOI, ItemType.PREMIUM_FLESH, ItemType.PRESUMED_GALLON_OF_RED_PAINT, ItemType.PURE_MITHRIL, ItemType.RARE_DIAMOND,
                    ItemType.RED_NOSE, ItemType.RED_SCARF, ItemType.ROCK_GEMSTONE, ItemType.RUSTY_ANCHOR, ItemType.SALMON_OPAL, ItemType.SEARING_STONE,
                    ItemType.SHINY_PRISM, ItemType.BLOOMING_THORNS, ItemType.DAEDALUS_NOTES, ItemType.DIRT_BOTTLE, ItemType.EXTREMELY_MILD_ADHESIVE, ItemType.GEOMETRIC_ODDITY,
                    ItemType.HASHBROWN, ItemType.MANTID_CLAW, ItemType.MARSHROOM, ItemType.MOIL_LOG, ItemType.MOONSTONE, ItemType.MORNING_DEW,
                    ItemType.OVERPRICED_DRINK, ItemType.SHRIVELED_CORNEA, ItemType.SUNSTONE, ItemType.TOIL_LOG, ItemType.SKYMART_BROCHURE,
                    ItemType.SPIRIT_DECOY, ItemType.SQUEAKY_TOY, ItemType.SUSPICIOUS_VIAL, ItemType.TERRY_SNOWGLOBE, ItemType.TITANIUM_TESSERACT, ItemType.AMBER_MATERIAL,
                    ItemType.BEADY_EYES, ItemType.BLESSED_FRUIT, ItemType.BULKY_STONE, ItemType.CLIPPED_WINGS, ItemType.DEEP_SEA_ORB, ItemType.DRAGON_HORN,
                    ItemType.FROZEN_BAUBLE, ItemType.GIANT_TOOTH, ItemType.GLEAMING_CRYSTAL, ItemType.GOLDEN_BALL, ItemType.KUUDRA_MANDIBLE, ItemType.LUCKY_DICE,
                    ItemType.PETRIFIED_STARFALL, ItemType.POCKET_ICEBERG, ItemType.PRECURSOR_GEAR, ItemType.REFINED_AMBER, ItemType.SADAN_BROOCH, ItemType.WITHER_BLOOD,
                    ItemType.FRIGID_HUSK, ItemType.HOT_STUFF)
                    .described("gui_bazaar.tile.reforge_stones"),
            new BazaarItemSet(ItemType.MAGMA_URCHIN, "Power Stones",
                    ItemType.DISPLACED_LEECH, ItemType.LUXURIOUS_SPOOL, ItemType.ROCK_CANDY, ItemType.ACACIA_BIRDHOUSE, ItemType.BEATING_HEART, ItemType.DARK_ORB,
                    ItemType.ENDER_MONOCLE, ItemType.END_STONE_SHULKER, ItemType.FURBALL, ItemType.MANDRAA, ItemType.OBSIDIAN_TABLET, ItemType.HORNS_OF_TORMENT,
                    ItemType.MAGMA_URCHIN, ItemType.PRECIOUS_PEARL, ItemType.BUBBA_BLISTER, ItemType.CHOCOLATE_CHIP, ItemType.ECCENTRIC_PAINTING, ItemType.GLACITE_SHARD,
                    ItemType.HAZMAT_ENDERMAN, ItemType.VITAMIN_DEATH, ItemType.SCORCHED_BOOKS),
            new BazaarItemSet(ItemType.STOCK_OF_STONKS, "Stock of Stonks",
                    ItemType.STOCK_OF_STONKS),
            new BazaarItemSet(ItemType.SIMPLE_CARROT_CANDY, "Pet Candies", "Pet Upgrades",
                    ItemType.SIMPLE_CARROT_CANDY, ItemType.GREAT_CARROT_CANDY, ItemType.SUPERB_CARROT_CANDY, ItemType.ULTIMATE_CARROT_CANDY, ItemType.CHYME, ItemType.PET_CAKE),
            new BazaarItemSet(ItemType.GRIFFIN_UPGRADE_STONE_UNCOMMON, "Griffin Upgrade Stones", "Pet Upgrades",
                    ItemType.GRIFFIN_UPGRADE_STONE_UNCOMMON, ItemType.GRIFFIN_UPGRADE_STONE_RARE, ItemType.GRIFFIN_UPGRADE_STONE_EPIC, ItemType.GRIFFIN_UPGRADE_STONE_LEGENDARY, ItemType.GRIFFIN_UPGRADE_STONE_MYTHIC),
            new BazaarItemSet(ItemType.SMALL_PIG_EAR, "Pig Ears", "Pet Upgrades",
                    ItemType.SMALL_PIG_EAR, ItemType.MEDIUM_PIG_EAR, ItemType.LARGE_PIG_EAR, ItemType.GIANT_PIG_EAR),
            new BazaarItemSet(ItemType.SMALL_SEAL_PLUSHIE, "Seal Plushies", "Pet Upgrades",
                    ItemType.SMALL_SEAL_PLUSHIE, ItemType.MEDIUM_SEAL_PLUSHIE, ItemType.LARGE_SEAL_PLUSHIE, ItemType.GIANT_SEAL_PLUSHIE),
            new BazaarItemSet(ItemType.SMALL_HONEY_DIPPER, "Honey Dippers", "Pet Upgrades",
                    ItemType.SMALL_HONEY_DIPPER, ItemType.MEDIUM_HONEY_DIPPER, ItemType.LARGE_HONEY_DIPPER, ItemType.GIANT_HONEY_DIPPER),
            new BazaarItemSet(ItemType.SMALL_WITCH_CAULDRON, "Witch Cauldrons", "Pet Upgrades",
                    ItemType.SMALL_WITCH_CAULDRON, ItemType.MEDIUM_WITCH_CAULDRON, ItemType.LARGE_WITCH_CAULDRON, ItemType.GIANT_WITCH_CAULDRON),
            new BazaarItemSet(ItemType.SMALL_FROG_TREAT, "Frog Treats", "Pet Upgrades",
                    ItemType.SMALL_FROG_TREAT, ItemType.MEDIUM_FROG_TREAT, ItemType.LARGE_FROG_TREAT, ItemType.GIANT_FROG_TREAT),
            new BazaarItemSet(ItemType.SMALL_SLOTH_CLAW, "Sloth Claws", "Pet Upgrades",
                    ItemType.SMALL_SLOTH_CLAW, ItemType.MEDIUM_SLOTH_CLAW, ItemType.LARGE_SLOTH_CLAW, ItemType.GIANT_SLOTH_CLAW),
            BazaarItemSet.product(ItemType.PET_ITEM_LUCKY_CLOVER_DROP, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.PET_ITEM_EXP_SHARE_DROP, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.PET_ITEM_TIER_BOOST_DROP, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.RADIOACTIVE_VIAL, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.ENDERMAN_CORTEX_REWRITER, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.PET_ITEM_TOY_JERRY, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.PET_ITEM_VAMPIRE_FANG, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.MIXED_MITE_GEL, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.PET_ITEM_PURE_MITHRIL_GEM, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.BLACK_WOOLEN_YARN, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.MAGIC_TOP_HAT, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.GUARDIAN_LUCKY_BLOCK, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.PET_ITEM_CHOCOLATE_SYRINGE, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.SCUTTLER_SHELL, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.FROZEN_SCUTE, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.TRUNK_POLISH, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.ROYAL_JELLY, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.UNALLOYED_SPEED, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.POCKET_SIZED_IGLOO, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.BUCKET_OF_FROGSPAWN, "Pet Upgrades"),
            BazaarItemSet.product(ItemType.RAINBOW_FEATHER, "Pet Upgrades"),
            new BazaarItemSet(ItemType.INFERNO_VERTEX, "Inferno Minion",
                    ItemType.CRUDE_GABAGOOL, ItemType.VERY_CRUDE_GABAGOOL, ItemType.FUEL_GABAGOOL, ItemType.HEAVY_GABAGOOL, ItemType.HYPERGOLIC_GABAGOOL, ItemType.INFERNO_VERTEX,
                    ItemType.INFERNO_APEX, ItemType.REAPER_PEPPER, ItemType.CHILI_PEPPER, ItemType.STUFFED_CHILI_PEPPER, ItemType.INFERNO_FUEL_BLOCK, ItemType.HYPERGOLIC_IONIZED_CERAMICS,
                    ItemType.SULPHURIC_COAL, ItemType.REHEATED_GUMMY_POLAR_BEAR, ItemType.CAPSAICIN_EYEDROPS_NO_CHARGES),
            new BazaarItemSet(ItemType.SMALL_ENCHANTED_CHEST, "Minion Storage", "Minion Upgrades",
                    ItemType.SMALL_ENCHANTED_CHEST, ItemType.MEDIUM_ENCHANTED_CHEST, ItemType.LARGE_ENCHANTED_CHEST, ItemType.XLARGE_ENCHANTED_CHEST, ItemType.XXLARGE_ENCHANTED_CHEST),
            new BazaarItemSet(ItemType.COMPACTOR, "Compactors", "Minion Upgrades",
                    ItemType.COMPACTOR, ItemType.DWARVEN_COMPACTOR, ItemType.SUPER_COMPACTOR_3000),
            BazaarItemSet.product(ItemType.DIAMOND_SPREADING, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.MINION_EXPANDER, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.POTATO_SPREADING, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.AUTO_SMELTER, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.SOULFLOW_ENGINE, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.LESSER_SOULFLOW_ENGINE, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.BUDGET_HOPPER, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.ENCHANTED_HOPPER, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.CORRUPT_SOIL, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.MITHRIL_INFUSION, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.BERBERIS_FUEL_INJECTOR, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.FLYCATCHER_UPGRADE, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.SLEEPY_HOLLOW, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.FREE_WILL, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_FISHING_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_CLAY_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_REVENANT_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_TARANTULA_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_ACACIA_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_BIRCH_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_DARK_OAK_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_JUNGLE_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_OAK_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_SPRUCE_12, "Minion Upgrades"),
            BazaarItemSet.product(ItemType.GENERATOR_UPGRADE_STONE_LILY_PAD_12, "Minion Upgrades"),
            new BazaarItemSet(ItemType.ENCHANTED_LAVA_BUCKET, "Fuels",
                    ItemType.ENCHANTED_LAVA_BUCKET, ItemType.MAGMA_BUCKET, ItemType.PLASMA_BUCKET, ItemType.HAMSTER_WHEEL, ItemType.FOUL_FLESH, ItemType.POWER_CRYSTAL,
                    ItemType.CATALYST, ItemType.HYPER_CATALYST, ItemType.SOLAR_PANEL, ItemType.OIL_BARREL, ItemType.SCORCHED_POWER_CRYSTAL, ItemType.VOLTA,
                    ItemType.DAYSWITCH, ItemType.NIGHTSWITCH, ItemType.THORNY_VINES, ItemType.SUNFLOWER_OIL),
            BazaarItemSet.ofProducts(BazaarProducts.essences().getFirst(), "Essence", null, BazaarProducts.essences())
                    .at(12),
            new BazaarItemSet(ItemType.GREEN_CANDY, "Spooky", "Events",
                    ItemType.GREEN_CANDY, ItemType.PURPLE_CANDY, ItemType.DARK_CANDY, ItemType.ECTOPLASM, ItemType.PUMPKIN_GUTS, ItemType.SPOOKY_SHARD,
                    ItemType.WEREWOLF_SKIN, ItemType.SOUL_FRAGMENT, ItemType.BAT_FIREWORK, ItemType.HORSEMAN_CANDLE, ItemType.EXPIRED_PUMPKIN, ItemType.BLAST_O_LANTERN)
                    .at(13),
            new BazaarItemSet(ItemType.GREEN_GIFT, "Winter", "Events",
                    ItemType.WHITE_GIFT, ItemType.GREEN_GIFT, ItemType.RED_GIFT, ItemType.PARTY_GIFT, ItemType.REFINED_BOTTLE_OF_JYRRE, ItemType.VOLCANIC_ROCK),
            new BazaarItemSet(ItemType.AGATHA_COUPON, "Minerals", "Events",
                    ItemType.REFINED_MINERAL, ItemType.GLOSSY_GEMSTONE, ItemType.JACOBS_TICKET, ItemType.AGATHA_COUPON),
            new BazaarItemSet(ItemType.SHARK_FIN, "Fishing Festival", "Events",
                    ItemType.NURSE_SHARK_TOOTH, ItemType.BLUE_SHARK_TOOTH, ItemType.TIGER_SHARK_TOOTH, ItemType.GREAT_WHITE_SHARK_TOOTH, ItemType.SHARK_FIN, ItemType.ENCHANTED_SHARK_FIN,
                    ItemType.GREAT_WHITE_TOOTH_MEAL),
            new BazaarItemSet(ItemType.JERRY_BOX_GREEN, "Jerry Boxes", "Events",
                    ItemType.JERRY_BOX_GREEN, ItemType.JERRY_BOX_BLUE, ItemType.JERRY_BOX_PURPLE, ItemType.JERRY_BOX_GOLDEN),
            new BazaarItemSet(ItemType.AVARICIOUS_CHALICE, "Diaz", "Events",
                    ItemType.AVARICIOUS_CHALICE, ItemType.FRESHLY_MINTED_COINS, ItemType.BLOOD_STAINED_COINS, ItemType.BLOOD_SOAKED_COINS),
            new BazaarItemSet(ItemType.REFINED_DARK_CACAO_TRUFFLE, "Hoppity's Hunt", "Events",
                    ItemType.SUPREME_CHOCOLATE_BAR, ItemType.REFINED_DARK_CACAO_TRUFFLE),
            new BazaarItemSet(ItemType.FACTION_RABBIT_ALLEY, "Hoppity Rabbits", "Events",
                    ItemType.FACTION_RABBIT_ALLEY, ItemType.FACTION_RABBIT_ANCHOR, ItemType.FACTION_RABBIT_ASTER, ItemType.FACTION_RABBIT_AVALANCHE, ItemType.FACTION_RABBIT_CANYON, ItemType.FACTION_RABBIT_CHASM,
                    ItemType.FACTION_RABBIT_CLOVER, ItemType.FACTION_RABBIT_COBBLE, ItemType.FACTION_RABBIT_COCONUT, ItemType.FACTION_RABBIT_CRAG, ItemType.FACTION_RABBIT_DAISY, ItemType.FACTION_RABBIT_DUNE,
                    ItemType.FACTION_RABBIT_EAGLE, ItemType.FACTION_RABBIT_GLACIER, ItemType.FACTION_RABBIT_GRAFFITI, ItemType.FACTION_RABBIT_GRANITE, ItemType.FACTION_RABBIT_HAYBALE, ItemType.FACTION_RABBIT_HAZEL,
                    ItemType.FACTION_RABBIT_HORIZON, ItemType.FACTION_RABBIT_ICEFUR, ItemType.FACTION_RABBIT_IRIS, ItemType.FACTION_RABBIT_JUNIPER, ItemType.FACTION_RABBIT_LAGOON, ItemType.FACTION_RABBIT_LAVENDER,
                    ItemType.FACTION_RABBIT_MARIGOLD, ItemType.FACTION_RABBIT_MAYOR, ItemType.FACTION_RABBIT_MINISTER, ItemType.FACTION_RABBIT_MISTRAL, ItemType.FACTION_RABBIT_MOCKTAIL, ItemType.FACTION_RABBIT_MOSS,
                    ItemType.FACTION_RABBIT_NECTAR, ItemType.FACTION_RABBIT_NEON, ItemType.FACTION_RABBIT_OASIS, ItemType.FACTION_RABBIT_PALM, ItemType.FACTION_RABBIT_PEON, ItemType.FACTION_RABBIT_ROOT,
                    ItemType.FACTION_RABBIT_ROSEBUD, ItemType.FACTION_RABBIT_SANDY, ItemType.FACTION_RABBIT_SCREE, ItemType.FACTION_RABBIT_SEABUN, ItemType.FACTION_RABBIT_SEASHELL, ItemType.FACTION_RABBIT_SHORTCUT,
                    ItemType.FACTION_RABBIT_SILO, ItemType.FACTION_RABBIT_SKYRISE, ItemType.FACTION_RABBIT_SPEEDY, ItemType.FACTION_RABBIT_SPLASH, ItemType.FACTION_RABBIT_STARFISH, ItemType.FACTION_RABBIT_SUBWAY,
                    ItemType.FACTION_RABBIT_SUMMIT, ItemType.FACTION_RABBIT_SUNSET, ItemType.FACTION_RABBIT_SURF, ItemType.FACTION_RABBIT_TAN, ItemType.FACTION_RABBIT_TAXI, ItemType.FACTION_RABBIT_TIMBER,
                    ItemType.FACTION_RABBIT_TRAFFIC, ItemType.FACTION_RABBIT_TUNDRA, ItemType.FACTION_RABBIT_TUNNEL, ItemType.FACTION_RABBIT_TWIGY, ItemType.FACTION_RABBIT_VALLEY, ItemType.FACTION_RABBIT_WALKER),
            new BazaarItemSet(ItemType.BOUNCY_BEACH_BALL, "Year of the Seal", "Events",
                    ItemType.BOUNCY_BEACH_BALL, ItemType.GIANT_BOUNCY_BEACH_BALL),
            new BazaarItemSet(ItemType.AGGOURDIAN, "Harvest Feast", "Events",
                    ItemType.AGGOURDIAN, ItemType.BOTROOT, ItemType.CACTUS_FLOWER, ItemType.CANE_KNOT, ItemType.CARROT_ZEST, ItemType.CORNUCOPIA,
                    ItemType.CRYSTALIZED_MOONLIGHT, ItemType.DEEPFRIES, ItemType.DESIGNER_COFFEE_BEANS, ItemType.FEASTFUNGUS, ItemType.FLORAL_GELATIN, ItemType.MELON_JUICE,
                    ItemType.SALTED_SUNFLOWER_SEEDS, ItemType.FEAST_FLASK),
            new BazaarItemSet(ItemType.METAPHYSICAL_SERUM, "Consumables",
                    ItemType.MAGIC_MUSHROOM_SOUP, ItemType.SUPER_MAGIC_MUSHROOM_SOUP, ItemType.METAPHYSICAL_SERUM, ItemType.DWARVEN_OS_ORE_OATS, ItemType.DWARVEN_OS_BLOCK_BRAN, ItemType.DWARVEN_OS_GEMSTONE_GRAHAMS,
                    ItemType.DWARVEN_OS_METALLIC_MINIS, ItemType.SPOTLITE, ItemType.MOBY_DUCK_COLLECTOR_EDITION, ItemType.VIAL_OF_VENOM, ItemType.FESTERING_MAGGOT, ItemType.BRAIN_FOOD,
                    ItemType.CELESTIAL_MASON_JAR, ItemType.VERY_MOLDY_BREAD, ItemType.FEAST_BURGER, ItemType.MOUND_OF_SEAGRASS, ItemType.ISOPOD_HUSK, ItemType.BEE_SALIVA),
            new BazaarItemSet(ItemType.AGARIMOO_TONGUE, "Cross-Dimensional",
                    ItemType.LUSH_BERBERIS, ItemType.ENCHANTED_LUSH_BERBERIS, ItemType.AGARIMOO_TONGUE, ItemType.EXPORTABLE_CARROTS, ItemType.HEMOVIBE, ItemType.HEMOGLASS,
                    ItemType.HEMOBOMB, ItemType.HIGHLITE, ItemType.FAKE_SHURIKEN),
            new BazaarItemSet(ItemType.BONZO_FRAGMENT, "Upgrade Fragments",
                    ItemType.BONZO_FRAGMENT, ItemType.DARK_QUEENS_SOUL_DROP, ItemType.GOLDEN_FRAGMENT, ItemType.LIVID_FRAGMENT, ItemType.SCARF_FRAGMENT, ItemType.THORN_FRAGMENT,
                    ItemType.WINTER_FRAGMENT)
    ),
    ;

    private final Material displayItem;
    private final Material glassItem;
    private final Set<BazaarItemSet> items;
    private final String color;

    BazaarCategories(Material displayItem, Material glassItem, String color, BazaarItemSet... items) {
        this.displayItem = displayItem;
        this.glassItem = glassItem;
        this.color = color;
        this.items = Collections.unmodifiableSet(new LinkedHashSet<>(List.of(items)));
    }

    BazaarCategories(Material displayItem, Material glassItem, String color) {
        this.displayItem = displayItem;
        this.glassItem = glassItem;
        this.color = color;
        this.items = Set.of();
    }

    @Override
    public String toString() {
        return displayName();
    }

    public String displayName() {
        return this == WOODS_AND_FISHES ? "Woods & Fishes" : StringUtility.toNormalCase(name());
    }

    public List<BazaarTile> tiles() {
        Map<String, List<BazaarItemSet>> groups = new LinkedHashMap<>();
        List<BazaarTile> tiles = new ArrayList<>();
        Map<String, Integer> groupTileIndex = new HashMap<>();
        for (BazaarItemSet set : items) {
            if (set.nestedGroup == null) {
                tiles.add(new BazaarTile(set.name(), List.of(set), false, set.gridPosition));
                continue;
            }
            List<BazaarItemSet> members = groups.get(set.nestedGroup);
            if (members == null) {
                members = new ArrayList<>();
                groups.put(set.nestedGroup, members);
                groupTileIndex.put(set.nestedGroup, tiles.size());
                tiles.add(null);
            }
            members.add(set);
        }
        for (Map.Entry<String, Integer> entry : groupTileIndex.entrySet()) {
            List<BazaarItemSet> members = List.copyOf(groups.get(entry.getKey()));
            tiles.set(entry.getValue(), new BazaarTile(entry.getKey(), members, true, members.getFirst().gridPosition));
        }
        return List.copyOf(tiles);
    }

    public List<BazaarItemSet> nestedSets(String group) {
        return items.stream().filter(set -> group.equals(set.nestedGroup)).toList();
    }

    public static Map.Entry<BazaarCategories, BazaarItemSet> getFromItem(ItemType itemType) {
        return Lookup.BY_ITEM.get(itemType);
    }

    public static Map.Entry<BazaarCategories, BazaarItemSet> getFromProduct(BazaarProduct product) {
        ItemType itemType = product.itemType();
        return itemType != null ? Lookup.BY_ITEM.get(itemType) : Lookup.BY_OTHER_PRODUCT.get(product.id());
    }

    private static final class Lookup {
        private static final Map<ItemType, Map.Entry<BazaarCategories, BazaarItemSet>> BY_ITEM = build();
        private static final Map<String, Map.Entry<BazaarCategories, BazaarItemSet>> BY_OTHER_PRODUCT = buildOther();

        private static Map<String, Map.Entry<BazaarCategories, BazaarItemSet>> buildOther() {
            Map<String, Map.Entry<BazaarCategories, BazaarItemSet>> index = new HashMap<>();
            for (BazaarCategories category : values()) {
                for (BazaarItemSet itemSet : category.items) {
                    if (!itemSet.items.isEmpty()) {
                        continue;
                    }
                    for (BazaarProduct product : itemSet.products()) {
                        index.putIfAbsent(product.id(), new AbstractMap.SimpleEntry<>(category, itemSet));
                    }
                }
            }
            return index;
        }

        private static Map<ItemType, Map.Entry<BazaarCategories, BazaarItemSet>> build() {
            Map<ItemType, Map.Entry<BazaarCategories, BazaarItemSet>> index = new HashMap<>();
            for (BazaarCategories category : values()) {
                for (BazaarItemSet itemSet : category.items) {
                    for (ItemType type : itemSet.items) {
                        index.putIfAbsent(type, new AbstractMap.SimpleEntry<>(category, itemSet));
                    }
                }
            }
            return index;
        }
    }
}
