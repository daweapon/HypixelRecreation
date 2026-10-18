package dev.daweapon.type.skyblockgeneric.island;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.minion.MinionCategories;

import java.util.Set;

public final class FloatingCrystalBonus {
    public static final double FARM_CRYSTAL_PERCENT = 10;
    public static final double WOODCUTTING_CRYSTAL_PERCENT = 10;
    public static final double WINTER_CRYSTAL_PERCENT = 5;
    public static final double CORNUCOPIA_CRYSTAL_BASE_PERCENT = 10;
    public static final double CORNUCOPIA_CRYSTAL_PERCENT_PER_UNIQUE = 1;
    public static final double POSTCARD_PERCENT = 5;

    private FloatingCrystalBonus() {
    }

    public static double percent(Set<ItemType> placed, String minionName, int uniqueFarmingMinions) {
        double bonus = 0;
        if (placed.contains(ItemType.POSTCARD)) bonus += POSTCARD_PERCENT;
        if (MinionCategories.isFarming(minionName)) {
            if (placed.contains(ItemType.FARM_CRYSTAL)) bonus += FARM_CRYSTAL_PERCENT;
            if (placed.contains(ItemType.CORNUCOPIA_CRYSTAL)) {
                bonus += CORNUCOPIA_CRYSTAL_BASE_PERCENT + CORNUCOPIA_CRYSTAL_PERCENT_PER_UNIQUE * uniqueFarmingMinions;
            }
        }
        if (MinionCategories.isForaging(minionName) && placed.contains(ItemType.WOODCUTTING_CRYSTAL)) {
            bonus += WOODCUTTING_CRYSTAL_PERCENT;
        }
        if (MinionCategories.isWinter(minionName) && placed.contains(ItemType.WINTER_ISLAND_CRYSTAL)) {
            bonus += WINTER_CRYSTAL_PERCENT;
        }
        return bonus;
    }
}
