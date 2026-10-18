package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.PlayerContextEnchant;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.BurningDamage;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fire Protection enchantment for armour (Magma Cream collection discount).
public class EnchantmentFireProtection implements Ench, PlayerContextEnchant {
    public static final int[] COSTS = {10, 15, 20, 25, 30, 100, 200};

    public static double trueDefense(int level) {
        return level * 2D;
    }

    @Override
    public String getDescription(int level) {
        return "Grants <f>+" + (int) trueDefense(level) + " ❂ True Defense <7>against fire, lava and burn damage.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(1, COSTS[0], 2, COSTS[1], 3, COSTS[2], 4, COSTS[3], 5, COSTS[4], 6, COSTS[5], 7, COSTS[6]));
        if (player.hasCustomCollectionAward(CustomCollectionAward.FIRE_PROTECTION_DISCOUNT)) {
            levels.replaceAll((level, cost) -> (int) (cost * 0.75));
        }
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int level) {
        if (level < 1 || !BurningDamage.isTakingFireDamage(player)) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.TRUE_DEFENSE, trueDefense(level)).build();
    }
}
