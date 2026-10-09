package net.swofty.type.kuudra.fight;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Kuudra's last quarter in the lair as real health (research K1 3.5): the final 25,000 of his 100,000 points are
 * 240M, so a hit's damage is divided by 9,600 to take points off, and the hit title shows what is left of 240M.
 */
public final class LairHealth {
    private LairHealth() {
    }

    public static double points(double damage, double damagePerPoint) {
        return damagePerPoint <= 0 ? 0 : Math.max(0, damage) / damagePerPoint;
    }

    /** The Infernal capture's hit subtitle, e.g. "☠ 239.9M/240M❤". */
    public static String title(double pointsLeft, double damagePerPoint, double lairHealth) {
        return "<d>☠ <a>" + shortNumber(Math.max(0, pointsLeft) * damagePerPoint) + "<f>/<a>" + shortNumber(lairHealth) + "<c>❤";
    }

    public static String shortNumber(double value) {
        if (value >= 1_000_000_000) return trim(value / 1_000_000_000) + "B";
        if (value >= 1_000_000) return trim(value / 1_000_000) + "M";
        if (value >= 1_000) return trim(value / 1_000) + "k";
        return trim(value);
    }

    private static String trim(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
