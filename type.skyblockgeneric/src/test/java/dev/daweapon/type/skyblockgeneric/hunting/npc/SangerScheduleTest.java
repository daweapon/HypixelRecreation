package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SangerScheduleTest {
    @Test
    void sangerHasTheThreeWikiLocations() {
        assertEquals(3, SangerSchedule.SPOTS.size());
        assertEquals(-632.5, SangerSchedule.SPOTS.get(0).x());
        assertEquals(160.5, SangerSchedule.SPOTS.get(0).z());
        assertEquals(-746.5, SangerSchedule.SPOTS.get(1).x());
        assertEquals(261.5, SangerSchedule.SPOTS.get(1).z());
        assertEquals(-526.5, SangerSchedule.SPOTS.get(2).x());
        assertEquals(218.5, SangerSchedule.SPOTS.get(2).z());
    }

    @Test
    void dayNumberStartsAtOneAndAdvancesEveryTwentyThousandFourHundredTicks() {
        assertEquals(1, SangerSchedule.dayNumber(0));
        assertEquals(1, SangerSchedule.dayNumber(SkyBlockCalendar.DAY - 1));
        assertEquals(2, SangerSchedule.dayNumber(SkyBlockCalendar.DAY));
        assertEquals(32, SangerSchedule.dayNumber(31L * SkyBlockCalendar.DAY));
    }

    @Test
    void theLocationChangesEveryDayAndRepeatsAfterThree() {
        for (long day = 1; day < 20; day++) {
            assertNotEquals(SangerSchedule.spotOn(day), SangerSchedule.spotOn(day + 1));
            assertEquals(SangerSchedule.spotOn(day), SangerSchedule.spotOn(day + 3));
        }
    }
}
