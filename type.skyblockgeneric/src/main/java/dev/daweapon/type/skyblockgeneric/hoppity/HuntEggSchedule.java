package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.region.IslandType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public final class HuntEggSchedule {
    public static final int HUNT_DAYS = 93;
    public static final int SPAWNS_PER_DAY = EggTimeOfDay.values().length;
    public static final int SPAWNS_PER_HUNT = HUNT_DAYS * SPAWNS_PER_DAY;
    public static final int MAX_ACTIVE = 6;
    public static final long HUNT_TICKS = (long) HUNT_DAYS * SkyBlockCalendar.DAY;
    private static final int RECENT_WINDOW = MAX_ACTIVE - 1;
    private static final Map<String, List<Integer>> ORDERS = new ConcurrentHashMap<>();

    private HuntEggSchedule() {
    }

    public static boolean isHuntActive(long elapsed) {
        return elapsed >= 0 && elapsed % SkyBlockCalendar.YEAR < HUNT_TICKS;
    }

    public static int yearOf(long elapsed) {
        return (int) (elapsed / SkyBlockCalendar.YEAR) + 1;
    }

    public static String spawnId(int year, int spawnNumber) {
        return year + ":" + spawnNumber;
    }

    public static int latestSpawnNumber(long elapsed) {
        long inYear = elapsed % SkyBlockCalendar.YEAR;
        if (inYear >= HUNT_TICKS) return SPAWNS_PER_HUNT - 1;
        long day = inYear / SkyBlockCalendar.DAY;
        long tick = inYear % SkyBlockCalendar.DAY;
        int passed = 0;
        for (EggTimeOfDay time : EggTimeOfDay.values()) {
            if (tick >= time.tickInDay()) passed++;
        }
        return (int) (day * SPAWNS_PER_DAY + passed) - 1;
    }

    public static EggTimeOfDay timeOfDay(int spawnNumber) {
        return EggTimeOfDay.values()[spawnNumber % SPAWNS_PER_DAY];
    }

    public static HoppityEggType eggType(int spawnNumber) {
        return timeOfDay(spawnNumber).typeOnDay(spawnNumber / SPAWNS_PER_DAY);
    }

    public static long spawnTickInYear(int spawnNumber) {
        return (long) (spawnNumber / SPAWNS_PER_DAY) * SkyBlockCalendar.DAY + timeOfDay(spawnNumber).tickInDay();
    }

    public static List<Integer> activeSpawnNumbers(long elapsed) {
        List<Integer> active = new ArrayList<>();
        if (!isHuntActive(elapsed)) return active;
        int latest = latestSpawnNumber(elapsed);
        for (int number = Math.max(0, latest - MAX_ACTIVE + 1); number <= latest; number++) {
            active.add(number);
        }
        return active;
    }

    public static ActiveEgg egg(int year, int spawnNumber, IslandType island, EggLocationCatalogue catalogue) {
        List<EggLocation> locations = catalogue.locations(island);
        if (locations.isEmpty() || spawnNumber < 0 || spawnNumber >= SPAWNS_PER_HUNT) return null;
        int index = order(year, island, locations.size()).get(spawnNumber);
        return new ActiveEgg(spawnId(year, spawnNumber), year, spawnNumber, eggType(spawnNumber), island, locations.get(index));
    }

    public static List<ActiveEgg> activeEggs(long elapsed, IslandType island, EggLocationCatalogue catalogue) {
        List<ActiveEgg> eggs = new ArrayList<>();
        int year = yearOf(elapsed);
        for (int number : activeSpawnNumbers(elapsed)) {
            ActiveEgg egg = egg(year, number, island, catalogue);
            if (egg != null) eggs.add(egg);
        }
        return eggs;
    }

    private static List<Integer> order(int year, IslandType island, int locationCount) {
        return ORDERS.computeIfAbsent(year + ":" + island.name() + ":" + locationCount, key -> buildOrder(year, island, locationCount));
    }

    private static List<Integer> buildOrder(int year, IslandType island, int locationCount) {
        Random random = new Random(year * 1_000_003L + island.ordinal() * 7_919L + locationCount);
        List<Integer> order = new ArrayList<>(SPAWNS_PER_HUNT);
        while (order.size() < SPAWNS_PER_HUNT) {
            List<Integer> block = new ArrayList<>(locationCount);
            for (int i = 0; i < locationCount; i++) block.add(i);
            for (int attempt = 0; attempt < 64; attempt++) {
                java.util.Collections.shuffle(block, random);
                if (!clashesWithRecent(order, block)) break;
            }
            order.addAll(block);
        }
        return List.copyOf(order.subList(0, SPAWNS_PER_HUNT));
    }

    private static boolean clashesWithRecent(List<Integer> order, List<Integer> block) {
        int recent = Math.min(RECENT_WINDOW, order.size());
        for (int i = 0; i < Math.min(RECENT_WINDOW, block.size()); i++) {
            for (int j = order.size() - recent; j < order.size(); j++) {
                if (order.get(j).equals(block.get(i)) && i + (order.size() - j) <= RECENT_WINDOW) return true;
            }
        }
        return false;
    }
}
