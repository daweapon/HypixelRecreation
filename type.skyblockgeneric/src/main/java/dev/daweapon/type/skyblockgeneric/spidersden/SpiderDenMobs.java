package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.entity.EntityType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.always;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.chance;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.pet;

/**
 * Spider's Den mobs from the wiki mob pages (Aug-Oct 2026 level rework), checked against a live Hypixel
 * capture on 2026-10-24 (levels and health of every spider variant matched). The wiki publishes no defense or
 * speed, so defense is 0 and speed is an estimate. Voracious Spider level 11 is interpolated. Each Bestiary family shares one id across its variants.
 */
public final class SpiderDenMobs {
    private static final String SPIDER_HEAD = "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1";
    private static final String SPLITTER_HEAD = "2acf69fc7af5497a1741891d1e5bf32b96ae0d6c0bbd3c14758ea44a03c57283";
    private static final String WEAVER_HEAD = "921438f646dc04515967e8195ccc3d31e23b02f9aa0ac914edc2822f98394b86";
    private static final String VORACIOUS_HEAD = "8300986ed0a04ea79904f6ae53f49ed3a0ff5b1df62bba622ecbd3777f156df8";
    private static final String JOCKEY_HEAD = "70939373cafe4b1f5397aafd09f3bb1663e7b629a41a75fbdc1860b6bf8b475f";
    private static final String SILVERFISH_HEAD = "da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540";
    private static final String RAIN_SLIME_HEAD = "895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c";
    private static final String FLINT_HEAD = "88eb68a4d38e9f446b8e99275f160302f366ef10216abf948487e582512bd0f3";
    private static final String BROODMOTHER_HEAD = "cf0622b3998d42b34d5bc760bb2c83fdbc6e68fab05b7ea17b35097ed81190d6";

    private static final double BONE_DYE_CHANCE = 100D / 3_000_000;

    private SpiderDenMobs() {
    }

    private static MobDrop scroll() {
        return chance("SPIDERS_DEN_TOP_TRAVEL_SCROLL", 0.02);
    }

    private static MobDrop[] tarantula(double epic, double legendary) {
        return new MobDrop[]{pet("TARANTULA_PET", Rarity.EPIC, epic), pet("TARANTULA_PET", Rarity.LEGENDARY, legendary)};
    }

    private static MobDrop[] join(MobDrop[] first, MobDrop... rest) {
        MobDrop[] all = new MobDrop[first.length + rest.length];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(rest, 0, all, first.length, rest.length);
        return all;
    }

    private static CrimsonMobProfile.Builder spider(String id, String name, int level, double health, double damage,
                                                    long xp, int orbs, int coins, double speed, int bracket, int tiers, String head) {
        return CrimsonMobProfile.builder(id, name).level(level).entity(EntityType.SPIDER).stats(health, damage)
                .rewards(xp, coins, orbs).speed(speed).types(MobType.ARTHROPOD).bestiary(bracket, tiers, head).neverCorrupted();
    }

    public static final CrimsonMobProfile DASHER_MOUND = spider("DASHER_SPIDER", "Dasher Spider", 5, 170, 55, 7, 5, 2, 150, 2, 15, SPIDER_HEAD)
            .drops(join(tarantula(0.004, 0.001), always("STRING", 1), chance("SPIDER_EYE", 50), scroll())).build();

    public static final List<CrimsonMobProfile> DASHER_BURROW = List.of(
            burrowDasher(8, 250, 75, 30, 8, 2), burrowDasher(9, 300, 80, 40, 10, 3), burrowDasher(10, 360, 85, 50, 11, 3));

    private static CrimsonMobProfile burrowDasher(int level, double health, double damage, long xp, int orbs, int coins) {
        return spider("DASHER_SPIDER", "Dasher Spider", level, health, damage, xp, orbs, coins, 150, 2, 15, SPIDER_HEAD)
                .drops(join(tarantula(0.004, 0.001), always("STRING", 1, 2), chance("SPIDER_EYE", 80),
                        chance("ARACHNE_KEEPER_FRAGMENT", 0.2), scroll())).build();
    }

    public static final CrimsonMobProfile SPLITTER_MOUND = spider("SPLITTER_SPIDER", "Splitter Spider", 4, 180, 30, 6, 4, 1, 120, 4, 20, SPLITTER_HEAD)
            .drops(join(tarantula(0.002, 0.0008), always("STRING", 1), always("SPIDER_EYE", 1), scroll())).build();

    public static final List<CrimsonMobProfile> SPLITTER_BURROW = List.of(
            burrowSplitter(8, 310, 55, 30, 8, 2), burrowSplitter(9, 370, 60, 40, 10, 3), burrowSplitter(10, 440, 65, 50, 11, 3));

