package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Corruption enchantment: chance to summon a corrupted variant of a caught sea creature.
public class EnchantmentCorruption implements Ench, EnchFromTable {
    @Override
    public String getDescription(int level) {
        return "<a>" + level * 10 + "%<7> chance to summon a <5>Corrupted <7>variant of a caught <3>Sea Creature<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
            1, 5,
            2, 10,
            3, 15,
            4, 20,
            5, 50
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
            1, 5,
            2, 10,
            3, 15,
            4, 20,
            5, 50
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 10;
    }
}
