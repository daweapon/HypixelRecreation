package dev.daweapon.type.skyblockgeneric.leaderboard;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;

@Getter
public enum Timeframe {
    WEEK("week", "This Week", 21L * 86_400),
    MONTH("month", "This Month", 62L * 86_400);

    private final String prefix;
    private final String label;
    private final long ttlSeconds;

    Timeframe(String prefix, String label, long ttlSeconds) {
        this.prefix = prefix;
        this.label = label;
        this.ttlSeconds = ttlSeconds;
    }

    public String windowId(LocalDate date) {
        return switch (this) {
            case WEEK -> String.format("%d-W%02d", date.get(IsoFields.WEEK_BASED_YEAR), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MONTH -> String.format("%d-%02d", date.getYear(), date.getMonthValue());
        };
    }

    public String currentWindowId() {
        return windowId(LocalDate.ofInstant(Instant.now(), ZoneOffset.UTC));
    }

    public static double gained(double current, double baseline) {
        return Math.max(0, current - baseline);
    }

    public static Timeframe fromPrefix(String prefix) {
        for (Timeframe timeframe : values()) {
            if (timeframe.prefix.equals(prefix)) return timeframe;
        }
        return null;
    }
}
