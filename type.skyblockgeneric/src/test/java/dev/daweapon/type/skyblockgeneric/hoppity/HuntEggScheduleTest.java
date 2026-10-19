package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import dev.daweapon.type.skyblockgeneric.region.IslandType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntEggScheduleTest {
    private static final EggLocationCatalogue CATALOGUE = EggLocationCatalogue.load(new File("../configuration/skyblock/hoppity/egg_locations.yml"));
    private static final long DAY = SkyBlockCalendar.DAY;
    private static final long YEAR = SkyBlockCalendar.YEAR;

    @Test
    void catalogueHoldsTheWikiLocations() {
        assertEquals(17, CATALOGUE.locations(IslandType.HUB).size());
        int total = 0;
        for (IslandType island : CATALOGUE.islands()) {
            total += CATALOGUE.locations(island).size();
            if (island != IslandType.HUB) assertEquals(15, CATALOGUE.locations(island).size(), island.name());
        }
        assertEquals(197, total);
        assertEquals(13, CATALOGUE.islands().size());
    }

    @Test
    void huntRunsForTheFirstThreeMonthsOfTheYear() {
        assertTrue(HuntEggSchedule.isHuntActive(0));
        assertTrue(HuntEggSchedule.isHuntActive(3 * SkyBlockCalendar.MONTH - 1));
        assertFalse(HuntEggSchedule.isHuntActive(3 * SkyBlockCalendar.MONTH));
        assertTrue(HuntEggSchedule.isHuntActive(YEAR + 5));
        assertEquals(279, HuntEggSchedule.SPAWNS_PER_HUNT);
    }

    @Test
    void threeEggsSpawnEachDayAt7am2pmAnd9pm() {
        assertEquals(-1, HuntEggSchedule.latestSpawnNumber(6_999));
        assertEquals(0, HuntEggSchedule.latestSpawnNumber(7_000));
        assertEquals(1, HuntEggSchedule.latestSpawnNumber(14_000));
        assertEquals(2, HuntEggSchedule.latestSpawnNumber(21_000));
        assertEquals(2, HuntEggSchedule.latestSpawnNumber(DAY + 6_999));
        assertEquals(3, HuntEggSchedule.latestSpawnNumber(DAY + 7_000));
        assertEquals(278, HuntEggSchedule.latestSpawnNumber(3 * SkyBlockCalendar.MONTH - 1));
        assertEquals(7_000, HuntEggSchedule.spawnTickInYear(0));
        assertEquals(DAY + 14_000, HuntEggSchedule.spawnTickInYear(4));
    }

    @Test
    void atMostSixEggsAreActiveAndTheOldestDropsOut() {
        assertEquals(List.of(0), HuntEggSchedule.activeSpawnNumbers(7_000));
        assertEquals(List.of(0, 1, 2), HuntEggSchedule.activeSpawnNumbers(21_000));
        assertEquals(List.of(0, 1, 2, 3, 4, 5), HuntEggSchedule.activeSpawnNumbers(DAY + 21_000));
        assertEquals(List.of(1, 2, 3, 4, 5, 6), HuntEggSchedule.activeSpawnNumbers(2 * DAY + 7_000));
        assertTrue(HuntEggSchedule.activeSpawnNumbers(100 * DAY).isEmpty());
    }

    @Test
    void anEggIsTheSameOnEveryServerAndNeverClashesWithAnotherActiveEgg() {
        for (IslandType island : CATALOGUE.islands()) {
            for (int year = 1; year <= 3; year++) {
                for (int number = 0; number < HuntEggSchedule.SPAWNS_PER_HUNT; number++) {
                    ActiveEgg first = HuntEggSchedule.egg(year, number, island, CATALOGUE);
                    ActiveEgg second = HuntEggSchedule.egg(year, number, island, EggLocationCatalogue.load(new File("../configuration/skyblock/hoppity/egg_locations.yml")));
                    assertEquals(first.location(), second.location());
                    Set<Integer> window = new HashSet<>();
                    for (int back = 0; back < HuntEggSchedule.MAX_ACTIVE && number - back >= 0; back++) {
                        assertTrue(window.add(HuntEggSchedule.egg(year, number - back, island, CATALOGUE).location().index()),
                                island + " year " + year + " spawn " + number);
                    }
                }
            }
        }
    }

    @Test
    void everyLocationOfAnIslandComesUpDuringOneHunt() {
        for (IslandType island : CATALOGUE.islands()) {
            Set<Integer> seen = new HashSet<>();
            for (int number = 0; number < HuntEggSchedule.SPAWNS_PER_HUNT; number++) {
                seen.add(HuntEggSchedule.egg(1, number, island, CATALOGUE).location().index());
            }
            assertEquals(CATALOGUE.locations(island).size(), seen.size(), island.name());
        }
    }

    @Test
    void eggTypesAlternateByDayAndHitmanIsNeverScheduled() {
        assertEquals(HoppityEggType.BREAKFAST, HuntEggSchedule.eggType(0));
        assertEquals(HoppityEggType.LUNCH, HuntEggSchedule.eggType(1));
        assertEquals(HoppityEggType.DINNER, HuntEggSchedule.eggType(2));
        assertEquals(HoppityEggType.BRUNCH, HuntEggSchedule.eggType(3));
        assertEquals(HoppityEggType.DEJEUNER, HuntEggSchedule.eggType(4));
        assertEquals(HoppityEggType.SUPPER, HuntEggSchedule.eggType(5));
        for (int number = 0; number < HuntEggSchedule.SPAWNS_PER_HUNT; number++) {
            assertFalse(HuntEggSchedule.eggType(number) == HoppityEggType.HITMAN);
        }
    }

    @Test
    void spawnIdsIncludeTheYear() {
        assertEquals("1:0", HuntEggSchedule.spawnId(1, 0));
        assertEquals(2, HuntEggSchedule.yearOf(YEAR));
    }
}
