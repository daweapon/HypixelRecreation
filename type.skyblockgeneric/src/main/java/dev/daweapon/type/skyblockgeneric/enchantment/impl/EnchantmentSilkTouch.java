package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Silk Touch enchantment: lets a pickaxe collect normally unobtainable block drops; conflicts
// with Smelting Touch and Fortune.
public class EnchantmentSilkTouch implements Ench, EnchFromTable, ConflictingEnch {

    @Override
    public String getDescription(int level) {
        return "<7>Allows you to collect normally unobtainable block drops.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10
        ));
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(
                EnchantItemGroups.PICKAXE,
                EnchantItemGroups.TOOLS
        );
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10
        ));
        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 5;
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.SMELTING_TOUCH, EnchantmentType.FORTUNE);
    }
}