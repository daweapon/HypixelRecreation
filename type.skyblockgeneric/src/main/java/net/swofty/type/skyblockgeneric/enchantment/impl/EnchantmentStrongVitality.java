package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Strong Vitality (Kuudra; reworked from a Mana enchant in 2026, internal id kept).
public class EnchantmentStrongVitality implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        return "Convert <a>" + (level * 2) + "% <7>of <4>♨ Vitality <7>used as <c>❁ Strength <7>for <a>10s<7>. <8>(Max 30 Strength)" + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 10, 2, 20, 3, 30, 4, 40, 5, 50, 6, 60, 7, 70, 8, 80, 9, 90, 10, 100)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }
}
