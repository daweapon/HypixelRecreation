package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Inferno ultimate enchantment (Kuudra loot).
public class EnchantmentInferno implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        return "Every <a>10th <7>hit on a mob spawns an inferno which traps the mob and deals <c>" + (100 + level * 25) + "% <7>of your last hit." + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 20, 2, 40, 3, 60, 4, 80, 5, 100)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD, EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.AXE, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.BOW);
    }

    @Override
    public boolean isUltimate() {
        return true;
    }
}
