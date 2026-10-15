package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Tabasco enchantment (Chili Pepper collection): flat weapon damage, starting at tier II.
public class EnchantmentTabasco implements Ench {
    @Override
    public String getDescription(int level) {
        return "Grants <a>+" + level + " <7>weapon damage if you don't have a <5>Dragon <7>pet equipped.";
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, (double) level).build();
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(2, 0, 3, 0)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD, EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.AXE, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.BOW);
    }
}
