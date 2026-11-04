package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.SharedCalendarClock;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

public final class CenturyEvent {
    public static final int CENTURY = 100;
    private static final String FORCED_KEY = "hsb:century:forced";
    private static final String LAST_KEY = "hsb:century:last";
    private static final long YEAR_MILLIS = (long) SkyBlockCalendar.YEAR * SharedCalendarClock.MILLIS_PER_UNIT;

    private static volatile @Nullable Window forced;
    private static volatile @Nullable Window last;

    private CenturyEvent() {
    }

    public record Window(String id, long start, long end) {
        public boolean contains(long now) {
            return now >= start && now < end;
        }

        public String serialize() {
            return id + "|" + start + "|" + end;
        }

        public static @Nullable Window parse(@Nullable String raw) {
            if (raw == null || raw.isBlank()) return null;
            String[] parts = raw.split("\\|");
            if (parts.length != 3) return null;
            try {
                return new Window(parts[0], Long.parseLong(parts[1]), Long.parseLong(parts[2]));
            } catch (NumberFormatException exception) {
                return null;
            }
        }
    }

    public static @Nullable Window active() {
        long now = System.currentTimeMillis();
        Window override = forced;
        if (override != null) return override.contains(now) ? override : null;
        Window natural = naturalWindow(now);
        return natural != null && natural.contains(now) ? natural : null;
    }

    public static boolean isActive() {
        return active() != null;
    }

    public static @Nullable Window latest() {
        Window current = active();
        return current != null ? current : last;
    }

    static @Nullable Window naturalWindow(long now) {
        int year = SkyBlockCalendar.getYear();
        if (year % CENTURY != 0) return null;
        long intoYear = SkyBlockCalendar.getElapsed() % SkyBlockCalendar.YEAR;
        long start = now - intoYear * SharedCalendarClock.MILLIS_PER_UNIT;
        return new Window("year" + year, start, start + YEAR_MILLIS);
    }

    public static int currentYear() {
        return SkyBlockCalendar.getYear();
    }

    public static boolean forceStart(long durationMillis) {
        if (SharedData.jedisPool() == null) return false;
        long now = System.currentTimeMillis();
        Window window = new Window("forced" + now, now, now + durationMillis);
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(FORCED_KEY, window.serialize());
            jedis.set(LAST_KEY, window.serialize());
        }
        forced = window;
        last = window;
        return true;
    }

    public static boolean forceEnd() {
        Window current = active();
        if (current == null || SharedData.jedisPool() == null) return false;
        Window ended = new Window(current.id(), current.start(), System.currentTimeMillis());
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(FORCED_KEY, ended.serialize());
            jedis.set(LAST_KEY, ended.serialize());
        }
        forced = ended;
        last = ended;
        return true;
    }

    public static boolean clearOverride() {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.del(FORCED_KEY);
        }
        forced = null;
        return true;
    }

    static void refresh() {
        if (SharedData.jedisPool() == null) return;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            forced = Window.parse(jedis.get(FORCED_KEY));
            Window natural = naturalWindow(System.currentTimeMillis());
            Window stored = Window.parse(jedis.get(LAST_KEY));
            if (forced == null && natural != null && (stored == null || !stored.id().equals(natural.id()))) {
                jedis.set(LAST_KEY, natural.serialize());
                stored = natural;
            }
            last = stored;
        } catch (Exception exception) {
            Logger.error(exception, "Failed to read the Raffle of the Century window");
        }
    }
}
