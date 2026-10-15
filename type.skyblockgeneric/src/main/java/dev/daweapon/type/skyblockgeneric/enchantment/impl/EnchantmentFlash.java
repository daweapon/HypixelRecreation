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

// Flash enchantment: grants bonus Fishing Speed.
public class EnchantmentFlash implements Ench, EnchFromTable {
    @Override
    public String getDescription(int level) {
        return "Grants <b>+" + level * 5 + "☂ Fishing Speed<7>.";
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder().withBase(ItemStatistic.FISHING_SPEED, level * 5.0).build();
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
            1, 10,
            2, 20,
            3, 30,
            4, 60,
            5, 100
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD, EnchantItemGroups.FISHING_WEAPON);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
            1, 10,
            2, 20,
            3, 30,
            4, 60,
            5, 100
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
