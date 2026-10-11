package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Paleontologist: more Suspicious Scrap from Glacite Mineshafts. Obtained from the Fossil Excavator.
public class EnchantmentPaleontologist implements Ench {
    public static final double[] SCRAP_CHANCE_PERCENT = {2.5, 5, 7.5, 10, 12.5};

    @Override
    public String getDescription(int level) {
        return "Increases chances of finding a <9>Suspicious Scrap <7>when mining in <b>Glacite Mineshafts <7>by <a>"
                + SCRAP_CHANCE_PERCENT[level - 1] + "%<7>.\n<8>(Not active yet)";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.DRILL, EnchantItemGroups.GAUNTLET);
    }
}
