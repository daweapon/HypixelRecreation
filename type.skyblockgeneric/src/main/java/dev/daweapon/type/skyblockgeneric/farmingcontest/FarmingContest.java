package dev.daweapon.type.skyblockgeneric.farmingcontest;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * One of Jacob's Farming Contests. A contest starts on the 2nd SkyBlock day and every 3rd day
 * after it, lasts one SkyBlock day (20 real minutes) and features three different crops. The
 * crops come from a random generator seeded with the contest's year and index, so every server
 * agrees on them without sharing state.
 */
public record FarmingContest(int year, int index, List<ContestCrop> crops) {
    public static final int CROPS_PER_CONTEST = 3;
    public static final long INTERVAL = 3L * SkyBlockCalendar.DAY;
    public static final long FIRST_START_IN_YEAR = SkyBlockCalendar.DAY;
    public static final long DURATION = SkyBlockCalendar.DAY;
    public static final int PER_YEAR = (int) ((SkyBlockCalendar.YEAR - FIRST_START_IN_YEAR + INTERVAL - 1) / INTERVAL);

    public static FarmingContest of(int year, int index) {
        Random random = new Random(year * 1_000L + index);
        List<ContestCrop> pool = new ArrayList<>(Arrays.asList(ContestCrop.values()));
        Collections.shuffle(pool, random);
        List<ContestCrop> picked = new ArrayList<>(pool.subList(0, CROPS_PER_CONTEST));
        picked.sort(Comparator.comparingInt(Enum::ordinal));
        return new FarmingContest(year, index, List.copyOf(picked));
    }

    public static Optional<FarmingContest> fromId(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) return Optional.empty();
        try {
            return Optional.of(of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static long startInYear(int index) {
        return FIRST_START_IN_YEAR + index * INTERVAL;
    }

    public static List<Long> startTimesInYear() {
        List<Long> times = new ArrayList<>(PER_YEAR);
        for (int index = 0; index < PER_YEAR; index++) times.add(startInYear(index));
        return times;
    }

    public static Optional<FarmingContest> activeAt(long elapsed) {
        FarmingContest latest = latestStartedAt(elapsed);
        if (latest == null || elapsed >= latest.endElapsed()) return Optional.empty();
        return Optional.of(latest);
    }

    public static Optional<FarmingContest> active() {
        return activeAt(SkyBlockCalendar.getElapsed());
    }

    public static List<FarmingContest> upcoming(int count) {
        long elapsed = SkyBlockCalendar.getElapsed();
        List<FarmingContest> result = new ArrayList<>(count);
        FarmingContest latest = latestStartedAt(elapsed);
        FarmingContest next = latest == null ? of(yearOf(elapsed), 0) : latest.next();
        while (result.size() < count) {
            result.add(next);
            next = next.next();
        }
        return result;
    }

    private static FarmingContest latestStartedAt(long elapsed) {
        int year = yearOf(elapsed);
        long inYear = elapsed - (year - 1L) * SkyBlockCalendar.YEAR;
        if (inYear < FIRST_START_IN_YEAR) {
            return year <= 1 ? null : of(year - 1, PER_YEAR - 1);
        }
        int index = (int) Math.min(PER_YEAR - 1, (inYear - FIRST_START_IN_YEAR) / INTERVAL);
        return of(year, index);
    }

    private static int yearOf(long elapsed) {
        return (int) (elapsed / SkyBlockCalendar.YEAR) + 1;
    }

    public FarmingContest next() {
        return index + 1 >= PER_YEAR ? of(year + 1, 0) : of(year, index + 1);
    }

    public String id() {
        return year + ":" + index;
    }

    public long startElapsed() {
        return (year - 1L) * SkyBlockCalendar.YEAR + startInYear(index);
    }

    public long endElapsed() {
        return startElapsed() + DURATION;
    }

    public boolean hasEnded() {
        return SkyBlockCalendar.getElapsed() >= endElapsed();
    }

    public boolean features(ContestCrop crop) {
        return crops.contains(crop);
    }

    public ContestCrop anitaCrop() {
        return crops.get(new Random(year * 1_000L + index + 7_919L).nextInt(crops.size()));
    }

    public String leaderboardKey(ContestCrop crop) {
        return "jacob_contest:" + year + ":" + index + ":" + crop.name();
    }

    public String dateLabel() {
        long start = startElapsed();
        return SkyBlockCalendar.getMonthName(start) + " " + StringUtility.ntify(SkyBlockCalendar.getDay(start))
                + ", Year " + year;
    }
}
