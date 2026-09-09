package net.swofty.type.skyblockgeneric.calendar;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.commands.MinionGenerationCommand;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Tracks the in-game SkyBlock date/time (year/month/day/hour) and fires calendar events as time
// elapses, plus helpers for looking up upcoming event occurrences.
public final class SkyBlockCalendar {
    private static final String DAY_SYMBOL = "☀";
    private static final String NIGHT_SYMBOL = "☽";

    public static final int YEAR = 8928000;
    public static final int MONTH = 744000;
    public static final int DAY = 24000;
    public static final int HOUR = 1000;

    public static final int INTEREST_INTERVAL = 31;
    public static final double INTEREST_RATE = 0.02;

    private static final List<String> MONTH_NAMES = Arrays.asList("Early Spring", "Spring",
            "Late Spring", "Early Summer", "Summer", "Late Summer", "Early Autumn",
            "Autumn", "Late Autumn", "Early Winter", "Winter", "Late Winter");

    private static final Set<String> firedEvents = new HashSet<>();

    @Getter
    @Setter
    private static long elapsed = 0L;

    public static void tick(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            elapsed += 10L;
            checkForEvents(getElapsed() % YEAR);
            return TaskSchedule.tick(10);
        });
    }

    public static Integer getHoursUntilNextInterest() {
        // Interest happens every 31 hours
        return (int) (INTEREST_INTERVAL - (getElapsed() % DAY) / 1000);
    }

    public static Long getDifferenceInHours(long time) {
        return (getElapsed() - time) / HOUR;
    }

    public static int getYear() {
        return (int) (getElapsed() / YEAR) + 1;
    }

    public static int getMonth() {
        return ((int) (getElapsed() / MONTH) % 12) + 1;
    }

    public static int getMonth(long elapsed) {
        return ((int) (elapsed / MONTH) % 12) + 1;
    }

    public static int getDay() {
        return ((int) (getElapsed() / DAY) % 31) + 1;
    }

    public static int getHour() {
        int currentTime = (int) ((getElapsed() % DAY) - 6000);
        if (currentTime < 0) currentTime += DAY; // Adjust if before 6:00AM
        int hours = 6 + (currentTime / 1000); // 1 hour = 1000 units in elapsed time
        if (hours >= 24) hours -= 24; // Adjust if the time is past midnight
        return hours;
    }

    public static String getMonthName(int month) {
        if (month < 1 || month > 12)
            return "Unknown Month";
        return MONTH_NAMES.get(month - 1);
    }

    public static String getMonthName() {
        return getMonthName(getMonth());
    }

    public static void checkForEvents(Long time) {
        List<CalendarEvent> eventsAtTime = CalendarEvent.getCurrentEvents(time);
        int year = getYear();
        Set<String> stillRunning = new HashSet<>(eventsAtTime.size());

        for (CalendarEvent event : eventsAtTime) {
            stillRunning.add(event.id());
            if (firedEvents.add(event.id())) {
                event.action().accept(time, year);
            }
        }

        firedEvents.retainAll(stillRunning);
    }

    public static List<CalendarEvent> getCurrentEvents() {
        return CalendarEvent.getCurrentEvents(getElapsed() % YEAR);
    }

    /**
     * A record to hold information about upcoming events.
     * @param timeUntilBegin <b>ticks</b> until the event begins
     * @param duration duration of the event
     * @param year the year the event will occur
     */
    public record EventInfo(long timeUntilBegin, Duration duration, int year) {}

    public static Map<EventInfo, CalendarEvent> getEventsWithDurationUntil(int amount) {
        return getEventsWithDurationUntil(amount, false);
    }

    /**
     * @param halveDarkAuction if true, only every other upcoming Dark Auction occurrence is
     *                         included, leaving more room for rarer events in the result.
     */
    public static Map<EventInfo, CalendarEvent> getEventsWithDurationUntil(int amount, boolean halveDarkAuction) {
        return collectUpcomingEvents(amount, List.of(), halveDarkAuction);
    }

    public static long ticksUntilEvent(CalendarEvent event) {
        long currentElapsed = getElapsed();
        int currentYear = getYear();

        for (int yearsAhead = 0; yearsAhead <= 100; yearsAhead++) {
            int targetYear = currentYear + yearsAhead;
            long yearStartElapsed = (long) (targetYear - 1) * YEAR;

            List<Long> sortedTimes = new ArrayList<>(event.times());
            Collections.sort(sortedTimes);

            for (Long eventTime : sortedTimes) {
                long eventElapsed = yearStartElapsed + eventTime;

                if (eventElapsed > currentElapsed) {
                    return eventElapsed - currentElapsed;
                }
            }
        }

        return -1;
    }

    private record UpcomingOccurrence(long eventElapsed, int year, CalendarEvent event) {}

    public static Map<EventInfo, CalendarEvent> getEventsWithDurationUntilSkipSpecific(int amount, List<CalendarEvent> skip) {
        return collectUpcomingEvents(amount, skip, false);
    }

    private static Map<EventInfo, CalendarEvent> collectUpcomingEvents(int amount, List<CalendarEvent> skip, boolean halveDarkAuction) {
        long currentElapsed = getElapsed();
        int currentYear = getYear();

        List<UpcomingOccurrence> occurrences = new ArrayList<>();
        int yearsAhead = 0;

        // When halving Dark Auction we need to gather extra raw occurrences up front, since half
        // of them get filtered back out below before we can tell if `amount` results were reached.
        int rawTarget = halveDarkAuction ? amount * 2 : amount;

        // Gather occurrences year by year until we have enough to satisfy `amount` once sorted
        // chronologically. Gathering per-event-then-truncating (as before) let a frequent event
        // like Dark Auction (~40 occurrences/year) exhaust the whole quota before rarer events
        // ever got a chance to appear.
        while (occurrences.size() < rawTarget) {
            int targetYear = currentYear + yearsAhead;
            long yearStartElapsed = (long) (targetYear - 1) * YEAR;

            for (CalendarEvent e : CalendarEvent.getAllEvents()) {
                if (skip.contains(e)) continue;

                for (Long eventTime : e.times()) {
                    long eventElapsed = yearStartElapsed + eventTime;

                    // Skip events that have already passed (including currently ongoing ones that started)
                    if (eventElapsed <= currentElapsed) {
                        continue;
                    }

                    occurrences.add(new UpcomingOccurrence(eventElapsed, targetYear, e));
                }
            }
            yearsAhead++;

            // Safety check to prevent infinite loops
            if (yearsAhead > 100) break;
        }

        occurrences.sort(Comparator.comparingLong(UpcomingOccurrence::eventElapsed));

        // Cap how many Dark Auction entries can occupy the result instead of skipping every other
        // one chronologically - skipping alone just spaces them further apart while something still
        // backfills every slot, leaving the visible count unchanged. Capping and refusing to
        // backfill past it means half the slots are genuinely left empty (or taken by a rarer event
        // that happens to fall in the gap), which is what "see half as many" actually means visually.
        int darkAuctionCap = halveDarkAuction ? (amount + 1) / 2 : Integer.MAX_VALUE;
        int darkAuctionCount = 0;

        Map<EventInfo, CalendarEvent> result = new LinkedHashMap<>();
        for (UpcomingOccurrence occurrence : occurrences) {
            if (result.size() >= amount) break;

            if (occurrence.event() == CalendarEvent.DARK_AUCTION) {
                if (darkAuctionCount >= darkAuctionCap) continue;
                darkAuctionCount++;
            }

            long timeUntilBegin = occurrence.eventElapsed() - currentElapsed;
            EventInfo info = new EventInfo(timeUntilBegin, occurrence.event().duration(), occurrence.year());
            result.put(info, occurrence.event());
        }

        return result;
    }

    public static String getMonthName(long elapsed) {
        return getMonthName((int) ((elapsed / MONTH) % 12) + 1);
    }

    public static int getDay(long elapsed) {
        return ((int) (elapsed / DAY) % 31) + 1;
    }

    public static Text getDisplay(long elapsed) {
        boolean isDaytime = true;
        int currentTime = (int) ((elapsed % DAY) - 6000);
        if (currentTime < 0) currentTime += DAY;
        int hours = 6 + (currentTime / 1000);
        int minutes = (currentTime % 1000) * 60 / 1000; // Corrected minutes calculation
        String formattedMinutes = String.format("%02d", minutes);
        if (hours >= 24) hours -= 24;
        if (hours < 6 || hours >= 20) isDaytime = false;

        String timePeriod = hours >= 12 ? "pm" : "am";
        hours = hours > 12 ? hours - 12 : (hours == 0 ? 12 : hours);

        Text symbol = isDaytime ? Text.of("<e>{}", DAY_SYMBOL) : Text.of("<b>{}", NIGHT_SYMBOL);

        return Text.of("{}:{}{} {}", hours, formattedMinutes, timePeriod, symbol)
                .appendIf(MinionGenerationCommand.divisionFactor != 1,
                        " <c>(Minion Speed: {})", MinionGenerationCommand.divisionFactor);
    }
}
