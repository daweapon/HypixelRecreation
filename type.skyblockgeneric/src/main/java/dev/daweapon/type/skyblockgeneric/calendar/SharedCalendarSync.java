package dev.daweapon.type.skyblockgeneric.calendar;

import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.CalendarState;
import dev.daweapon.commons.skyblock.SharedCalendarClock;
import dev.daweapon.type.generic.redis.service.AdminServerStatusHandler;
import org.tinylog.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Keeps this server's SkyBlockCalendar on the Redis epoch every server shares. The first server to
// start seeds the epoch from its persisted date so switching to the shared clock never jumps the
// calendar; a staff time skip republishes it and the others follow on their next refresh.
public final class SharedCalendarSync {
    private static final long REFRESH_SECONDS = 5;
    private static final ScheduledExecutorService REFRESHER = Executors.newSingleThreadScheduledExecutor(
            Thread.ofPlatform().daemon().name("calendar-epoch-refresh").factory());

    private SharedCalendarSync() {}

    public static void start() {
        AdminServerStatusHandler.setCalendarSource(SharedCalendarSync::snapshot);
        REFRESHER.execute(SharedCalendarSync::claim);
        REFRESHER.scheduleWithFixedDelay(SharedCalendarSync::refresh, REFRESH_SECONDS, REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    static void publish(long epochMillis) {
        try {
            SharedCalendarClock.publish(epochMillis);
        } catch (Exception e) {
            Logger.warn(e, "Failed to publish the shared SkyBlock calendar epoch");
        }
    }

    private static void claim() {
        try {
            long proposed = SharedCalendarClock.epochFor(SkyBlockCalendar.getElapsed(), System.currentTimeMillis());
            SkyBlockCalendar.useSharedEpoch(SharedCalendarClock.claim(proposed));
        } catch (Exception e) {
            Logger.warn(e, "Shared SkyBlock calendar unavailable, keeping this server's own clock for now");
        }
    }

    private static void refresh() {
        try {
            Long epoch = SharedCalendarClock.read();
            if (epoch == null) {
                claim();
                return;
            }
            SkyBlockCalendar.useSharedEpoch(epoch);
        } catch (Exception e) {
            Logger.warn(e, "Failed to refresh the shared SkyBlock calendar epoch");
        }
    }

    private static CalendarState snapshot() {
        long elapsed = SkyBlockCalendar.getElapsed();
        return new CalendarState(elapsed, (int) (elapsed / SkyBlockCalendar.YEAR) + 1, SkyBlockCalendar.getMonth(elapsed),
                SkyBlockCalendar.getMonthName(elapsed), SkyBlockCalendar.getDay(elapsed), SkyBlockCalendar.getHour(elapsed),
                SkyBlockCalendar.isShared());
    }
}