    private static CrimsonMobProfile burrowSplitter(int level, double health, double damage, long xp, int orbs, int coins) {
        return spider("SPLITTER_SPIDER", "Splitter Spider", level, health, damage, xp, orbs, coins, 120, 4, 20, SPLITTER_HEAD)
                .drops(join(tarantula(0.002, 0.001), always("STRING", 1), always("SPIDER_EYE", 1), scroll())).build();
    }

    public static final CrimsonMobProfile SILVERFISH = CrimsonMobProfile.builder("DEN_SILVERFISH", "Silverfish")
            .level(1).entity(EntityType.SILVERFISH).stats(50, 20).rewards(5, 1, 1).speed(130)
            .types(MobType.ARTHROPOD).bestiary(2, 15, SILVERFISH_HEAD).neverCorrupted()
            .drops(chance("STRING", 80)).build();

    public static final CrimsonMobProfile WEAVER_MOUND = spider("WEAVER_SPIDER", "Weaver Spider", 4, 160, 35, 6, 4, 1, 110, 4, 20, WEAVER_HEAD)
            .drops(join(tarantula(0.002, 0.001), always("STRING", 1), chance("SPIDER_EYE", 50), scroll())).build();

    public static final List<CrimsonMobProfile> WEAVER_BURROW = List.of(
            burrowWeaver(7, 260, 55, 30, 7, 2), burrowWeaver(8, 310, 60, 40, 8, 3), burrowWeaver(9, 370, 65, 50, 9, 3));

    private static CrimsonMobProfile burrowWeaver(int level, double health, double damage, long xp, int orbs, int coins) {
        return spider("WEAVER_SPIDER", "Weaver Spider", level, health, damage, xp, orbs, coins, 110, 4, 20, WEAVER_HEAD)
                .drops(join(tarantula(0.004, 0.001), chance("SPIDER_EYE", 80), chance("STRING", 25),
                        chance("ARACHNE_KEEPER_FRAGMENT", 0.02), scroll())).build();
    }

    public static final CrimsonMobProfile VORACIOUS_MOUND = voracious(9, 300, 80, 13, 9, 3);

    public static final List<CrimsonMobProfile> VORACIOUS_BURROW = List.of(
            voracious(10, 360, 85, 30, 10, 3), voracious(11, 435, 90, 40, 12, 4), voracious(13, 510, 95, 50, 14, 4));

    private static CrimsonMobProfile voracious(int level, double health, double damage, long xp, int orbs, int coins) {
        return spider("VORACIOUS_SPIDER", "Voracious Spider", level, health, damage, xp, orbs, coins, 130, 4, 20, VORACIOUS_HEAD)
                .drops(always("STRING", 1), chance("SPIDER_EYE", 10)).build();
    }

    public static final CrimsonMobProfile JOCKEY_MOUND = jockey(6, 220, 45, 9, 6, 2);
    public static final CrimsonMobProfile JOCKEY_BURROW = jockey(12, 500, 90, 30, 13, 4);

    private static CrimsonMobProfile jockey(int level, double health, double damage, long xp, int orbs, int coins) {
        return spider("SPIDER_JOCKEY", "Spider Jockey", level, health, damage, xp, orbs, coins, 140, 4, 15, JOCKEY_HEAD)
                .drops(always("STRING", 2), chance("SPIDER_EYE", 10), scroll()).build();
    }

    public static final CrimsonMobProfile JOCKEY_SKELETON_MOUND = jockeySkeleton(6, 250, 40, 9, 6, 2);
    public static final CrimsonMobProfile JOCKEY_SKELETON_BURROW = jockeySkeleton(16, 1_000, 90, 24, 17, 5);

    private static CrimsonMobProfile jockeySkeleton(int level, double health, double damage, long xp, int orbs, int coins) {
        return CrimsonMobProfile.builder("SPIDER_JOCKEY", "Jockey Skeleton").level(level).entity(EntityType.SKELETON)
                .stats(health, damage).rewards(xp, coins, orbs).speed(100).types(MobType.SKELETAL, MobType.ARTHROPOD)
                .bestiary(4, 15, JOCKEY_HEAD).neverCorrupted()
                .drops(always("BONE", 2), chance("DYE_BONE", BONE_DYE_CHANCE)).build();
    }

    public static final CrimsonMobProfile FLINT_SKELETON = CrimsonMobProfile.builder("FLINT_SKELETON", "Flint Skeleton")
            .level(3).entity(EntityType.SKELETON).stats(100, 35).rewards(5, 1, 3).speed(100)
            .types(MobType.SKELETAL).bestiary(4, 15, FLINT_HEAD).neverCorrupted()
            .drops(always("BONE", 2, 4), always("FLINT", 1, 2), chance("ENCHANTED_BONE", 2), chance("ENCHANTED_FLINT", 1),
                    chance("DYE_BONE", BONE_DYE_CHANCE)).build();

