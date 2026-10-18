package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryPower;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.PlayerContextEnchant;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Cayenne enchantment for equipment (Chili Pepper collection).
public class EnchantmentCayenne implements Ench, PlayerContextEnchant {
    public static final int[] COSTS = {100, 200};

    public static int digits(int accessoryPower) {
        return accessoryPower <= 0 ? 0 : String.valueOf(accessoryPower).length();
    }

    public static double health(int level) {
        return level >= 5 ? 1 : 0.8;
    }

    public static double trueDefense(int level) {
        return level >= 5 ? 0.5 : 0.4;
    }

    @Override
    public String getDescription(int level) {
        String health = level >= 5 ? "1" : "0.8";
        String trueDefense = level >= 5 ? "0.5" : "0.4";
        return "Grants <c>+" + health + " ❤ Health <7>and <f>+" + trueDefense + " ❂ True Defense <7>per digit in your Accessory Power.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(4, COSTS[0], 5, COSTS[1])));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.EQUIPMENT);
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int level) {
        if (level < 4) return ItemStatistics.empty();
        int digits = digits(AccessoryPower.of(player));
        if (digits == 0) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, health(level) * digits)
                .withBase(ItemStatistic.TRUE_DEFENSE, trueDefense(level) * digits)
                .build();
    }
}
