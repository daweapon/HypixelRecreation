package dev.daweapon.type.skyblockgeneric.advent;

import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.island.furniture.FurnitureCatalog;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdventCalendarTest {
    private static final Map<Integer, AdventCalendar.Day> DAYS = AdventCalendar.load(new File("../configuration/skyblock/advent_calendar.yml"));

    @Test
    void hasTwentyFiveDaysEachWithCoinsAndGifts() {
        assertEquals(25, DAYS.size());
        for (int day = 1; day <= 25; day++) {
            assertNotNull(DAYS.get(day), "day " + day);
            assertTrue(DAYS.get(day).coins() >= 10_000, "day " + day);
            assertTrue(DAYS.get(day).gifts().values().stream().mapToInt(Integer::intValue).sum() >= 1, "day " + day);
        }
    }

    @Test
    void matchesTheWikiForTheRankedFurniture() {
        assertEquals(Rank.VIP, DAYS.get(1).items().getFirst().rank());
        assertEquals("ADVENT_CALENDAR_DISPLAY", DAYS.get(1).items().getFirst().item());
        assertEquals(Rank.MVP_PLUS, DAYS.get(8).items().getFirst().rank());
        assertEquals("HOT_CHOCOLATE_AND_COOKIES", DAYS.get(23).items().getFirst().item());
        assertEquals(50_000, DAYS.get(25).coins());
        assertEquals(3, DAYS.get(25).gifts().get("RED_GIFT"));
    }

    @Test
    void everyRewardFurnitureCanBePlaced() {
        DAYS.values().forEach(day -> day.items().forEach(item ->
                assertTrue(FurnitureCatalog.get(item.item()) != null || item.item().equals("SNOWMAN_PERSONALITY"), item.item() + " is not placeable furniture")));
    }

    @Test
    void theCalendarOnlyOpensInDecember() {
        assertEquals(0, AdventService.dayOf(LocalDate.of(2026, 11, 30)));
        assertEquals(1, AdventService.dayOf(LocalDate.of(2026, 12, 1)));
        assertEquals(25, AdventService.dayOf(LocalDate.of(2026, 12, 25)));
        assertEquals(25, AdventService.dayOf(LocalDate.of(2026, 12, 31)));
    }

    @Test
    void claimsAreKeptPerYear() {
        assertEquals(Set.of(1, 5, 25), AdventService.parse(AdventService.serialize(2026, Set.of(1, 5, 25)), 2026));
        assertTrue(AdventService.parse(AdventService.serialize(2026, Set.of(1)), 2027).isEmpty());
        assertTrue(AdventService.parse("", 2026).isEmpty());
    }
}
