package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.Arrays;
import java.util.List;

/**
 * Stats and drops from the Hypixel SkyBlock Wiki mob pages (Aug 2026 balance pass). The wiki lists no speed or
 * defense for these mobs: speed 100 (Enderman 120) and defense 0 are estimates.
 */
public final class EndMobSpecs {
    private EndMobSpecs() {
    }

    public record Extra(ItemType item, List<Double> chancesPercent) {
        public static Extra of(ItemType item, double... chancesPercent) {
            return new Extra(item, Arrays.stream(chancesPercent).boxed().toList());
        }
    }

    public static final List<Extra> ZEALOT_EXTRAS = List.of(Extra.of(ItemType.ENDER_PEARL, 80, 40));
    public static final List<Extra> SEER_EXTRAS = List.of(
            Extra.of(ItemType.FLINT_ARROW, 50, 50, 50), Extra.of(ItemType.ENDER_PEARL, 50));
    public static final List<Extra> OBSIDIAN_DEFENDER_EXTRAS = List.of(Extra.of(ItemType.OBSIDIAN, 50));

    private static final double PEARLESCENT_DYE_CHANCE = 0.00001;
    private static final double BONE_DYE_CHANCE = 100D / 3_000_000;

    public static final CrystalHollowsMobSpec ENDERMAN_LOW = enderman("ENDERMAN_42", 45, 4_500, 500, 67, 15, 49, 1, 3);
    public static final CrystalHollowsMobSpec ENDERMAN_MID = enderman("ENDERMAN_45", 51, 6_000, 600, 76, 17, 56, 1, 3);
    public static final CrystalHollowsMobSpec ENDERMAN_HIGH = enderman("ENDERMAN_50", 59, 9_000, 700, 88, 19, 64, 1, 2);

