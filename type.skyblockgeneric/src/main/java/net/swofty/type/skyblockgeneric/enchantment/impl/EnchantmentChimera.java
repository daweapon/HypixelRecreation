package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Chimera ultimate enchantment: copies 20% of the active pet's stats per level. The copying lives
// in MythologicalGear, which knows the wielder's pet.
public class EnchantmentChimera implements Ench {
    public static final double COPY_PER_LEVEL = 0.2;

    @Override
    public String getDescription(int level) {
        return "Copies <a>" + Math.round(level * COPY_PER_LEVEL * 100) + "% <7>of your active pet's stats.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 50, 2, 100, 3, 150, 4, 200, 5, 250)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET);
    }

    @Override
    public boolean isUltimate() {
        return true;
    }
}
