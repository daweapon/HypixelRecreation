package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Charm enchantment: increases the chance of catching higher-tiered trophy fish.
public class EnchantmentCharm implements Ench {
    @Override
    public String getDescription(int level) {
        return "<7>Increases the chance to receive higher-tiered Trophy Fish by <a>" + level * 2 + "%</a>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.ofEntries(
            Map.entry(1, 10),
            Map.entry(2, 20),
            Map.entry(3, 30),
            Map.entry(4, 40),
            Map.entry(5, 50),
            Map.entry(6, 100)
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD);
    }
}
