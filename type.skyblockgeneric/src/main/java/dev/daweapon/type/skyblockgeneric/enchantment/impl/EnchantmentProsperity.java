package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentSource;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Prosperity enchantment: grants +3 Health per level on equipment. Books only drop from the Reindrake.
public class EnchantmentProsperity implements Ench {
    private static final int HEALTH_PER_LEVEL = 3;

    @Override
    public String getDescription(int level) {
        return "Grants <a>+" + level * HEALTH_PER_LEVEL + " <c>❤ Health<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 91, 2, 91, 3, 91, 4, 91, 5, 179)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.EQUIPMENT);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder().withBase(ItemStatistic.HEALTH, (double) level * HEALTH_PER_LEVEL).build();
    }

    @Override
    public List<EnchantmentSource> getSources(SkyBlockPlayer player) {
        return List.of(new EnchantmentSource("<c>Reindrake", 1, 1));
    }
}
