package dev.daweapon.type.theend.dragon;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DragonLoot {
    public static final double PEARLESCENT_DYE_CHANCE = 0.001;
    public static final double SUPERIOR_PEARLESCENT_DYE_CHANCE = 0.002;

    public interface Roller {
        boolean roll(ItemType item, double chancePercent);
    }

    public record Reward(ItemType item, int amount, @Nullable Rarity rarity, boolean major) {
    }

    private record Major(String id, int quality, double chance, @Nullable Rarity rarity) {
    }

    private DragonLoot() {
    }

    public static List<Reward> plan(DragonVariant variant, int quality, int eyesPlaced, int rank, double damage,
                                    Roller roller) {
        List<Reward> rewards = new ArrayList<>();
        int remaining = quality;
        for (Major major : majors(variant, eyesPlaced)) {
            ItemType item = ItemType.get(major.id());
            if (item == null || quality < major.quality() || remaining < major.quality()) continue;
            if (!roller.roll(item, major.chance())) continue;
            remaining -= major.quality();
            rewards.add(new Reward(item, 1, major.rarity(), true));
            break;
        }
        add(rewards, ItemType.get(variant.fragmentId()), LootQuality.fragments(remaining), null);
        add(rewards, ItemType.ENDER_PEARL, LootQuality.enderPearls(rank, damage), null);
        add(rewards, ItemType.ENCHANTED_ENDER_PEARL, LootQuality.enchantedEnderPearls(rank), null);
        ItemType dye = ItemType.get("DYE_PEARLESCENT");
        double dyeChance = variant.superior() ? SUPERIOR_PEARLESCENT_DYE_CHANCE : PEARLESCENT_DYE_CHANCE;
        if (dye != null && roller.roll(dye, dyeChance)) rewards.add(new Reward(dye, 1, null, true));
        return rewards;
    }

    private static void add(List<Reward> rewards, @Nullable ItemType item, int amount, @Nullable Rarity rarity) {
        if (item != null && amount > 0) rewards.add(new Reward(item, amount, rarity, false));
    }

    private static List<Major> majors(DragonVariant variant, int eyes) {
        List<Major> majors = new ArrayList<>();
        majors.add(new Major("ENDER_DRAGON_PET", 450, eyes * 0.01, Rarity.LEGENDARY));
        majors.add(new Major("ENDER_DRAGON_PET", 449, eyes * 0.05, Rarity.EPIC));
        if (variant.superior()) majors.add(new Major("DRAGON_HORN", 448, 30, null));
        if (!variant.superior()) majors.add(new Major("ASPECT_OF_THE_DRAGON", 447, eyes * 3.0, null));
        majors.add(new Major("DRAGON_CLAW", 446, eyes * 2.0, null));
        majors.add(new Major(variant.armorId("CHESTPLATE"), 410, 30, null));
        majors.add(new Major(variant.armorId("LEGGINGS"), 360, 30, null));
        if (variant == DragonVariant.YOUNG) majors.add(new Major("DRAGON_SCALE", 296, 30, null));
        majors.add(new Major(variant.armorId("HELMET"), 295, 30, null));
        majors.add(new Major(variant.armorId("BOOTS"), 290, 30, null));
        majors.add(new Major("DRAGON_NEST_TRAVEL_SCROLL", 250, variant == DragonVariant.UNSTABLE ? 35 : 30, null));
        return majors;
    }
}
