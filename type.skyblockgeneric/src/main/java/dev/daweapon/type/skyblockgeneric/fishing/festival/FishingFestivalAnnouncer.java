package dev.daweapon.type.skyblockgeneric.fishing.festival;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;

/**
 * Sends the Fishing Festival's countdown (3, 2 and 1 SkyBlock days before it starts) and its
 * closing message. The opening message comes from the calendar event itself.
 */
public final class FishingFestivalAnnouncer {
    private static final int COUNTDOWN_DAYS = 3;

    private static boolean wasActive;
    private static int lastDaysUntilStart;

    private FishingFestivalAnnouncer() {
    }

    public static void start(Scheduler scheduler) {
        wasActive = FishingFestival.isActive();
        lastDaysUntilStart = daysUntilStart();
        scheduler.submitTask(() -> {
            tick();
            return TaskSchedule.seconds(1);
        });
    }

    private static void tick() {
        boolean active = FishingFestival.isActive();
        if (wasActive && !active) FishingFestival.announceEnd();
        wasActive = active;

        int days = active ? -1 : daysUntilStart();
        if (days != lastDaysUntilStart && days >= 1 && days <= COUNTDOWN_DAYS) {
            FishingFestival.announceCountdown(days);
        }
        lastDaysUntilStart = days;
    }

    private static int daysUntilStart() {
        long ticks = SkyBlockCalendar.ticksUntilEvent(CalendarEvent.FISHING_FESTIVAL);
        if (ticks <= 0) return -1;
        return (int) ((ticks + SkyBlockCalendar.DAY - 1) / SkyBlockCalendar.DAY);
    }
}
