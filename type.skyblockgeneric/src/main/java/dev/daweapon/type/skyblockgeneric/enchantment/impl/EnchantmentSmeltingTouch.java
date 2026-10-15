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

// Smelting Touch enchantment: automatically smelts broken blocks into their smelted form;
// conflicts with Silk Touch.
public class EnchantmentSmeltingTouch implements Ench, EnchFromTable, ConflictingEnch {

    @Override
    public String getDescription(int level) {
        return "<7>Automatically smelts broken blocks into their smelted form.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 5
        ));
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.PICKAXE, EnchantItemGroups.TOOLS, EnchantItemGroups.AXE, EnchantItemGroups.DRILL, EnchantItemGroups.GAUNTLET, EnchantItemGroups.SHOVEL);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 5
        ));
        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.SILK_TOUCH);
    }
}