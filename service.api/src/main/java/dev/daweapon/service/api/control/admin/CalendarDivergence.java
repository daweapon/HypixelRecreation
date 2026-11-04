package dev.daweapon.service.api.control.admin;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

// Servers answer a status broadcast a few hundred milliseconds apart and pick up a staff time skip
// on their next epoch refresh, so a small spread is normal; beyond the tolerance they disagree.
public final class CalendarDivergence {
    public static final long YEAR = 8_928_000L;
    public static final long MONTH = 744_000L;
    public static final long DAY = 24_000L;
    public static final long TOLERANCE = 200L;

    private static final List<String> MONTH_NAMES = List.of("Early Spring", "Spring", "Late Spring", "Early Summer",
            "Summer", "Late Summer", "Early Autumn", "Autumn", "Late Autumn", "Early Winter", "Winter", "Late Winter");

    public record Assessment(long spread, boolean diverged, int reporting) {}

    public record SkyBlockDate(long elapsed, int year, String monthName, int day, int hour) {}

    private CalendarDivergence() {}

    public static Assessment assess(Collection<Long> reported, @Nullable Long reference) {
        if (reported.isEmpty()) return new Assessment(0L, false, 0);
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long value : reported) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        if (reference != null) {
            min = Math.min(min, reference);
            max = Math.max(max, reference);
        }
        long spread = max - min;
        return new Assessment(spread, spread > TOLERANCE, reported.size());
    }

    public static SkyBlockDate describe(long elapsed) {
        int year = (int) (elapsed / YEAR) + 1;
        int month = (int) ((elapsed / MONTH) % 12);
        int day = (int) ((elapsed / DAY) % 31) + 1;
        int sinceSix = (int) ((elapsed % DAY) - 6000);
        if (sinceSix < 0) sinceSix += (int) DAY;
        int hour = (6 + sinceSix / 1000) % 24;
        return new SkyBlockDate(elapsed, year, MONTH_NAMES.get(month), day, hour);
    }
}
