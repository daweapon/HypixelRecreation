package net.swofty.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import org.jetbrains.annotations.Nullable;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.levels.causes.CollectedMobTypesCause;
import net.swofty.type.skyblockgeneric.levels.causes.CollectionLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.FairySoulExchangeLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.MissionLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.MuseumLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.NewAccessoryLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.SkillLevelCause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The complete catalogue of real Hypixel SkyBlock XP sources.
 *
 * <p>Values are taken from the "Main Levels" sheet of the Skyblock Things Spreadsheet by
 * &#64;Georik (column for the 01.09.2026 "Rabbit Faction" update), cross-referenced against
 * the SkyBlock Wiki's SkyBlock Levels/Tasks page. The group subtotals reconcile exactly to
 * {@link #TOTAL_MAX_XP}, which is {@code 620.19} levels at 100 XP per level — real Hypixel's
 * maximum obtainable SkyBlock Level for that version.
 *
 * <p>Every real source is listed here regardless of whether this server implements it yet, so
 * that wiring a system up later is a single {@code addSourceXP} call rather than a schema
 * change. {@link Availability} records how far along each one is.
 */
@Getter
public enum SkyBlockXPSource {
    // ----- Core Tasks (19,810 XP) -----
    SKILL_LEVEL_UP(Group.CORE, null, 8710, Availability.LIVE, Material.DIAMOND_SWORD),
    MUSEUM_DONATIONS(Group.CORE, null, 3646, Availability.LIVE, Material.GOLD_BLOCK),
    FAIRY_SOULS(Group.CORE, null, 570, Availability.LIVE, Material.LIGHT_BLUE_DYE),
    ACCESSORY_BAG(Group.CORE, null, 0, Availability.LIVE, Material.FLOWER_POT),
    PET_SCORE(Group.CORE, null, 0, Availability.LIVE, Material.BONE),
    COLLECTIONS(Group.CORE, null, 3160, Availability.LIVE, Material.PAINTING),
    CRAFT_MINIONS(Group.CORE, null, 3164, Availability.UNWIRED, Material.PLAYER_HEAD),
    BANK_UPGRADES(Group.CORE, null, 200, Availability.UNWIRED, Material.GOLD_INGOT),
    FAST_TRAVELS_UNLOCKED(Group.CORE, null, 360, Availability.UNWIRED, Material.MAP),

    // ----- Event Tasks (1,439 XP) -----
    MINING_FIESTA(Group.EVENT, null, 200, Availability.NOT_IN_GAME, Material.IRON_PICKAXE),
    FISHING_FESTIVAL(Group.EVENT, null, 100, Availability.LIVE, Material.FISHING_ROD),
    HARVEST_FEAST_SEASONING(Group.EVENT, null, 100, Availability.NOT_IN_GAME, Material.PUMPKIN_PIE),
    SPOOKY_FESTIVAL(Group.EVENT, null, 225, Availability.UNWIRED, Material.JACK_O_LANTERN),
    HOPPITYS_HUNT(Group.EVENT, null, 285, Availability.UNWIRED, Material.COCOA_BEANS),
    JACOBS_FARMING_CONTEST(Group.EVENT, null, 130, Availability.LIVE, Material.GOLDEN_HOE),
    FISHING_FESTIVAL_PERK_SHOP(Group.EVENT, null, 49, Availability.NOT_IN_GAME, Material.COD),
    MINING_FIESTA_PERK_SHOP(Group.EVENT, null, 49, Availability.NOT_IN_GAME, Material.RAW_IRON),
    MYTHOLOGICAL_RITUAL_PERK_SHOP(Group.EVENT, null, 49, Availability.LIVE, Material.FEATHER),
    SPOOKY_FESTIVAL_PERK_SHOP(Group.EVENT, null, 49, Availability.LIVE, Material.PUMPKIN),
    SEASON_OF_JERRY_PERK_SHOP(Group.EVENT, null, 49, Availability.LIVE, Material.SNOWBALL),
    HARVEST_FEAST_PERK_SHOP(Group.EVENT, null, 49, Availability.NOT_IN_GAME, Material.BREAD),
    YEAR_OF_THE_WITCH(Group.EVENT, null, 105, Availability.NOT_IN_GAME, Material.CAULDRON),

    // ----- Dungeon Tasks (2,760 XP) -----
    CATACOMBS_LEVEL_UP(Group.DUNGEON, null, 1220, Availability.NOT_IN_GAME, Material.WITHER_SKELETON_SKULL),
    CLASS_LEVEL_UP(Group.DUNGEON, null, 1000, Availability.NOT_IN_GAME, Material.WITHER_SKELETON_SKULL),
    COMPLETE_DUNGEONS(Group.DUNGEON, null, 540, Availability.NOT_IN_GAME, Material.WITHER_SKELETON_SKULL),

    // ----- Essence Shop Tasks (2,121 XP) -----
    DRAGON_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 185, Availability.NOT_IN_GAME, Material.DRAGON_HEAD),
    ICE_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 121, Availability.LIVE, Material.ICE),
    SPIDER_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 126, Availability.NOT_IN_GAME, Material.COBWEB),
    UNDEAD_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 128, Availability.NOT_IN_GAME, Material.ZOMBIE_HEAD),
    WITHER_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 161, Availability.NOT_IN_GAME, Material.WITHER_SKELETON_SKULL),
    CRIMSON_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 194, Availability.LIVE, Material.CRIMSON_FUNGUS),
    GOLD_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 188, Availability.NOT_IN_GAME, Material.GOLD_BLOCK),
    DIAMOND_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 226, Availability.NOT_IN_GAME, Material.DIAMOND),
    FOREST_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 171, Availability.NOT_IN_GAME, Material.OAK_SAPLING),
    FOSSIL_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 226, Availability.NOT_IN_GAME, Material.BONE_BLOCK),
    SAFARI_ESSENCE_SHOP(Group.ESSENCE_SHOP, null, 395, Availability.NOT_IN_GAME, Material.LEAD),

    // ----- Slaying Tasks (12,315 XP) -----
    SLAYER_LEVEL_UP(Group.SLAYING, null, 3825, Availability.LIVE, Material.POTION),
    BOSS_COLLECTIONS(Group.SLAYING, null, 1015, Availability.NOT_IN_GAME, Material.BONE_BLOCK),
    BESTIARY_PROGRESS(Group.SLAYING, null, 5655, Availability.UNWIRED, Material.WRITTEN_BOOK),
    MYTHOLOGICAL_KILLS(Group.SLAYING, null, 100, Availability.LIVE, Material.BONE_MEAL),
    COLLECT_MOB_TYPES(Group.SLAYING, null, 35, Availability.LIVE, Material.ZOMBIE_SPAWN_EGG),
    SLAY_DRAGONS(Group.SLAYING, null, 200, Availability.NOT_IN_GAME, Material.DRAGON_EGG),
    DEFEAT_SLAYERS(Group.SLAYING, null, 1125, Availability.UNWIRED, Material.ROTTEN_FLESH),
    DEFEAT_KUUDRA(Group.SLAYING, null, 300, Availability.NOT_IN_GAME, Material.MAGMA_CREAM),
    DEFEAT_ARACHNE(Group.SLAYING, null, 60, Availability.NOT_IN_GAME, Material.SPIDER_EYE),

    // ----- Skill Related Tasks (11,732 XP) -----
    HEART_OF_THE_MOUNTAIN(Group.SKILL_RELATED, SubArea.MINING, 3117, Availability.UNWIRED, Material.REDSTONE),
    COMMISSION_MILESTONES(Group.SKILL_RELATED, SubArea.MINING, 255, Availability.LIVE, Material.PAPER),
    CRYSTAL_NUCLEUS_RUNS(Group.SKILL_RELATED, SubArea.MINING, 200, Availability.NOT_IN_GAME, Material.AMETHYST_SHARD),
    CORE_OF_THE_MOUNTAIN(Group.SKILL_RELATED, SubArea.MINING, 1000, Availability.NOT_IN_GAME, Material.NETHERITE_PICKAXE),
    FOSSIL_RESEARCH(Group.SKILL_RELATED, SubArea.MINING, 80, Availability.NOT_IN_GAME, Material.BONE),
    FROZEN_CORPSE_MILESTONES(Group.SKILL_RELATED, SubArea.MINING, 140, Availability.NOT_IN_GAME, Material.PACKED_ICE),
    ROCK_MILESTONES(Group.SKILL_RELATED, SubArea.MINING, 100, Availability.NOT_IN_GAME, Material.COBBLESTONE),

    ANITAS_SHOP_UPGRADES(Group.SKILL_RELATED, SubArea.FARMING, 250, Availability.LIVE, Material.GOLDEN_HOE),
    GARDEN_LEVEL(Group.SKILL_RELATED, SubArea.FARMING, 140, Availability.NOT_IN_GAME, Material.WHEAT),
    GARDEN_VISITORS_SERVED(Group.SKILL_RELATED, SubArea.FARMING, 96, Availability.NOT_IN_GAME, Material.VILLAGER_SPAWN_EGG),
    OFFERS_ACCEPTED(Group.SKILL_RELATED, SubArea.FARMING, 90, Availability.NOT_IN_GAME, Material.EMERALD),
    CROP_UPGRADES(Group.SKILL_RELATED, SubArea.FARMING, 117, Availability.NOT_IN_GAME, Material.CARROT),
    GARDEN_CROP_MILESTONES_REACHED(Group.SKILL_RELATED, SubArea.FARMING, 598, Availability.NOT_IN_GAME, Material.HAY_BLOCK),
    GARDEN_UNLOCKED_PLOTS(Group.SKILL_RELATED, SubArea.FARMING, 120, Availability.NOT_IN_GAME, Material.GRASS_BLOCK),
    COMPOSTER_UPGRADES(Group.SKILL_RELATED, SubArea.FARMING, 305, Availability.NOT_IN_GAME, Material.COMPOSTER),
    GREENHOUSE_UPGRADES(Group.SKILL_RELATED, SubArea.FARMING, 100, Availability.NOT_IN_GAME, Material.GLASS),
    DNA_ANALYSIS_MILESTONES(Group.SKILL_RELATED, SubArea.FARMING, 90, Availability.NOT_IN_GAME, Material.BREWING_STAND),
    FARMING_CHIPS(Group.SKILL_RELATED, SubArea.FARMING, 410, Availability.NOT_IN_GAME, Material.REPEATER),
    SOWDUST(Group.SKILL_RELATED, SubArea.FARMING, 250, Availability.NOT_IN_GAME, Material.SAND),

    TROPHY_FISH(Group.SKILL_RELATED, SubArea.FISHING, 1080, Availability.LIVE, Material.TROPICAL_FISH),
    TROPHY_FROGS(Group.SKILL_RELATED, SubArea.FISHING, 540, Availability.NOT_IN_GAME, Material.LILY_PAD),
    DOLPHIN_MILESTONES(Group.SKILL_RELATED, SubArea.FISHING, 100, Availability.NOT_IN_GAME, Material.COD),
    SHIP_PARTS(Group.SKILL_RELATED, SubArea.FISHING, 15, Availability.NOT_IN_GAME, Material.OAK_PLANKS),
    SHIP_CREW(Group.SKILL_RELATED, SubArea.FISHING, 20, Availability.NOT_IN_GAME, Material.OAK_BOAT),

    HEART_OF_THE_FOREST(Group.SKILL_RELATED, SubArea.FORAGING, 1965, Availability.NOT_IN_GAME, Material.OAK_LOG),
    CENTER_OF_THE_FOREST(Group.SKILL_RELATED, SubArea.FORAGING, 250, Availability.NOT_IN_GAME, Material.OAK_SAPLING),
    TREE_GIFT_MILESTONES(Group.SKILL_RELATED, SubArea.FORAGING, 84, Availability.NOT_IN_GAME, Material.CHEST_MINECART),
    SAFARI_MILESTONES(Group.SKILL_RELATED, SubArea.FORAGING, 220, Availability.NOT_IN_GAME, Material.LEAD),

    // ----- Miscellaneous Tasks (6,266 XP) -----
    ACCESSORY_BAG_UPGRADES(Group.MISCELLANEOUS, null, 0, Availability.LIVE, Material.BUNDLE),
    TIMECHARMS(Group.MISCELLANEOUS, null, 800, Availability.NOT_IN_GAME, Material.CLOCK),
    UNLOCKING_POWERS(Group.MISCELLANEOUS, null, 0, Availability.NOT_IN_GAME, Material.HEART_OF_THE_SEA),
    UPGRADED_RELAYS(Group.MISCELLANEOUS, null, 45, Availability.NOT_IN_GAME, Material.LIGHTNING_ROD),
    TAMING_CAP_INCREASES(Group.MISCELLANEOUS, null, 100, Availability.NOT_IN_GAME, Material.WOLF_SPAWN_EGG),
    HOOTIE_UPGRADES(Group.MISCELLANEOUS, null, 65, Availability.NOT_IN_GAME, Material.FEATHER),
    STARLYN_SISTER_LEVELS(Group.MISCELLANEOUS, null, 40, Availability.NOT_IN_GAME, Material.NETHER_STAR),
    BEACON_TIERS(Group.MISCELLANEOUS, null, 40, Availability.NOT_IN_GAME, Material.BEACON),
    ATTRIBUTE_LEVELS(Group.MISCELLANEOUS, null, 3210, Availability.UNWIRED, Material.PRISMARINE_SHARD),
    FISH_FAMILY(Group.MISCELLANEOUS, null, 45, Availability.NOT_IN_GAME, Material.COD),
    CARROLYNS_EXPORTABLE_CROPS(Group.MISCELLANEOUS, null, 40, Availability.NOT_IN_GAME, Material.CARROT),
    THE_DOJO(Group.MISCELLANEOUS, null, 425, Availability.LIVE, Material.LEATHER_CHESTPLATE),
    HARP_SONGS(Group.MISCELLANEOUS, null, 236, Availability.UNWIRED, Material.ENCHANTED_BOOK),
    ABIPHONE_CONTACTS(Group.MISCELLANEOUS, null, 840, Availability.UNWIRED, Material.PAPER),
    COMMUNITY_SHOP_UPGRADES(Group.MISCELLANEOUS, null, 120, Availability.UNWIRED, Material.EMERALD_BLOCK),
    PERSONAL_BANK_UPGRADES(Group.MISCELLANEOUS, null, 110, Availability.LIVE, Material.GOLD_BLOCK),
    MAGE_REPUTATION(Group.MISCELLANEOUS, null, 75, Availability.LIVE, Material.BLUE_BANNER),
    BARBARIAN_REPUTATION(Group.MISCELLANEOUS, null, 75, Availability.LIVE, Material.RED_BANNER),

    // ----- Story Tasks (1,132 XP) -----
    RIFT_GUIDE(Group.STORY, null, 360, Availability.NOT_IN_GAME, Material.ENDER_EYE),
    COMPLETE_OBJECTIVES(Group.STORY, null, 212, Availability.LIVE, Material.ENCHANTED_BOOK),
    CHAPTERS(Group.STORY, null, 560, Availability.NOT_IN_GAME, Material.WRITABLE_BOOK),

    // ----- Consumables Tasks (299 XP) -----
    METAPHYSICAL_SERUM(Group.CONSUMABLES, null, 15, Availability.NOT_IN_GAME, Material.POTION),
    REAPER_PEPPERS(Group.CONSUMABLES, null, 50, Availability.NOT_IN_GAME, Material.RED_DYE),
    MCGRUBBERS_BURGER(Group.CONSUMABLES, null, 25, Availability.NOT_IN_GAME, Material.COOKED_BEEF),
    WRIGGLING_LARVA(Group.CONSUMABLES, null, 25, Availability.NOT_IN_GAME, Material.PAPER),
    VIAL_OF_VENOM(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.POTION),
    FESTERING_MAGGOT(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.WHITE_DYE),
    REFINED_JYRRE(Group.CONSUMABLES, null, 10, Availability.LIVE, Material.POTION),
    REFINED_DARK_CACAO_TRUFFLE(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.COCOA_BEANS),
    SPOTLITE(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.GLOWSTONE_DUST),
    DWARVEN_OS_ORE_OATS(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.RAW_IRON),
    DWARVEN_OS_BLOCK_BRAN(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.STONE),
    DWARVEN_OS_GEMSTONE_GRAHAMS(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.AMETHYST_SHARD),
    DWARVEN_OS_METALLIC_MINIS(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.IRON_NUGGET),
    MOBY_DUCK(Group.CONSUMABLES, null, 5, Availability.NOT_IN_GAME, Material.BOOK),
    BRAIN_FOOD(Group.CONSUMABLES, null, 25, Availability.NOT_IN_GAME, Material.BREAD),
    FILLED_ROSEWATER_FLASK(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.PAPER),
    VERY_MOLDY_BREAD(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.BREAD),
    FEAST_BURGER_WITH_A_SIDE_OF_DEEPFRIES(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.PAPER),
    MOUND_OF_SEAGRASS(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.SEAGRASS),
    HUNTING_TOOLKIT(Group.CONSUMABLES, null, 2, Availability.NOT_IN_GAME, Material.PAPER),
    FARMING_TOOLKIT(Group.CONSUMABLES, null, 2, Availability.NOT_IN_GAME, Material.BUNDLE),
    ISOPOD_HUSK(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.PAPER),
    BEE_SALIVA(Group.CONSUMABLES, null, 10, Availability.NOT_IN_GAME, Material.PAPER),
    ;

    /**
     * Marks a task with no reachable ceiling. Hypixel shows these with a running total instead
     * of a progress bar and leaves them out of every category total, so they contribute zero
     * here too.
     */
    public static final int UNCAPPED = 0;

    /**
     * Real Hypixel's total obtainable SkyBlock XP, summed from the category totals shown in the
     * live Ways to Level Up menu: 57,874 XP == SkyBlock Level 578.74. Uncapped tasks are
     * excluded, exactly as Hypixel excludes them.
     */
    public static final int TOTAL_MAX_XP;
    /** XP required per SkyBlock level. Real Hypixel grants one level per 100 XP. */
    public static final int XP_PER_LEVEL = 100;

    private static final Map<Group, List<SkyBlockXPSource>> BY_GROUP = new LinkedHashMap<>();

    static {
        int total = 0;
        for (SkyBlockXPSource source : values()) {
            total += source.maxXP;
            BY_GROUP.computeIfAbsent(source.group, _ -> new ArrayList<>()).add(source);
        }
        TOTAL_MAX_XP = total;
    }

    private final Group group;
    private final SubArea subArea;
    private final int maxXP;
    private final Availability availability;
    private final Material icon;

    SkyBlockXPSource(Group group, SubArea subArea, int maxXP, Availability availability, Material icon) {
        this.group = group;
        this.subArea = subArea;
        this.maxXP = maxXP;
        this.availability = availability;
        this.icon = icon;
    }

    public String getDisplayName() {
        return switch (this) {
            // Transcribed from the live menu wherever title-casing the constant gets it wrong.
            case HEART_OF_THE_MOUNTAIN -> "Heart of the Mountain";
            case CORE_OF_THE_MOUNTAIN -> "Core of the Mountain";
            case HEART_OF_THE_FOREST -> "Heart of the Forest";
            case CENTER_OF_THE_FOREST -> "Center of the Forest";
            case YEAR_OF_THE_WITCH -> "Year of the Witch";
            case MOUND_OF_SEAGRASS -> "Mound of Seagrass";
            case FEAST_BURGER_WITH_A_SIDE_OF_DEEPFRIES -> "Feast Burger with a Side of Deepfries";
            case VIAL_OF_VENOM -> "Vial Of Venom";
            case ANITAS_SHOP_UPGRADES -> "Anita's Shop Upgrades";
            case JACOBS_FARMING_CONTEST -> "Jacob's Farming Contest";
            case HOPPITYS_HUNT -> "Hoppity's Hunt";
            case CARROLYNS_EXPORTABLE_CROPS -> "Carrolyn's Exportable Crops";
            case MCGRUBBERS_BURGER -> "McGrubber's Burger";
            case MOBY_DUCK -> "Moby-Duck";
            case DWARVEN_OS_ORE_OATS -> "Dwarven O's Ore Oats";
            case DWARVEN_OS_BLOCK_BRAN -> "Dwarven O's Block Bran";
            case DWARVEN_OS_GEMSTONE_GRAHAMS -> "Dwarven O's Gemstone Grahams";
            case DWARVEN_OS_METALLIC_MINIS -> "Dwarven O's Metallic Minis";
            default -> StringUtility.toNormalCase(name());
        };
    }

    /**
     * Tasks Hypixel draws with a custom head rather than a vanilla item. A null texture means
     * {@link #getIcon()} is used; never give a task a bare {@code PLAYER_HEAD} icon, because an
     * untextured head renders as Steve.
     */
    public @Nullable String getHeadTexture() {
        return switch (this) {
            case ACCESSORY_BAG_UPGRADES -> "3b648b9a44e280bcdf25f4a66a97bd5c33542e5e82415e15b475c6b999b8d635";
            case BRAIN_FOOD -> "867e27a256cdb7b741dbfe8419f5f3d89bdfbd52a1b3f0fdb9c95edeeafc1f3f";
            case CARROLYNS_EXPORTABLE_CROPS -> "2e51db6f6955e5e53e7df3086838796679770f9470c792ae1ba9e7f45762b987";
            case COMMUNITY_SHOP_UPGRADES -> "9017b6acf22deb1686293d7cccffe40901aa5aa4a6fa90f8e1217114e1d37302";
            case CRIMSON_ESSENCE_SHOP -> "67c41930f8ff0f2b0430e169ae5f38e984df1244215705c6f173862844543e9d";
            case DIAMOND_ESSENCE_SHOP -> "964f25cfff754f287a9838d8efe03998073c22df7a9d3025c425e3ed7ff52c20";
            case DRAGON_ESSENCE_SHOP -> "33ff416aa8bec1665b92701fbe68a4effff3d06ed9147454fa77712dd6079b33";
            case DWARVEN_OS_BLOCK_BRAN -> "4f6bddba1e3d014fc75e70e27223ca30cbfa42990d448d9a940e500a6435bec";
            case DWARVEN_OS_GEMSTONE_GRAHAMS -> "566c50f1aa917239b179dcc03dfb1ed6b5d2844eb8c8fa779f1223d6cbba5063";
            case DWARVEN_OS_METALLIC_MINIS -> "96663b544ae8f8a4ce4cb0cbb6f9190efe5cd9996047520705c276695ad1734";
            case DWARVEN_OS_ORE_OATS -> "fda793b4095892b01d954ca10db0d6e1d366d6b4e7612dedc273dfa579ecb227";
            case FARMING_TOOLKIT -> "7418c74e0d7d56bdaf5fb5018a738d4300b8b33a0ea8875fb876123943089f5d";
            case FESTERING_MAGGOT -> "e828af3fa80154ccee135c64cd57412feda895cd1d21f240d876773ba9d6a56d";
            case FOREST_ESSENCE_SHOP -> "e019ece125ec79f95178e1af4da2a8b982de71ed42c31cac4b1d2f665355df5a";
            case FOSSIL_ESSENCE_SHOP -> "93a1b830399ab432a5178fdaf3939b24bf25c724a66be947296c503352bc380d";
            case GOLD_ESSENCE_SHOP -> "8816606260779b23ed15f87c56c932240db745f86f683d1f4deb83a4a125fa7b";
            case HOOTIE_UPGRADES -> "da3216da54e7368fb40b721239ad95e07ef4f97d93f1c42ff319bab9a53882af";
            case HOPPITYS_HUNT -> "b79e7f3341b672d9de6564cbaca052a6a723ea466a2e66af35ba1ba855f0d692";
            case ICE_ESSENCE_SHOP -> "ddba642efffa13ec3730eafc5914ab68115c1f998803f74452e2e0cd26af0b8";
            case MCGRUBBERS_BURGER -> "d33ddb92cb6b3a79280b8bdced8976aeab13a4bffeaef2d46d828bd91dee0f3e";
            case METAPHYSICAL_SERUM -> "1dbf5e10d0837f20dd7ae691aa6b5c508e4ce11eb1c5aa7b0acc71a554fcbd81";
            case MOUND_OF_SEAGRASS -> "8902f1c55838121a837361d35e3c054a975ee606e197c2415f91beff888fad1c";
            case MYTHOLOGICAL_RITUAL_PERK_SHOP -> "4c27e3cb52a64968e60c861ef1ab84e0a0cb5f07be103ac78da67761731f00c8";
            case REAPER_PEPPERS -> "b5d617b4ce45705240bd48f5ba50e25ca8375290a212acc5f3039c2ed8178606";
            case REFINED_DARK_CACAO_TRUFFLE -> "4f991215e258d65f77eedeb69356f5521bb24cb9dd47765e38f52a892d7989ad";
            case SAFARI_ESSENCE_SHOP -> "2d9eb19536a482fdffba3dcd89dcbbd5daa94180d69e156d052061e376373aea";
            case SPIDER_ESSENCE_SHOP -> "16617131250e578333a441fdf4a5b8c62163640a9d06cd67db89031d03accf6";
            case SPOTLITE -> "5f4c57e744a0fe395fb145e7496a85ccc41277395433b98b73c7f29bf54dcc35";
            case TAMING_CAP_INCREASES -> "99bb13737979f98ba236e37ae48fd5dddedba719ec62068e8ac962abf7e07d83";
            case THE_DOJO -> "c3b0d9622df69159c20706e9af400d6bb308ebcb7520025f57d977ad034f6b5a";
            case UNDEAD_ESSENCE_SHOP -> "71d7c816fc8c636d7f50a93a0ba7aaeff06c96a561645e9eb1bef391655c531";
            case VIAL_OF_VENOM -> "778067c034da7bfe4988e0bf515b0499f8ac2c992fd7fd0606fa02259f5a8c6f";
            case WITHER_ESSENCE_SHOP -> "c4db4adfa9bf48ff5d41707ae34ea78bd2371659fcd8cd8934749af4cce9b";
            default -> null;
        };
    }

    /** Whether this server can currently award this source's XP through normal gameplay. */
    public boolean isObtainable() {
        return availability == Availability.LIVE;
    }

    /**
     * Whether XP for this source is tracked as free-form per-source progress rather than
     * through the existing {@link SkyBlockLevelCause} registry. Sources with a dedicated cause
     * class are read from those causes instead, so counting them both ways would inflate a
     * player's total. Independent of {@link #isObtainable()}: a source can be awarded through
     * per-source progress and still be fully obtainable.
     */
    public boolean usesSourceProgress() {
        return getBackingCauseType() == null;
    }

    /**
     * For {@link Availability#LIVE} sources, the existing {@link SkyBlockLevelCause} class that
     * already records this source's XP. Those are summed from completed causes rather than
     * stored as per-source progress, so a player is never credited twice for the same thing.
     */
    public Class<? extends SkyBlockLevelCauseAbstr> getBackingCauseType() {
        return switch (this) {
            case SKILL_LEVEL_UP -> SkillLevelCause.class;
            case COLLECTIONS -> CollectionLevelCause.class;
            case MUSEUM_DONATIONS -> MuseumLevelCause.class;
            case FAIRY_SOULS -> FairySoulExchangeLevelCause.class;
            case ACCESSORY_BAG -> NewAccessoryLevelCause.class;
            case COLLECT_MOB_TYPES -> CollectedMobTypesCause.class;
            case COMPLETE_OBJECTIVES -> MissionLevelCause.class;
            default -> null;
        };
    }


    /**
     * The tooltip body Hypixel shows for this task. Entries below were transcribed from
     * screenshots of the live menu; the rest fall back to a generic line and are worth
     * replacing as their real wording is sighted.
     */
    public List<String> getTaskDescription() {
        return switch (this) {
            case SKILL_LEVEL_UP -> List.of(
                    "Gain XP by leveling up your Skills!",
                    "Level 1-10: <b>+5 XP",
                    "Level 11-25: <b>+10 XP",
                    "Level 26-50: <b>+20 XP",
                    "Level 51-60: <b>+30 XP");
            case COLLECTIONS -> List.of(
                    "Gain XP by progressing your",
                    "Collections!",
                    "Per Collection Milestone: <b>+4 XP");
            case ACCESSORY_BAG -> List.of(
                    "Gain <b>+1 XP <7>per Accessory Power you",
                    "have in your Accessory Bag!");
            case PET_SCORE -> List.of(
                    "Gain <b>+3 XP <7>per Pet Score from",
                    "collecting unique pets!");
            case SLAYER_LEVEL_UP -> List.of(
                    "Gain XP by leveling up your Slayer",
                    "Boss Progress!",
                    "Level 1: <b>+15 XP", "Level 2: <b>+25 XP", "Level 3: <b>+35 XP",
                    "Level 4: <b>+50 XP", "Level 5: <b>+75 XP", "Level 6: <b>+100 XP",
                    "Level 7: <b>+125 XP", "Level 8: <b>+150 XP", "Level 9: <b>+150 XP");
            case BOSS_COLLECTIONS -> List.of(
                    "Gain XP by completing your Boss",
                    "Collections!");
            case BESTIARY_PROGRESS -> List.of(
                    "Gain XP by progressing in your",
                    "Bestiary!",
                    "Each Family Tier: <b>+1 XP",
                    "Every 10 Milestones: <b>+10 XP");
            case MYTHOLOGICAL_KILLS -> List.of(
                    "Gain XP by killing Mythological",
                    "Creatures and Minos!",
                    "Per 100 Kills: <b>+1 XP",
                    "<8>(Caps at 10,000 kills!)");
            case FISHING_FESTIVAL -> List.of(
                    "Gain XP by killing Sharks",
                    "during the <b>Fishing Festival<7>",
                    "event!",
                    "Per 50 Sharks Killed: <b>+1 XP",
                    "<8>(Caps at 5,000 Sharks killed!)");
            case COLLECT_MOB_TYPES -> List.of(
                    "Gain XP by collecting all of the Mob",
                    "Types!",
                    "Each unique Mob Type collected will",
                    "grant you <b>+1 XP<7>.",
                    "Collecting all Mob Types will grant an",
                    "additional <b>+10 XP<7>.");
            case FISHING_FESTIVAL_PERK_SHOP, MINING_FIESTA_PERK_SHOP, MYTHOLOGICAL_RITUAL_PERK_SHOP,
                 SPOOKY_FESTIVAL_PERK_SHOP, SEASON_OF_JERRY_PERK_SHOP, HARVEST_FEAST_PERK_SHOP -> List.of(
                    "Gain XP by purchasing perk levels from",
                    getDisplayName() + "!",
                    "Tier I: <b>+2 XP", "Tier II: <b>+2 XP", "Tier III: <b>+3 XP",
                    "Tier IV: <b>+5 XP", "Tier V: <b>+7 XP");
            case THE_DOJO -> List.of(
                    "Gain XP by obtaining Dojo Belts!",
                    "White Belt: <b>+20 XP", "Yellow Belt: <b>+30 XP", "Green Belt: <b>+50 XP",
                    "Blue Belt: <b>+75 XP", "Brown Belt: <b>+100 XP", "Black Belt: <b>+150 XP");
            case MAGE_REPUTATION, BARBARIAN_REPUTATION -> List.of(
                    "Gain XP by earning Reputation with",
                    "the " + (this == MAGE_REPUTATION ? "Mage" : "Barbarian") + " Faction!",
                    "Friendly Status: <b>+5 XP", "Trusted Status: <b>+10 XP",
                    "Honored Status: <b>+20 XP", "Hero Status: <b>+40 XP");
            default -> List.of("Gain XP from " + getDisplayName() + "!");
        };
    }

    /**
     * The sub-tasks Hypixel lists inside a composite task's tooltip. A task with sub-tasks is
     * headed "N Tasks" rather than "XP Task".
     */
    public List<String> getSubTasks() {
        return switch (this) {
            case SLAY_DRAGONS -> List.of(
                    "Slay Superior Dragon", "Slay Old Dragon", "Slay Young Dragon",
                    "Slay Wise Dragon", "Slay Protector Dragon", "Slay Unstable Dragon",
                    "Slay Strong Dragon");
            case DEFEAT_SLAYERS -> List.of(
                    "Defeat Revenant Horror", "Defeat Tarantula Broodfather",
                    "Defeat Sven Packmaster", "Defeat Voidgloom Seraph",
                    "Defeat Riftstalker Bloodfiend", "Defeat Inferno Demonlord");
            case DEFEAT_KUUDRA -> List.of(
                    "Defeat Kuudra Basic Tier", "Defeat Kuudra Hot Tier",
                    "Defeat Kuudra Burning Tier", "Defeat Kuudra Fiery Tier",
                    "Defeat Kuudra Infernal Tier");
            case DEFEAT_ARACHNE -> List.of(
                    "Defeat Arachne Tier 1", "Defeat Arachne Tier 2");
            default -> List.of();
        };
    }

    /**
     * The last line of a task's tooltip. Hypixel colours it yellow when clicking opens a menu
     * and red when it is only telling you where to go.
     */
    public TaskAction getAction() {
        return switch (this) {
            case SKILL_LEVEL_UP -> TaskAction.clickable("Click to see your Skills!");
            case COLLECTIONS -> TaskAction.clickable("Click to see Collections!");
            case BOSS_COLLECTIONS -> TaskAction.clickable("Click to see Boss Collections!");
            case BESTIARY_PROGRESS -> TaskAction.clickable("Click to see Bestiary!");
            case COLLECT_MOB_TYPES -> TaskAction.clickable("Click to view your progress!");
            case SLAY_DRAGONS, DEFEAT_SLAYERS, DEFEAT_KUUDRA, DEFEAT_ARACHNE ->
                    TaskAction.clickable("Click to view tasks!");
            case SLAYER_LEVEL_UP -> TaskAction.hint(
                    "Visit Maddox the Slayer in the Hub to", "view Slayer Levels.");
            case MYTHOLOGICAL_KILLS -> TaskAction.hint("Kill Mythological Creatures!");
            case FISHING_FESTIVAL -> TaskAction.hint("Go Kill some sharks!");
            default -> TaskAction.hint("Go earn some XP!");
        };
    }

    public record TaskAction(List<String> lines, boolean clickable) {
        static TaskAction clickable(String line) {
            return new TaskAction(List.of(line), true);
        }

        static TaskAction hint(String... lines) {
            return new TaskAction(List.of(lines), false);
        }
    }

    /**
     * Hypixel shows a progress bar for tasks with a reachable ceiling and a running total for
     * tasks that simply keep accruing, and leaves the latter out of its category totals.
     */
    public boolean isUncapped() {
        return maxXP == UNCAPPED;
    }

    public static List<SkyBlockXPSource> getByGroup(Group group) {
        return BY_GROUP.getOrDefault(group, List.of());
    }

    public static List<SkyBlockXPSource> getBySubArea(SubArea subArea) {
        return Arrays.stream(values()).filter(source -> source.subArea == subArea).toList();
    }

    public static int getTotalXPFor(Group group) {
        return getByGroup(group).stream().mapToInt(SkyBlockXPSource::getMaxXP).sum();
    }

    public static int getTotalXPFor(SubArea subArea) {
        return getBySubArea(subArea).stream().mapToInt(SkyBlockXPSource::getMaxXP).sum();
    }

    /** The highest SkyBlock Level reachable if every source is completed, e.g. {@code 620.19}. */
    public static double getMaxLevel() {
        return (double) TOTAL_MAX_XP / XP_PER_LEVEL;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Getter
    public enum Group {
        CORE("Core Tasks", Material.NETHER_STAR, Material.GRAY_STAINED_GLASS_PANE,
                "All of the core XP tasks that are", "integral to your general progression", "in SkyBlock!"),
        EVENT("Event Tasks", Material.CLOCK, Material.YELLOW_STAINED_GLASS_PANE,
                "All of the XP tasks related to", "different events around SkyBlock!"),
        DUNGEON("Dungeon Tasks", Material.DEEPSLATE_BRICKS, Material.RED_STAINED_GLASS_PANE,
                "All Dungeon related XP Tasks."),
        ESSENCE_SHOP("Essence Shop Tasks", Material.INK_SAC, Material.GRAY_STAINED_GLASS_PANE,
                "All XP Tasks that have to do with the", "various Essence Shops in SkyBlock!"),
        SLAYING("Slaying Tasks", Material.GOLDEN_SWORD, Material.ORANGE_STAINED_GLASS_PANE,
                "All XP Tasks that require you to do", "some killing!"),
        SKILL_RELATED("Skill Related Tasks", Material.DIAMOND_SWORD, Material.GRAY_STAINED_GLASS_PANE,
                "All XP Tasks that are related to a", "player-specific skill!"),
        MISCELLANEOUS("Miscellaneous Tasks", Material.GOLD_INGOT, Material.LIME_STAINED_GLASS_PANE,
                "A random assortment of XP tasks", "that do not fit other categories!"),
        STORY("Story Tasks", Material.BOOK, Material.PURPLE_STAINED_GLASS_PANE,
                "All XP Tasks that are directly", "related with progressing through", "SkyBlock's story!"),
        CONSUMABLES("Consumables", Material.APPLE, Material.PURPLE_STAINED_GLASS_PANE,
                "All XP Tasks that are related to", "consuming items!"),
        ;

        private final String displayName;
        private final Material icon;
        private final Material filler;
        private final List<String> description;

        Group(String displayName, Material icon, Material filler, String... description) {
            this.displayName = displayName;
            this.icon = icon;
            this.filler = filler;
            this.description = List.of(description);
        }

        /** The name Hypixel puts after "Tasks -> " in the category screen's title. */
        public String getShortName() {
            return displayName.endsWith(" Tasks")
                    ? displayName.substring(0, displayName.length() - " Tasks".length())
                    : displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Getter
    public enum SubArea {
        MINING("Mining", Material.IRON_PICKAXE, Material.GRAY_STAINED_GLASS_PANE,
                "All Mining related XP Tasks."),
        FARMING("Farming", Material.GOLDEN_HOE, Material.YELLOW_STAINED_GLASS_PANE,
                "All Farming related XP Tasks."),
        FISHING("Fishing", Material.FISHING_ROD, Material.GRAY_STAINED_GLASS_PANE,
                "All Fishing related XP Tasks."),
        FORAGING("Foraging", Material.BAMBOO, Material.LIME_STAINED_GLASS_PANE,
                "All Foraging related XP Tasks."),
        ;

        private final String displayName;
        private final Material icon;
        private final Material filler;
        private final List<String> description;

        SubArea(String displayName, Material icon, Material filler, String... description) {
            this.displayName = displayName;
            this.icon = icon;
            this.filler = filler;
            this.description = List.of(description);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /** How far along this server is at actually granting a source's XP. */
    @Getter
    public enum Availability {
        /** Earned through normal play right now. */
        LIVE("<a>", "Obtainable"),
        /**
         * The underlying system exists on this server but nothing awards SkyBlock XP for it
         * yet. These are the one-line wiring jobs: call
         * {@code getSkyBlockExperience().addSourceXP(source, amount)} where the system
         * already reports progress.
         */
        UNWIRED("<e>", "Not yet awarding XP"),
        /** The content itself does not exist on this server yet. */
        NOT_IN_GAME("<c>", "Not in the game yet"),
        ;

        private final String colour;
        private final String displayName;

        Availability(String colour, String displayName) {
            this.colour = colour;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
