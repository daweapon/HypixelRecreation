package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

/**
 * Stats and drops from the Hypixel SkyBlock Wiki mob pages (2026-08-25 / 2026-10-06 rebalance). The wiki lists no
 * speed or aggro range: speed 100 and a 16-block aggro range (the Hub Ruins wolves' values) are estimates.
 */
public final class HowlingCaveMobSpecs {
    private HowlingCaveMobSpecs() {
    }

    public record HoundChance(Rarity rarity, double chancePercent) {
    }

    public static final String HOUND_PET = "HOUND_PET";
    public static final List<HoundChance> HOUND_CHANCES = List.of(
            new HoundChance(Rarity.EPIC, 0.003), new HoundChance(Rarity.LEGENDARY, 0.001));

    public static final String SPIRIT_TEXTURE = "69d1d3113ec43ac2961dd59f28175fb4718873c6c448dfca8722317d67";
    public static final String ALPHA_TEXTURE = "d359537c15534f61c1cd886bc118774ed22280e7cdab6613870160aad4ca39";

    private static final double SPIRIT_TRAVEL_SCROLL_CHANCE = 0.02;
    private static final int AGGRO_RANGE = 16;

    public static final CrystalHollowsMobSpec PACK_SPIRIT = wolf("PACK_SPIRIT_30", "Pack Spirit", 43, 6_000, 300, 3)
            .rewards(64, 14, 47)
            .drops(
                    Drop.single(ItemType.BONE, 50),
                    Drop.single(ItemType.OAK_LOG, 15),
                    Drop.single(ItemType.BIRCH_LOG, 15),
                    Drop.single(ItemType.PARK_CAVE_TRAVEL_SCROLL, SPIRIT_TRAVEL_SCROLL_CHANCE));

    public static final CrystalHollowsMobSpec HOWLING_SPIRIT = wolf("HOWLING_SPIRIT_35", "Howling Spirit", 51, 7_000, 500, 3)
            .rewards(76, 17, 56)
            .drops(
                    Drop.single(ItemType.SPRUCE_LOG, 15),
                    Drop.single(ItemType.DARK_OAK_LOG, 15),
                    Drop.single(ItemType.ACACIA_LOG, 15),
                    Drop.single(ItemType.PARK_CAVE_TRAVEL_SCROLL, SPIRIT_TRAVEL_SCROLL_CHANCE));

    public static final CrystalHollowsMobSpec SOUL_OF_THE_ALPHA = wolf("SOUL_OF_THE_ALPHA_55", "Soul of the Alpha", 90, 31_150, 1_425, 4)
            .rewards(135, 30, 99)
            .drops(
                    Drop.always(ItemType.JUNGLE_LOG, 1, 1),
                    Drop.single(ItemType.WEAK_WOLF_CATALYST, 0.089),
                    Drop.single(ItemType.PARK_CAVE_TRAVEL_SCROLL, 0.035),
                    Drop.single(ItemType.PET_ITEM_FORAGING_SKILL_BOOST_EPIC, 0.01));

    private static CrystalHollowsMobSpec wolf(String id, String name, int level, double health, double damage, int bracket) {
        return CrystalHollowsMobSpec.of(id, name, level, health, damage)
                .bestiary(bracket, 15, Material.BONE)
                .types(MobType.ANIMAL, MobType.ARCANE)
                .aggroRange(AGGRO_RANGE);
    }
}
