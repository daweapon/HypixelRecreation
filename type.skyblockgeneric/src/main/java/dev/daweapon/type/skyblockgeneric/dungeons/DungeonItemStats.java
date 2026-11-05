package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.DungeonItemComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * Dungeon item scaling inside the Catacombs: the item's stats (reforge, books and gems included) times
 * 1 + (Catacombs item stat bonus + 10% per star, up to 5, + 5% per Master Star in Master Mode). The bonuses add
 * together before multiplying. Outside the Catacombs the normal +2% per star applies instead. Health Regen and
 * Vitality are not scaled.
 */
public final class DungeonItemStats {
    public static final int STAR_BONUS_PERCENT = 10;
    public static final int MASTER_STAR_BONUS_PERCENT = 5;
    public static final int MAX_STARS = 5;
    private static final Set<ItemStatistic> UNSCALED = EnumSet.of(ItemStatistic.HEALTH_REGENERATION, ItemStatistic.VITALITY);

    private DungeonItemStats() {
    }

    public static double multiplier(int catacombsLevel, int stars, int masterStars, boolean masterMode) {
        int percent = CatacombsLevels.itemStatBonusPercent(catacombsLevel)
                + STAR_BONUS_PERCENT * clampStars(stars)
                + (masterMode ? MASTER_STAR_BONUS_PERCENT * clampStars(masterStars) : 0);
        return 1 + percent / 100.0;
    }

    public static double multiplier(SkyBlockPlayer player, SkyBlockItem item) {
        return multiplier(CatacombsProgress.level(player), item.getAttributeHandler().getUpgradeLevel(), 0, false);
    }

    public static boolean scales(ItemStatistic statistic) {
        return !UNSCALED.contains(statistic);
    }

    public static boolean isDungeonItem(@Nullable SkyBlockItem item) {
        return item != null && !item.isAir() && item.hasComponent(DungeonItemComponent.class);
    }

    public static double inDungeonValue(double unstarredValue, double multiplier) {
        return unstarredValue * multiplier;
    }

    public static ItemStatistics bonus(ItemStatistics unstarred, ItemStatistics outsideStars, double multiplier) {
        ItemStatistics.Builder bonus = ItemStatistics.builder();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            if (!scales(statistic)) continue;
            double extra = unstarred.getBase(statistic) * (multiplier - 1) - outsideStars.getBase(statistic);
            if (extra != 0) bonus.withBase(statistic, extra);
        }
        return bonus.build();
    }

    public static ItemStatistics extraItemStatistics(SkyBlockItem item, @Nullable SkyBlockPlayer player, ItemStatistics itemExtras) {
        if (player == null || !CatacombsZone.isInCatacombs() || !isDungeonItem(item)) return ItemStatistics.empty();
        ItemAttributeHandler handler = item.getAttributeHandler();
        return bonus(ItemStatistics.add(handler.getUnstarredStatistics(), itemExtras), handler.getStarStatistics(),
                multiplier(player, item));
    }

    private static int clampStars(int stars) {
        return Math.max(0, Math.min(MAX_STARS, stars));
    }
}