    public static final CrimsonMobProfile BROODMOTHER = spider("BROODMOTHER", "Broodmother", 41, 3_000, 125, 307, 123, 102, 120, 7, 15, BROODMOTHER_HEAD)
            .drops(join(new MobDrop[]{always("STRING", 1), always("SPIDER_EYE", 1), always("SPIDER_TALISMAN", 1),
                    chance("LUXURIOUS_SPOOL", 5).noMagicFind(), chance("SPIDERS_DEN_TOP_TRAVEL_SCROLL", 0.03).noMagicFind()},
                    pet("TARANTULA_PET", Rarity.EPIC, 0.02).noMagicFind(), pet("TARANTULA_PET", Rarity.LEGENDARY, 0.008).noMagicFind()))
            .build();

    public static final CrimsonMobProfile RAIN_SLIME = CrimsonMobProfile.builder("RAIN_SLIME", "Rain Slime")
            .level(6).entity(EntityType.SLIME).stats(200, 50).rewards(9, 2, 6).speed(100)
            .types(MobType.CUBIC).bestiary(4, 15, RAIN_SLIME_HEAD).neverCorrupted()
            .drops(always("SLIME_BALL", 3, 5)).build();

    public static final CrimsonMobProfile TOXIC_RAIN_SLIME = CrimsonMobProfile.builder("RAIN_SLIME", "Toxic Rain Slime")
            .level(15).entity(EntityType.SLIME).stats(750, 100).rewards(22, 5, 16).speed(100)
            .types(MobType.CUBIC).bestiary(4, 15, RAIN_SLIME_HEAD).neverCorrupted()
            .drops(always("SLIME_BALL", 10, 20), chance("ENCHANTED_SLIME_BALL", 50), chance("POISON_SAMPLE", 5)).build();

    public static final CrimsonMobProfile BROOD_CAVE_SPIDER = CrimsonMobProfile.builder("BROODMOTHER_BROOD", "Cave Spider")
            .level(12).entity(EntityType.CAVE_SPIDER).stats(600, 40).rewards(0, 0, 0).speed(140)
            .types(MobType.ARTHROPOD).neverCorrupted().build();

    public static final CrimsonMobProfile ARACHNE_LESSER = spider("ARACHNE", "Arachne", 87, 20_000, 300, 652, 261, 217, 160, 7, 15, SPIDER_HEAD)
            .build();

    public static final CrimsonMobProfile ARACHNE_GREATER = spider("ARACHNE", "Arachne", 143, 100_000, 600, 1_072, 429, 357, 180, 7, 15, SPIDER_HEAD)
            .build();

    public static final CrimsonMobProfile BROOD_LESSER = spider("ARACHNES_BROOD", "Arachne's Brood", 34, 4_000, 200, 51, 37, 11, 150, 4, 15, SPIDER_HEAD)
            .drops(always("SPIDER_EYE", 10), always("STRING", 5)).build();

    public static final CrimsonMobProfile BROOD_GREATER = spider("ARACHNES_BROOD", "Arachne's Brood", 62, 20_000, 400, 93, 68, 20, 170, 4, 15, SPIDER_HEAD)
            .drops(always("SPIDER_EYE", 10), always("STRING", 5)).build();

    public static final CrimsonMobProfile ARACHNES_KEEPER = spider("ARACHNES_KEEPER", "Arachne's Keeper", 31, 3_000, 200, 46, 34, 10, 140, 6, 15, SPIDER_HEAD)
            .drops(always("ARACHNE_KEEPER_FRAGMENT", 1),
                    always("SPIDER_EYE", 4), chance("SPIDER_EYE", 75), chance("SPIDER_EYE", 75), chance("SPIDER_EYE", 50), chance("SPIDER_EYE", 50),
                    always("STRING", 2), chance("STRING", 75), chance("STRING", 50),
                    chance("ARACHNE_NECKLACE", 2.5), chance("ARACHNE_CLOAK", 2.5), chance("ARACHNE_BELT", 2.5), chance("ARACHNE_GLOVES", 2.5))
            .build();

    public static List<CrimsonMobProfile> all() {
        List<CrimsonMobProfile> all = new java.util.ArrayList<>(List.of(DASHER_MOUND, SPLITTER_MOUND, SILVERFISH, WEAVER_MOUND,
                VORACIOUS_MOUND, JOCKEY_MOUND, JOCKEY_BURROW, JOCKEY_SKELETON_MOUND, JOCKEY_SKELETON_BURROW, FLINT_SKELETON,
                BROODMOTHER, BROOD_CAVE_SPIDER, RAIN_SLIME, TOXIC_RAIN_SLIME,
                ARACHNE_LESSER, ARACHNE_GREATER, BROOD_LESSER, BROOD_GREATER, ARACHNES_KEEPER));
        all.addAll(DASHER_BURROW);
        all.addAll(SPLITTER_BURROW);
        all.addAll(WEAVER_BURROW);
        all.addAll(VORACIOUS_BURROW);
        return all;
    }
}
