package dev.daweapon.service.control.backup;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 * When scheduled backups run: never, once a day at a wall-clock time, or every fixed interval.
 *
 * Written as a short spec string so it round-trips through an env var, the settings file and
 * the app unchanged: {@code off}, {@code daily}, {@code daily@04:30}, {@code every 6h},
 * {@code every 90m}.
 */
public record BackupSchedule(Kind kind, LocalTime at, Duration every) {
    public enum Kind {
        OFF,
        DAILY,
        INTERVAL
    }

    public static final LocalTime DEFAULT_TIME = LocalTime.of(4, 0);
    public static final BackupSchedule DEFAULT = daily(DEFAULT_TIME);
    public static final Duration MINIMUM_INTERVAL = Duration.ofMinutes(15);

    public static BackupSchedule off() {
        return new BackupSchedule(Kind.OFF, null, null);
    }

    public static BackupSchedule daily(LocalTime at) {
        return new BackupSchedule(Kind.DAILY, at, null);
    }

    public static BackupSchedule every(Duration interval) {
        if (interval.compareTo(MINIMUM_INTERVAL) < 0) {
            throw new IllegalArgumentException("Backup interval must be at least "
                    + MINIMUM_INTERVAL.toMinutes() + " minutes");
        }
        return new BackupSchedule(Kind.INTERVAL, null, interval);
    }

    public static BackupSchedule parse(String spec) {
        if (spec == null || spec.isBlank()) {
            return DEFAULT;
        }
        String value = spec.trim().toLowerCase(Locale.ROOT);
        if (value.equals("off") || value.equals("none") || value.equals("disabled")) {
            return off();
        }
        if (value.equals("daily")) {
            return DEFAULT;
        }
        if (value.startsWith("daily@")) {
            try {
                return daily(LocalTime.parse(value.substring("daily@".length()).trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad time in '" + spec + "' - expected daily@HH:MM");
            }
        }
        String interval = value.startsWith("every") ? value.substring("every".length()).trim() : value;
        return every(parseInterval(interval, spec));
    }

    private static Duration parseInterval(String value, String original) {
        if (value.length() < 2) {
            throw new IllegalArgumentException("Unrecognised schedule '" + original
                    + "' - expected off, daily, daily@HH:MM or every <n>h|m");
        }
        char unit = value.charAt(value.length() - 1);
        long amount;
        try {
            amount = Long.parseLong(value.substring(0, value.length() - 1).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unrecognised schedule '" + original
                    + "' - expected off, daily, daily@HH:MM or every <n>h|m");
        }
        return switch (unit) {
            case 'h' -> Duration.ofHours(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Unrecognised interval unit in '" + original
                    + "' - use h, m or d");
        };
    }

    public String spec() {
        return switch (kind) {
            case OFF -> "off";
            case DAILY -> "daily@" + at;
            case INTERVAL -> every.toMinutes() % 60 == 0
                    ? "every " + every.toHours() + "h"
                    : "every " + every.toMinutes() + "m";
        };
    }

    /** The first run strictly after {@code after}, or null when backups are off. */
    public Instant next(Instant after, ZoneId zone) {
        return switch (kind) {
            case OFF -> null;
            case INTERVAL -> after.plus(every);
            case DAILY -> {
                ZonedDateTime local = after.atZone(zone);
                ZonedDateTime candidate = ZonedDateTime.of(local.toLocalDate(), at, zone);
                if (!candidate.toInstant().isAfter(after)) {
                    candidate = ZonedDateTime.of(local.toLocalDate().plusDays(1), at, zone);
                }
                yield candidate.toInstant();
            }
        };
    }
}
