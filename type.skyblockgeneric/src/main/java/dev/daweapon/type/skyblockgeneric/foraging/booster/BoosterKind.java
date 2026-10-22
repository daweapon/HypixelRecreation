package dev.daweapon.type.skyblockgeneric.foraging.booster;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

public enum BoosterKind {
    SWEEP("Sweep", "SWEEP_BOOSTER", "SWEEP_BOOSTER_UNCOMMON", ItemStatistic.SWEEP,
            new double[]{5, 2, 1}, new double[]{10, 5, 2}),
    FORAGING_FORTUNE("Foraging Fortune", "FORAGING_FORTUNE_BOOSTER", "FORAGING_FORTUNE_BOOSTER_UNCOMMON",
            ItemStatistic.FORAGING_FORTUNE, new double[]{10, 5, 2}, new double[]{20, 10, 5}),
    FORAGING_WISDOM("Foraging Wisdom", "FORAGING_WISDOM_BOOSTER", "FORAGING_WISDOM_BOOSTER_UNCOMMON",
            ItemStatistic.FORAGING_WISDOM, new double[]{2, 1, 0.5}, new double[]{3, 2, 1}),
    HUNTING_WISDOM("Hunting Wisdom", "HUNTING_WISDOM_BOOSTER_COMMON", "HUNTING_WISDOM_BOOSTER_UNCOMMON",
            ItemStatistic.HUNTING_WISDOM, new double[]{2, 1, 0.5}, new double[]{3, 2, 1}),
    FIGHTING("Fighting", "FIGHTING_BOOSTER", "FIGHTING_BOOSTER_UNCOMMON", null,
            new double[]{5, 2, 1}, new double[]{10, 5, 2});

    public static final int COMMON = 1;
    public static final int UNCOMMON = 2;

    private final String displayName;
    private final String commonItem;
    private final String uncommonItem;
    private final @Nullable ItemStatistic statistic;
    private final double[] common;
    private final double[] uncommon;

    BoosterKind(String displayName, String commonItem, String uncommonItem, @Nullable ItemStatistic statistic,
                double[] common, double[] uncommon) {
        this.displayName = displayName;
        this.commonItem = commonItem;
        this.uncommonItem = uncommonItem;
        this.statistic = statistic;
        this.common = common;
        this.uncommon = uncommon;
    }

    public String displayName() {
        return displayName;
    }

    public String itemId(int level) {
        return level >= UNCOMMON ? uncommonItem : commonItem;
    }

    public @Nullable ItemStatistic statistic() {
        return statistic;
    }

    public double amount(int level, BoosterSlot slot) {
        if (level < COMMON) return 0;
        return (level >= UNCOMMON ? uncommon : common)[slot.ordinal()];
    }

    public static @Nullable BoosterKind ofItem(@Nullable String itemId) {
        for (BoosterKind kind : values()) if (kind.commonItem.equals(itemId) || kind.uncommonItem.equals(itemId)) return kind;
        return null;
    }

    public static int levelOfItem(@Nullable String itemId) {
        for (BoosterKind kind : values()) {
            if (kind.commonItem.equals(itemId)) return COMMON;
            if (kind.uncommonItem.equals(itemId)) return UNCOMMON;
        }
        return 0;
    }
}
