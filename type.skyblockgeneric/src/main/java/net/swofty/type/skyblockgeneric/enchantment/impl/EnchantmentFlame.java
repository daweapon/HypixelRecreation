package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Flame enchantment for bows (Blaze Rod collection discount).
public class EnchantmentFlame implements Ench {
    private static final String NOT_ACTIVE = "\n<8>(Not active yet)";

    @Override
    public String getDescription(int level) {
        return "Arrows ignite your enemies for <a>" + (level + 2) + "s<7>." + NOT_ACTIVE;
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(1, 15, 2, 30));
        if (player.hasCustomCollectionAward(CustomCollectionAward.FLAME_DISCOUNT)) {
            levels.replaceAll((level, cost) -> (int) (cost * 0.75));
        }
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }
}
