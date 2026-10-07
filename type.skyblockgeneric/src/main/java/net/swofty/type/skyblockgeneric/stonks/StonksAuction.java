package net.swofty.type.skyblockgeneric.stonks;

import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.Optional;

/**
 * Richard's Stonks Auction for one SkyBlock month. While Diaz's Stock Exchange perk is active a new
 * auction runs every month and closes when the month ends, so every server agrees on it from the
 * calendar alone.
 */
public record StonksAuction(int year, int month) {
    public static final long MINIMUM_BID = 2_000_000L;
    private static final long MILLIS_PER_CALENDAR_UNIT = 50L;

    public static StonksAuction current() {
        long elapsed = SkyBlockCalendar.getElapsed();
        return new StonksAuction((int) (elapsed / SkyBlockCalendar.YEAR) + 1, SkyBlockCalendar.getMonth(elapsed));
    }

    public static Optional<StonksAuction> fromId(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) return Optional.empty();
        try {
            return Optional.of(new StonksAuction(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public String id() {
        return year + ":" + month;
    }

    public String leaderboardKey() {
        return "stonks_auction:" + id();
    }

    public long endElapsed() {
        return (year - 1L) * SkyBlockCalendar.YEAR + (long) month * SkyBlockCalendar.MONTH;
    }

    public boolean hasEnded() {
        return SkyBlockCalendar.getElapsed() >= endElapsed();
    }

    public long millisRemaining() {
        return Math.max(0, endElapsed() - SkyBlockCalendar.getElapsed()) * MILLIS_PER_CALENDAR_UNIT;
    }

    public String label() {
        return SkyBlockCalendar.getMonthName(month) + ", Year " + year;
    }
}