    public static final CrystalHollowsMobSpec ENDERMITE_LOW = CrystalHollowsMobSpec.of("ENDERMITE_37", "Endermite", 34, 2_000, 400)
            .bestiary(7, 25, Material.ENDERMITE_SPAWN_EGG)
            .rewards(51, 11, 37)
            .types(MobType.ENDER)
            .aggroRange(8)
            .drops(Drop.always(ItemType.END_STONE, 1, 2), Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE));

    public static final CrystalHollowsMobSpec ENDERMITE_HIGH = CrystalHollowsMobSpec.of("ENDERMITE_40", "Endermite", 37, 2_300, 475)
            .bestiary(7, 25, Material.ENDERMITE_SPAWN_EGG)
            .rewards(55, 12, 40)
            .types(MobType.ENDER)
            .aggroRange(8)
            .drops(Drop.always(ItemType.END_STONE, 2, 4), Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE));

    public static final CrystalHollowsMobSpec NEST_ENDERMITE = CrystalHollowsMobSpec.of("NEST_ENDERMITE_50", "Nest Endermite", 50, 4_500, 750)
            .bestiary(7, 25, Material.ENDERMITE_SPAWN_EGG)
            .rewards(75, 16, 55)
            .types(MobType.ENDER)
            .aggroRange(8)
            .drops(
                    Drop.always(ItemType.ENCHANTED_END_STONE, 1, 1),
                    Drop.always(ItemType.MITE_GEL, 1, 1),
                    Drop.single(ItemType.DYE_PEARLESCENT, 0.00002));

    public static final CrystalHollowsMobSpec SEER = CrystalHollowsMobSpec.of("WATCHER_55", "Seer", 55, 9_500, 500)
            .bestiary(7, 25, Material.ENDER_EYE)
            .rewards(82, 18, 60)
            .types(MobType.ENDER)
            .aggroRange(16)
            .drops(
                    Drop.always(ItemType.FLINT_ARROW, 1, 1),
                    Drop.always(ItemType.ENDER_PEARL, 1, 1),
                    Drop.single(ItemType.ENCHANTED_BONE, 1),
                    Drop.single(ItemType.ENDER_CLOAK, 1),
                    Drop.single(ItemType.ENDER_MONOCLE, 0.5),
                    Drop.single(ItemType.END_STONE_BOW, 0.1),
                    Drop.single(ItemType.PET_ITEM_COMBAT_SKILL_BOOST_RARE, 0.015),
                    Drop.single(ItemType.PET_ITEM_COMBAT_SKILL_BOOST_EPIC, 0.002),
                    Drop.single(ItemType.DYE_BONE, BONE_DYE_CHANCE),
                    Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE));

    public static final CrystalHollowsMobSpec OBSIDIAN_DEFENDER = CrystalHollowsMobSpec.of("OBSIDIAN_WITHER_55", "Obsidian Defender", 44, 10_000, 200)
            .bestiary(7, 25, Material.OBSIDIAN)
            .rewards(66, 14, 48)
            .types(MobType.CONSTRUCT)
            .aggroRange(8)
            .drops(
                    Drop.always(ItemType.OBSIDIAN, 6, 6),
                    Drop.single(ItemType.ENCHANTED_OBSIDIAN, 1),
                    Drop.single(ItemType.ENDER_BELT, 1),
                    Drop.single(ItemType.OBSIDIAN_CHESTPLATE, 0.1),
                    Drop.single(ItemType.PET_ITEM_COMBAT_SKILL_BOOST_RARE, 0.01),
                    Drop.single(ItemType.PET_ITEM_COMBAT_SKILL_BOOST_EPIC, 0.0033),
                    Drop.single(ItemType.DYE_BONE, BONE_DYE_CHANCE),
                    Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE));

    public static final CrystalHollowsMobSpec VOIDLING_FANATIC = CrystalHollowsMobSpec.of("VOIDLING_FANATIC_85", "Voidling Fanatic", 183, 750_000, 3_500)
            .speed(120)
            .bestiary(5, 25, Material.ENDER_PEARL)
            .rewards(274, 61, 201)
            .types(MobType.ENDER)
            .provokedOnly()
            .drops(
                    Drop.always(ItemType.ENDER_PEARL, 4, 5),
                    Drop.single(ItemType.ENCHANTED_ENDER_PEARL, 1),
                    Drop.single(ItemType.DYE_PEARLESCENT, 0.00002));

    public static final CrystalHollowsMobSpec VOIDLING_EXTREMIST = CrystalHollowsMobSpec.of("VOIDLING_EXTREMIST_100", "Voidling Extremist", 305, 8_000_000, 13_500)
            .speed(120)
            .bestiary(4, 15, Material.ENDER_PEARL)
            .rewards(457, 101, 335)
            .types(MobType.ENDER)
            .provokedOnly()
            .drops(
                    Drop.always(ItemType.ENDER_PEARL, 32, 32),
                    Drop.single(ItemType.ENCHANTED_ENDER_PEARL, 16),
                    Drop.single(ItemType.ENDERMAN_CORTEX_REWRITER, 0.1),
                    Drop.single(ItemType.SUMMONING_EYE, 0.04),
                    Drop.single(ItemType.DYE_PEARLESCENT, 0.00002));

    public static final CrystalHollowsMobSpec ZEALOT = zealot(false);
    public static final CrystalHollowsMobSpec ZEALOT_WITH_CHEST = zealot(true);

    public static final CrystalHollowsMobSpec ZEALOT_BRUISER = CrystalHollowsMobSpec.of("ZEALOT_BRUISER_100", "Zealot Bruiser", 115, 65_000, 2_500)
            .speed(120)
            .bestiary(5, 25, Material.ENDER_CHEST)
            .rewards(172, 38, 126)
            .types(MobType.ENDER)
            .provokedOnly()
            .drops(
                    Drop.always(ItemType.ENDER_PEARL, 2, 2),
                    Drop.single(ItemType.ENCHANTED_ENDER_PEARL, 4),
                    Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE));

    public static final CrystalHollowsMobSpec SPECIAL_ZEALOT = CrystalHollowsMobSpec.of("ZEALOT_SPECIAL_ENDERMAN_55", "Special Zealot", 73, 13_000, 1_250)
            .speed(120)
            .bestiary(5, 25, Material.ENDER_CHEST)
            .rewards(109, 24, 80)
            .types(MobType.ENDER)
            .aggroRange(16)
            .drops(
                    Drop.always(ItemType.ENDER_PEARL, 2, 2),
                    Drop.single(ItemType.ENCHANTED_ENDER_PEARL, 2),
                    Drop.single(ItemType.DYE_PEARLESCENT, 0.001));


    private static CrystalHollowsMobSpec enderman(String id, int level, double health, double damage,
                                                  long xp, int coins, int orbs, int minPearls, int maxPearls) {
        return CrystalHollowsMobSpec.of(id, "Enderman", level, health, damage)
                .speed(120)
                .bestiary(7, 25, Material.ENDERMAN_SPAWN_EGG)
                .rewards(xp, coins, orbs)
                .types(MobType.ENDER)
                .aggroRange(10)
                .drops(
                        Drop.always(ItemType.ENDER_PEARL, minPearls, maxPearls),
                        Drop.single(ItemType.ENCHANTED_ENDER_PEARL, 1),
                        Drop.single(ItemType.ENDER_NECKLACE, 0.5),
                        Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE))
                .exclusiveDrops(
                        Drop.single(ItemType.END_HELMET, 0.25),
                        Drop.single(ItemType.END_CHESTPLATE, 0.25),
                        Drop.single(ItemType.END_LEGGINGS, 0.25),
                        Drop.single(ItemType.END_BOOTS, 0.25));
    }

    private static CrystalHollowsMobSpec zealot(boolean carryingChest) {
        CrystalHollowsMobSpec spec = CrystalHollowsMobSpec.of("ZEALOT_ENDERMAN_55", "Zealot", 73, 13_000, 1_250)
                .speed(120)
                .bestiary(5, 25, Material.ENDER_CHEST)
                .rewards(109, 24, 80)
                .types(MobType.ENDER)
                .provokedOnly()
                .drops(
                        Drop.always(ItemType.ENDER_PEARL, 2, 2),
                        Drop.single(ItemType.ENCHANTED_ENDER_PEARL, 2),
                        Drop.single(ItemType.DYE_PEARLESCENT, PEARLESCENT_DYE_CHANCE));
        if (!carryingChest) return spec;
        return spec.exclusiveDrops(
                Drop.rolled(ItemType.ENCHANTED_ENDER_PEARL, 2, 2, 40.65),
                Drop.single(ItemType.ENCHANTED_END_STONE, 24.39),
                Drop.single(ItemType.ENCHANTED_OBSIDIAN, 16.26),
                Drop.single(ItemType.ENCHANTED_EYE_OF_ENDER, 6.5));
    }
}
