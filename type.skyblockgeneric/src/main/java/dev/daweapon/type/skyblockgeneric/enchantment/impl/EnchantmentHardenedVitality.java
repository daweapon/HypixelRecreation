package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Hardened Vitality (Kuudra; reworked from a Mana enchant in 2026, internal id kept).
public class EnchantmentHardenedVitality implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        return "Convert <a>" + (level * 4) + "% <7>of <4>♨ Vitality <7>used as <a>❈ Defense <7>for <a>10s<7>. <8>(Max 25 Defense)" + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 10, 2, 20, 3, 30, 4, 40, 5, 50, 6, 60, 7, 70, 8, 80, 9, 90, 10, 100)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public boolean isEffectActive() {
        return false;
    }
}
