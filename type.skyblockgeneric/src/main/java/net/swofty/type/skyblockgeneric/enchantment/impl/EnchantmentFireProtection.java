package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fire Protection enchantment for armour (Magma Cream collection discount).
public class EnchantmentFireProtection implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        return "Grants <f>+" + (level * 2) + " ❂ True Defense <7>against fire and lava." + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(1, 10, 2, 20, 3, 30, 4, 40, 5, 50, 6, 60, 7, 70));
        if (player.hasCustomCollectionAward(CustomCollectionAward.FIRE_PROTECTION_DISCOUNT)) {
            levels.replaceAll((level, cost) -> (int) (cost * 0.75));
        }
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }
}
