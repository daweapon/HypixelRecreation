package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.always;
import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.book;
import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.chance;
import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.cyclamen;
import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.oneOf;
import static net.swofty.type.skyblockgeneric.crimson.mob.MobDrop.pet;

/**
 * Every Crimson Isle mob, miniboss and summon (wiki mob pages after the July 2026 level rework). Bestiary
 * heads come from NEU's constants/bestiary.json. Defense is 0 unless the wiki publishes it (only the Krondor
 * Necromancer Summon does). Speeds are estimates.
 */
public final class CrimsonMobs {
    private static final String BLAZE_HEAD = "b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0";
    private static final String MAGMA_CUBE_HEAD = "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429";
    private static final String RIDER_HEAD = "74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb";
    private static final String GHAST_HEAD = "de8a38e9afbd3da10d19b577c55c7bfd6b4f2e407e44d4017b23be9167abff02";
    private static final String SPECTRE_HEAD = "7f5f3384384d7f03bf97a73099b0bafb72c538fc0415ac8613b664f87579a379";
    private static final MobDrop RAMPART = oneOf(1, "RAMPART_HELMET", "RAMPART_CHESTPLATE", "RAMPART_LEGGINGS", "RAMPART_BOOTS");
    private static final MobDrop SWORD_OF_BAD_HEALTH = chance("SWORD_OF_BAD_HEALTH", 0.05);

    private CrimsonMobs() {
    }

