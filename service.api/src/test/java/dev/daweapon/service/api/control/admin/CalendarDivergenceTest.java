package dev.daweapon.service.api.control.admin;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarDivergenceTest {
    @Test
    void serversWithinToleranceAgree() {
        CalendarDivergence.Assessment assessment = CalendarDivergence.assess(List.of(1_000_000L, 1_000_030L), 1_000_050L);
        assertEquals(50L, assessment.spread());
        assertFalse(assessment.diverged());
        assertEquals(2, assessment.reporting());
    }

    @Test
    void aServerADayBehindDiverges() {
        CalendarDivergence.Assessment assessment = CalendarDivergence.assess(
                List.of(5_000_000L, 5_000_000L - CalendarDivergence.DAY), null);
        assertTrue(assessment.diverged());
        assertEquals(CalendarDivergence.DAY, assessment.spread());
    }

    @Test
    void noReportsIsNotDivergence() {
        assertFalse(CalendarDivergence.assess(List.of(), 123L).diverged());
    }

    @Test
    void describesTheDateTheGameShows() {
        assertEquals(new CalendarDivergence.SkyBlockDate(0L, 1, "Early Spring", 1, 0), CalendarDivergence.describe(0L));
        long elapsed = 2 * CalendarDivergence.YEAR + 11 * CalendarDivergence.MONTH + 30 * CalendarDivergence.DAY + 18_500L;
        assertEquals(new CalendarDivergence.SkyBlockDate(elapsed, 3, "Late Winter", 31, 18), CalendarDivergence.describe(elapsed));
    }
}
