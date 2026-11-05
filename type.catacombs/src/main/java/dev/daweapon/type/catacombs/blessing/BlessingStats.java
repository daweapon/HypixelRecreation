package dev.daweapon.type.catacombs.blessing;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.blessing.BlessingSet;
import dev.daweapon.dungeons.catacombs.blessing.BlessingStat;

/** Turns the party's stacked blessings into stats: the flat part adds, the percent part multiplies. */
public final class BlessingStats {
    public static final double FLOOR_THREE_BONUS = 1.2;

    private BlessingStats() {
    }

    /** Blessings are 20% stronger from Floor III on (wiki). */
    public static double effectiveness(CatacombsFloor floor) {
        return floor.ordinal() >= CatacombsFloor.FLOOR_THREE.ordinal() ? FLOOR_THREE_BONUS : 1.0;
    }

    public static ItemStatistics of(BlessingSet blessings, double effectiveness) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        blessings.appliedStats(effectiveness).forEach((stat, applied) -> builder
                .withBase(statistic(stat), applied.flat())
                .withMultiplicativePercentage(statistic(stat), applied.percent()));
        return builder.build();
    }

    public static ItemStatistic statistic(BlessingStat stat) {
        return switch (stat) {
            case HEALTH -> ItemStatistic.HEALTH;
            case HEALTH_REGEN -> ItemStatistic.HEALTH_REGENERATION;
            case STRENGTH -> ItemStatistic.STRENGTH;
            case CRIT_DAMAGE -> ItemStatistic.CRITICAL_DAMAGE;
            case DEFENSE -> ItemStatistic.DEFENSE;
            case DAMAGE -> ItemStatistic.DAMAGE;
            case INTELLIGENCE -> ItemStatistic.INTELLIGENCE;
            case SPEED -> ItemStatistic.SPEED;
        };
    }
}
