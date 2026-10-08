package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Cayenne enchantment for equipment (Chili Pepper collection).
public class EnchantmentCayenne implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        String health = level >= 5 ? "1" : "0.8";
        String trueDefense = level >= 5 ? "0.5" : "0.4";
        return "Grants <c>+" + health + " ❤ Health <7>and <f>+" + trueDefense + " ❂ True Defense <7>per digit in your Accessory Power." + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(4, 40, 5, 50)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.EQUIPMENT);
    }
}