    public static final CrimsonMobProfile BLAZE = CrimsonMobProfile.builder("BLAZE", "Blaze")
            .level(140).entity(EntityType.BLAZE).stats(250_000, 2_000).rewards(210, 46, 154)
            .types(MobType.INFERNAL).bestiary(4, 15, BLAZE_HEAD)
            .drops(always("BLAZE_ROD", 1), chance("BLAZE_ROD", 50), chance("BLAZE_ASHES", 20), RAMPART,
                    SWORD_OF_BAD_HEALTH, cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile MUTATED_BLAZE = CrimsonMobProfile.builder("MUTATED_BLAZE", "Mutated Blaze")
            .level(163).entity(EntityType.BLAZE).stats(500_000, 2_500).rewards(244, 54, 179)
            .types(MobType.INFERNAL).bestiary(4, 15, BLAZE_HEAD)
            .drops(always("BLAZE_ROD", 2, 3), chance("MUTATED_BLAZE_ASHES", 20), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile BEZAL = CrimsonMobProfile.builder("BEZAL", "Bezal")
            .level(144).entity(EntityType.BLAZE).stats(300_000, 2_000).rewards(216, 48, 158)
            .types(MobType.INFERNAL).bestiary(4, 15, BLAZE_HEAD)
            .drops(always("BLAZE_ROD", 1), chance("BLAZE_ROD", 50), chance("BEZOS", 10), RAMPART,
                    SWORD_OF_BAD_HEALTH, cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile WITHER_SKELETON = CrimsonMobProfile.builder("WITHER_SKELETON", "Wither Skeleton")
            .level(144).entity(EntityType.WITHER_SKELETON).stats(300_000, 2_000).rewards(216, 48, 158).speed(130)
            .types(MobType.WITHER, MobType.SKELETAL).bestiary(5, 20, "7953b6c68448e7e6b6bf8fb273d7203acd8e1be19e81481ead51f45de59a8")
            .drops(always("BONE", 3), chance("COAL", 50), chance("WITHER_SOUL", 20), chance("ENCHANTED_COAL", 1),
                    chance("ENCHANTED_CHARCOAL", 1), RAMPART, SWORD_OF_BAD_HEALTH,
                    chance("DYE_BONE", 100D / 3_000_000), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile WITHER_SPECTRE = CrimsonMobProfile.builder("WITHER_SPECTRE", "Wither Spectre")
            .level(144).entity(EntityType.WITHER_SKELETON).stats(300_000, 2_000).rewards(216, 48, 158).speed(150)
            .types(MobType.WITHER, MobType.ARCANE).bestiary(4, 20, SPECTRE_HEAD)
            .drops(always("GUNPOWDER", 1), chance("REDSTONE", 20), chance("SPECTRE_DUST", 20), RAMPART,
                    SWORD_OF_BAD_HEALTH, chance("DYE_BONE", 100D / 3_000_000), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile SMOLDERING_BLAZE = CrimsonMobProfile.builder("SMOLDERING_BLAZE", "Smoldering Blaze")
            .level(201).entity(EntityType.BLAZE).stats(2_000_000, 2_500).rewards(301, 67, 221)
            .types(MobType.INFERNAL, MobType.SUBTERRANEAN).bestiary(3, 20, BLAZE_HEAD)
            .drops(always("BLAZE_ROD", 2, 3), chance("BLAZE_ASHES", 20), book("SMOLDERING", 1, 0.016),
                    chance("NETHERRACK_LOOKING_SUNSHADE", 0.1), cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile MILLENNIA_AGED_BLAZE = CrimsonMobProfile.builder("MILLENNIA_AGED_BLAZE", "Millennia-Aged Blaze")
            .level(290).entity(EntityType.BLAZE).stats(12_000_000, 6_000).rewards(435, 96, 319)
            .types(MobType.INFERNAL, MobType.SUBTERRANEAN).bestiary(4, 15, BLAZE_HEAD)
            .drops(always("BLAZE_ROD", 32), chance("BLAZE_ASHES", 20), chance("MILLENIA_OLD_BLAZE_ASHES", 4.5),
                    chance("NETHERRACK_LOOKING_SUNSHADE", 1), book("SMOLDERING", 1, 0.016), cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile FLAMING_SPIDER = CrimsonMobProfile.builder("FLAMING_SPIDER", "Flaming Spider")
            .level(140).entity(EntityType.SPIDER).stats(500_000, 1_000).rewards(210, 46, 154).speed(140)
            .types(MobType.ARTHROPOD, MobType.INFERNAL).bestiary(3, 20, "5a65ef23ea35047150d33880473a47fe635f0c0e3c823bd76dc849cb02441655")
            .drops(always("STRING", 1), chance("STRING", 50), chance("BURNING_EYE", 20), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile MAGMA_CUBE_RIDER = CrimsonMobProfile.builder("MAGMA_CUBE_RIDER", "Magma Cube Rider")
            .level(160).entity(EntityType.MAGMA_CUBE).stats(750_000, 1_500).rewards(240, 53, 176)
            .types(MobType.UNDEAD, MobType.CUBIC, MobType.INFERNAL).bestiary(4, 20, RIDER_HEAD)
            .drops(always("ROTTEN_FLESH", 1), chance("ROTTEN_FLESH", 50), chance("MAGMA_CHUNK", 25), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile MAGMA_CUBE = CrimsonMobProfile.builder("MAGMA_CUBE", "Magma Cube")
            .level(151).entity(EntityType.MAGMA_CUBE).stats(400_000, 2_000).rewards(226, 50, 166)
            .types(MobType.CUBIC, MobType.INFERNAL).bestiary(4, 20, MAGMA_CUBE_HEAD)
            .drops(always("MAGMA_CREAM", 1, 3), chance("MAGMA_CHUNK", 20), pet("MAGMA_CUBE_PET", Rarity.COMMON, 1),
                    pet("MAGMA_CUBE_PET", Rarity.UNCOMMON, 0.05), chance("COMBAT_EXP_BOOST", 0.1), RAMPART,
                    pet("MAGMA_CUBE_PET", Rarity.RARE, 0.01), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile PACK_MAGMA_CUBE = CrimsonMobProfile.builder("PACK_MAGMA_CUBE", "Pack Magma Cube")
            .level(170).entity(EntityType.MAGMA_CUBE).stats(600_000, 2_750).rewards(255, 56, 187)
            .types(MobType.CUBIC, MobType.INFERNAL).bestiary(4, 20, MAGMA_CUBE_HEAD)
            .drops(always("MAGMA_CREAM", 1), chance("MAGMA_CREAM", 80), chance("MAGMA_CREAM", 50), chance("MAGMAG", 10))
            .build();

    public static final CrimsonMobProfile KADA_KNIGHT = CrimsonMobProfile.builder("KADA_KNIGHT", "Kada Knight")
            .level(178).entity(EntityType.ZOMBIE).stats(800_000, 2_750).rewards(267, 59, 195).speed(160)
            .types(MobType.ANIMAL, MobType.UNDEAD).bestiary(4, 20, RIDER_HEAD)
            .drops(always("ROTTEN_FLESH", 1), chance("ROTTEN_FLESH", 50), chance("FEATHER", 40), chance("KADA_LEAD", 20),
                    cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile GHAST = CrimsonMobProfile.builder("GHAST", "Ghast")
            .level(140).entity(EntityType.GHAST).stats(100_000, 5_000).rewards(210, 46, 154)
            .types(MobType.CUBIC, MobType.AIRBORNE).bestiary(5, 20, GHAST_HEAD)
            .drops(always("GHAST_TEAR", 1), chance("GHAST_TEAR", 50), chance("TENTACLE_MEAT", 10), cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile DIVE_GHAST = CrimsonMobProfile.builder("DIVE_GHAST", "Dive Ghast")
            .level(157).entity(EntityType.GHAST).stats(100_000, 10_000).rewards(235, 52, 172)
            .types(MobType.CUBIC, MobType.AIRBORNE).bestiary(5, 20, GHAST_HEAD)
            .drops(always("GHAST_TEAR", 2), chance("TENTACLE_MEAT", 10), cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile MUSHROOM_BULL = CrimsonMobProfile.builder("MUSHROOM_BULL", "Mushroom Bull")
            .level(167).entity(EntityType.MOOSHROOM).stats(1_000_000, 1_500).rewards(250, 55, 183).speed(220)
            .types(MobType.ANIMAL).bestiary(4, 20, "a163bc416b8e6058f92b231e9a524b7fe118eb6e7eeab4ad16d1b52a3ec04fcd")
            .drops(always("RED_MUSHROOM", 4), always("LEATHER", 1), always("RAW_BEEF", 1), chance("DIGESTED_MUSHROOMS", 20),
                    cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile FLARE = CrimsonMobProfile.builder("FLARE", "Flare")
            .level(240).entity(EntityType.BLAZE).stats(5_000_000, 3_500).rewards(360, 80, 264).speed(0)
            .types(MobType.INFERNAL).bestiary(3, 20, BLAZE_HEAD)
            .drops(always("BLAZE_ROD", 2, 3), chance("GAZING_PEARL", 5), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile HELLWISP = CrimsonMobProfile.builder("HELLWISP", "Hellwisp")
            .level(181).entity(EntityType.GHAST).stats(2_500_000, 1_000).rewards(271, 60, 199).speed(0)
            .types(MobType.CUBIC, MobType.INFERNAL, MobType.SUBTERRANEAN)
            .bestiary(6, 15, "199c3470ba4317913717ab36b626aec0b563266e28b3a517d71e9a0af0c90f96")
            .drops(chance("TENTACLE_MEAT", 15), cyclamen(0.0004))
            .build();

    public static final CrimsonMobProfile MATCHO = CrimsonMobProfile.builder("MATCHO", "Matcho")
            .level(203).entity(EntityType.MANNEQUIN).stats(750_000, 7_000).rewards(304, 67, 223).speed(180)
            .types(MobType.HUMANOID, MobType.INFERNAL)
            .bestiary(6, 15, "ef2daabb78a1f7aa12d145d88c0ca46b9e856f5534e9286e555faf0c291f4fd5")
            .drops(always("MATCH_STICKS", 2), chance("MATCH_STICKS", 4), chance("SULPHUR", 50), cyclamen(0.0004))
            .build();

    public static final CrimsonMobProfile CINDERBAT = CrimsonMobProfile.builder("CINDERBAT", "Cinderbat")
            .level(264).entity(EntityType.BAT).stats(5_000_000, 7_000).rewards(396, 88, 290).speed(0)
            .types(MobType.ANIMAL, MobType.INFERNAL, MobType.AIRBORNE)
            .drops(chance("FLAMES", 1), cyclamen(0.0004))
            .build();

    public static final CrimsonMobProfile BARBARIAN = CrimsonMobProfile.builder("BARBARIAN", "Barbarian")
            .level(211).entity(EntityType.PIGLIN_BRUTE).stats(2_000_000, 3_500).rewards(316, 70, 232).speed(130)
            .types(MobType.HUMANOID, MobType.ANIMAL).faction(CrimsonFaction.BARBARIAN, 5)
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile GOLIATH_BARBARIAN = CrimsonMobProfile.builder("GOLIATH_BARBARIAN", "Goliath Barbarian")
            .level(245).entity(EntityType.PIGLIN_BRUTE).stats(5_000_000, 4_000).rewards(367, 81, 269).speed(120)
            .types(MobType.HUMANOID, MobType.ANIMAL).faction(CrimsonFaction.BARBARIAN, 25)
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile FIRE_MAGE = CrimsonMobProfile.builder("FIRE_MAGE", "Fire Mage")
            .level(207).entity(EntityType.EVOKER).stats(2_000_000, 3_000).rewards(310, 69, 227).speed(120)
            .types(MobType.HUMANOID, MobType.ARCANE, MobType.INFERNAL).faction(CrimsonFaction.MAGE, 5)
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile KRONDOR_NECROMANCER = CrimsonMobProfile.builder("KRONDOR_NECROMANCER", "Krondor Necromancer")
            .level(245).entity(EntityType.EVOKER).stats(5_000_000, 4_000).rewards(367, 81, 269).speed(140)
            .types(MobType.HUMANOID, MobType.ARCANE, MobType.INFERNAL).faction(CrimsonFaction.MAGE, 25)
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile KRONDOR_NECROMANCER_SUMMON = CrimsonMobProfile.builder("KRONDOR_NECROMANCER_SUMMON", "Krondor Necromancer Summon")
            .level(222).entity(EntityType.ZOMBIE).stats(1_000_000, 10_000).defense(500).rewards(333, 74, 244).speed(220)
            .types(MobType.ARCANE, MobType.UNDEAD).faction(CrimsonFaction.MAGE, 25)
            .build();

    public static final CrimsonMobProfile BARBARIAN_GUARD = CrimsonMobProfile.builder("BARBARIAN_GUARD", "Barbarian Guard")
            .level(398).entity(EntityType.PIGLIN_BRUTE).stats(50_000_000, 20_000).rewards(597, 132, 437).speed(160)
            .types(MobType.HUMANOID).neverCorrupted()
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile MAGE_GUARD = CrimsonMobProfile.builder("MAGE_GUARD", "Mage Guard")
            .level(398).entity(EntityType.VINDICATOR).stats(50_000_000, 20_000).rewards(597, 132, 437).speed(160)
            .types(MobType.HUMANOID).neverCorrupted()
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile EXE = atom("EXE", "Exe");
    public static final CrimsonMobProfile WAI = atom("WAI", "Wai");
    public static final CrimsonMobProfile ZEE = atom("ZEE", "Zee");

    public static final CrimsonMobProfile CHICKZILLA = CrimsonMobProfile.builder("CHICKZILLA", "Chickzilla")
            .level(375).entity(EntityType.CHICKEN).stats(50_000_000, 12_000).rewards(562, 125, 412).speed(150)
            .types(MobType.ANIMAL, MobType.ARCANE)
            .drops(chance("SULPHUR", 50))
            .build();

    public static final CrimsonMobProfile INTRO_BLAZE = CrimsonMobProfile.builder("INTRO_BLAZE", "Blaze")
            .level(44).entity(EntityType.BLAZE).stats(100_000, 20).rewards(66, 14, 48).speed(0)
            .types(MobType.INFERNAL).neverCorrupted()
            .drops(always("BLAZE_ROD", 1), cyclamen(0.00001))
            .build();

    public static final CrimsonMobProfile VANQUISHER = CrimsonMobProfile.builder("VANQUISHER", "Vanquisher")
            .level(235).entity(EntityType.WITHER).stats(5_000_000, 3_000).rewards(352, 78, 258).speed(140)
            .types(MobType.WITHER, MobType.ELUSIVE).neverCorrupted()
            .bestiary(6, 15, "3f86b3affbf65a91faf084a0cd70e40455130ea99b2b3844d2510e1c8b3a5746")
            .drops(always("NETHER_STAR", 3).noMagicFind(), cyclamen(0.0004))
            .build();

    // Minibosses and the Magma Boss. Their loot (rolled per player who dealt 250,000+ damage) lives in CrimsonMinibossLoot.
    public static final CrimsonMobProfile BLADESOUL = CrimsonMobProfile.builder("BLADESOUL", "Bladesoul")
            .level(477).entity(EntityType.WITHER_SKELETON).stats(50_000_000, 3_000).rewards(3_577, 1_192, 1_431).speed(160)
            .types(MobType.WITHER, MobType.SKELETAL, MobType.ARCANE).neverCorrupted()
            .bestiary(7, 15, "7f5f3384384d7f03bf97a73099b0bafb72c538fc0415ac8613b664f87579a379")
            .build();

    public static final CrimsonMobProfile MAGE_OUTLAW = CrimsonMobProfile.builder("MAGE_OUTLAW", "Mage Outlaw")
            .level(515).entity(EntityType.MANNEQUIN).stats(70_000_000, 4_000).rewards(3_862, 1_287, 1_545).speed(0)
            .types(MobType.HUMANOID, MobType.ARCANE).neverCorrupted()
            .bestiary(7, 15, "ebec99b3a050fd277528407676366e20b92006ad88d14727a4d99eb8cb273b62")
            .build();

    public static final CrimsonMobProfile BARBARIAN_DUKE_X = CrimsonMobProfile.builder("BARBARIAN_DUKE_X", "Barbarian Duke X")
            .level(486).entity(EntityType.MANNEQUIN).stats(70_000_000, 4_500).rewards(3_645, 1_215, 1_458).speed(170)
            .types(MobType.HUMANOID, MobType.ANIMAL).neverCorrupted()
            .bestiary(7, 15, "fee9fc7c181e2f630f6b1af85d4951359cafcfa82ff5ea3bc283e0e6088c6e54")
            .build();

    public static final CrimsonMobProfile ASHFANG = CrimsonMobProfile.builder("ASHFANG", "Ashfang")
            .level(508).entity(EntityType.BLAZE).stats(50_000_000, 5_000).rewards(3_810, 1_270, 1_524).speed(0)
            .types(MobType.INFERNAL, MobType.ARCANE).neverCorrupted()
            .bestiary(7, 15, "3d2a5b4b109bd788edba0171d0aab8a55305ac2f56184df70a319cd488a36c3e")
            .build();

    public static final CrimsonMobProfile MAGMA_BOSS = CrimsonMobProfile.builder("MAGMA_BOSS", "Magma Boss")
            .level(523).entity(EntityType.MAGMA_CUBE).stats(160_000_000, 2_000).rewards(3_922, 1_307, 1_569).speed(0)
            .types(MobType.CUBIC, MobType.INFERNAL).neverCorrupted()
            .bestiary(7, 15, MAGMA_CUBE_HEAD)
            .build();

    public static final CrimsonMobProfile WITHER_GUARD = CrimsonMobProfile.builder("BLADESOUL_WITHER_GUARD", "Wither Guard")
            .level(300).entity(EntityType.WITHER_SKELETON).stats(5_000_000, 4_000).speed(260)
            .types(MobType.WITHER, MobType.SKELETAL).neverCorrupted()
            .build();

    public static final CrimsonMobProfile SUMMONED_SKULL = CrimsonMobProfile.builder("SUMMONED_SKULL", "Summoned Skull")
            .level(150).entity(EntityType.VEX).stats(200_000, 2_000).speed(0)
            .types(MobType.ARCANE).neverCorrupted()
            .build();

    public static final CrimsonMobProfile EMPOWERED_SKULL = CrimsonMobProfile.builder("EMPOWERED_SUMMONED_SKULL", "Summoned Skull")
            .level(200).entity(EntityType.VEX).stats(500_000, 2_000).speed(0)
            .types(MobType.ARCANE).neverCorrupted()
            .build();

    public static final CrimsonMobProfile ASHFANG_FOLLOWER = ashfangMinion("ASHFANG_FOLLOWER", "Ashfang Follower", 2_000);
    public static final CrimsonMobProfile ASHFANG_ACOLYTE = ashfangMinion("ASHFANG_ACOLYTE", "Ashfang Acolyte", 1_000);
    public static final CrimsonMobProfile ASHFANG_UNDERLING = ashfangMinion("ASHFANG_UNDERLING", "Ashfang Underling", 1_000);

    public static final CrimsonMobProfile MAGMA_GLARE = CrimsonMobProfile.builder("MAGMA_GLARE", "Magma Glare")
            .level(195).entity(EntityType.MAGMA_CUBE).stats(2_000_000, 2_000).rewards(292, 65, 214).speed(60)
            .types(MobType.CUBIC, MobType.INFERNAL).neverCorrupted()
            .drops(cyclamen(0.00004))
            .build();

    public static final CrimsonMobProfile UNSTABLE_MAGMA_40 = unstable("UNSTABLE_MAGMA_40", 109, 240_000, 500, 163, 36, 119);
    public static final CrimsonMobProfile UNSTABLE_MAGMA_60 = unstable("UNSTABLE_MAGMA_60", 127, 400_000, 700, 190, 42, 139);
    public static final CrimsonMobProfile UNSTABLE_MAGMA_80 = unstable("UNSTABLE_MAGMA_80", 144, 600_000, 1_000, 216, 48, 158);

    private static CrimsonMobProfile atom(String id, String name) {
        return CrimsonMobProfile.builder(id, name)
                .level(6).entity(EntityType.ARMOR_STAND).stats(100, 100).rewards(9, 2, 6).speed(0)
                .types(MobType.ARCANE).neverCorrupted()
                .build();
    }

    private static CrimsonMobProfile ashfangMinion(String id, String name, double damage) {
        return CrimsonMobProfile.builder(id, name)
                .level(175).entity(EntityType.BLAZE).stats(2_000_000, damage).rewards(262, 58, 192).speed(0)
                .types(MobType.INFERNAL).neverCorrupted()
                .build();
    }

    private static CrimsonMobProfile unstable(String id, int level, double health, double damage, long xp, int coins, int orbs) {
        return CrimsonMobProfile.builder(id, "Unstable Magma")
                .level(level).entity(EntityType.MAGMA_CUBE).stats(health, damage).rewards(xp, coins, orbs).speed(140)
                .types(MobType.CUBIC, MobType.INFERNAL).neverCorrupted()
                .drops(cyclamen(0.00004))
                .build();
    }
}
