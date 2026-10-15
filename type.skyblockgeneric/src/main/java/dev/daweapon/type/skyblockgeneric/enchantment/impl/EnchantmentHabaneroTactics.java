package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Habanero Tactics ultimate enchantment (Chili Pepper collection).
public class EnchantmentHabaneroTactics implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        boolean five = level >= 5;
        return "<a>ℏ <7>Heal <a>+" + (five ? "7.5" : "6") + "% <7>more from wands.\n"
                + "<a>ℏ <7>Deal <c>+" + (five ? 25 : 20) + "% <7>damage against Slayer Bosses with Slayer weapons.\n"
                + "<a>ℏ <7>Gain <3>+" + (five ? "5" : "2.5") + "☯ Combat Wisdom <7>with Slayer weapons.\n"
                + "<a>ℏ <7>With Smoldering Polarization, gain <b>+" + (five ? 15 : 10) + " Magic Find <7>on your slayer weapon." + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(4, 40, 5, 50)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public boolean isUltimate() {
        return true;
    }
}
