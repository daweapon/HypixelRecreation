package net.swofty.type.skyblockgeneric.accessory.power;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Markup for stat lines in Maxwell's menus and on power stones, e.g. {@code +69.42 Strength}. */
public final class StatText {
    private StatText() {
    }

    public static String number(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static String line(ItemStatistic statistic, double value) {
        return "<color:" + statistic.getDisplayColor().asHexString() + ">" + (value >= 0 ? "+" : "")
                + number(value) + (statistic.getIsPercentage() ? "%" : "") + " " + statistic.getDisplayName();
    }

    public static List<String> lines(Map<ItemStatistic, Double> statistics) {
        List<String> lines = new ArrayList<>();
        statistics.forEach((statistic, value) -> lines.add(line(statistic, value)));
        return lines;
    }

    public static List<String> lines(ItemStatistics statistics) {
        List<String> lines = new ArrayList<>();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            double value = statistics.getOverall(statistic);
            if (value != 0) lines.add(line(statistic, value));
        }
        return lines;
    }
}
