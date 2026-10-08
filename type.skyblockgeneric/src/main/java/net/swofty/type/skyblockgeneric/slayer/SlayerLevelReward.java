package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;

import java.util.List;
import java.util.Map;

/**
 * One slayer level: its XP threshold and title, the permanent stats it grants, the recipes it
 * unlocks, perk lines, and catalogued rewards whose content isn't in the game yet.
 */
public record SlayerLevelReward(
    int level,
    int requiredXp,
    String title,
    Map<ItemStatistic, Double> stats,
    List<ItemType> unlocks,
    List<String> perks,
    List<String> catalogued
) {
    private static final int[] SKYBLOCK_XP = {15, 25, 35, 50, 75, 100, 125, 150, 150};

    public SlayerLevelReward(int level, int requiredXp, String title) {
        this(level, requiredXp, title, Map.of(), List.of(), List.of(), List.of());
    }

    public int skyBlockXp() {
        return level >= 1 && level <= SKYBLOCK_XP.length ? SKYBLOCK_XP[level - 1] : 0;
    }
}
