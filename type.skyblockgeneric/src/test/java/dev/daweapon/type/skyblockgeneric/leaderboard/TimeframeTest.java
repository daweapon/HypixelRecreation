package dev.daweapon.type.skyblockgeneric.leaderboard;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeframeTest {

    @Test
    void weekWindowUsesTheIsoWeekBasedYear() {
        assertEquals("2026-W01", Timeframe.WEEK.windowId(LocalDate.of(2025, 12, 29)));
        assertEquals("2026-W53", Timeframe.WEEK.windowId(LocalDate.of(2027, 1, 3)));
        assertEquals("2026-W44", Timeframe.WEEK.windowId(LocalDate.of(2026, 10, 29)));
    }

    @Test
    void monthWindowIsYearAndMonth() {
        assertEquals("2026-10", Timeframe.MONTH.windowId(LocalDate.of(2026, 10, 29)));
        assertEquals("2027-01", Timeframe.MONTH.windowId(LocalDate.of(2027, 1, 1)));
    }

    @Test
    void gainedIsCurrentMinusBaselineAndNeverNegative() {
        assertEquals(150, Timeframe.gained(1150, 1000));
        assertEquals(0, Timeframe.gained(900, 1000));
    }

    @Test
    void windowedBoardKeysCarryTheWindowAndSourceId() {
        SkyBlockBoard board = SkyBlockBoards.byId("week:" + SkyBlockBoards.LEVEL).orElseThrow();
        assertEquals("skyblock:board:week:2026-W44:level", board.windowKey("2026-W44"));
        assertTrue(board.key().startsWith("skyblock:board:week:" + Timeframe.WEEK.currentWindowId()));
        assertEquals(BoardCategory.MONTHLY, SkyBlockBoards.byId("month:skill_total").orElseThrow().category());
    }
}
