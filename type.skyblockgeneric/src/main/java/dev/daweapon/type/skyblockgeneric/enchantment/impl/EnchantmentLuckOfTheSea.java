package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Luck of the Sea enchantment: grants bonus Magic Find on fishing rods.
public class EnchantmentLuckOfTheSea implements Ench, EnchFromTable {
    @Override
    public String getDescription(int level) {
        return "Grants <b>+" + level + "✯ Magic Find<7>.";
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, (double) level).build();
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
            1, 10,
            2, 20,
            3, 30,
            4, 40,
            5, 50,
            6, 80,
            7, 120
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
            1, 10,
            2, 20,
            3, 30,
            4, 40,
            5, 50
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
