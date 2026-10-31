package dev.daweapon.type.skyblockgeneric.pity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class PityProgress {
    public static final String UNAVAILABLE = "<c>Not available yet";

    private PityProgress() {
    }

    public static String line(String label, long current, long max) {
        long shown = Math.max(0, Math.min(current, max));
        return "<7>" + label + ": " + colour(shown >= max) + format(shown) + "<7>/<a>" + format(max);
    }

    public static String percentLine(String label, double fraction) {
        double percent = Math.max(0, Math.min(1, fraction)) * 100;
        return "<7>" + label + ": " + colour(percent >= 100) + percent(percent) + "%<7>/<a>100%";
    }

    public static String format(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    public static String percent(double percent) {
        return new BigDecimal(percent).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static String colour(boolean complete) {
        return complete ? "<a>" : "<c>";
    }
}
