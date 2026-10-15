package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Caster enchantment: chance to not consume bait when fishing.
public class EnchantmentCaster implements Ench, EnchFromTable {
    @Override
    public String getDescription(int level) {
        return "<a>" + level * 5 + "%<7> chance to not consume bait.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
            1, 10,
            2, 15,
            3, 20,
            4, 25,
            5, 30,
            6, 60
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
            2, 15,
            3, 20,
            4, 25,
            5, 30
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
