package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Sugar Rush enchantment (Roadmap #19, sourced from the Fear Mongerer's Spooky Festival shop):
// grants flat Speed on boots. Only level I's cost/effect (+2 Speed, 20 Exp Levels to apply) was
// confirmed via the shop tooltip screenshot - real Hypixel may have higher levels, not sourced.
//
// Real Hypixel restricts this to boots specifically ("Applicable on: Boots"); this codebase's
// EnchantItemGroups has no boots-only group, only the general ARMOR group, so it's applicable to
// any armor piece here - a documented, intentional divergence rather than a silent approximation.
public class EnchantmentSugarRush implements Ench {

    public static final double[] SPEED_PER_LEVEL = new double[]{2.0};

    @Override
    public String getDescription(int level) {
        return "Grants <a>+" + (int) SPEED_PER_LEVEL[level - 1] + "<stat:speed>";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 20)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder().withBase(ItemStatistic.SPEED, SPEED_PER_LEVEL[level - 1]).build();
    }
}
