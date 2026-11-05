package dev.daweapon.type.catacombs.run;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsSidebarTest {
    private static final List<CatacombsSidebar.Teammate> TEAM = List.of(
            new CatacombsSidebar.Teammate('B', "#55ffff", "techmaster54", 1251, 1400, false),
            new CatacombsSidebar.Teammate('M', "#55ff55", "josomater", 300, 1400, false),
            new CatacombsSidebar.Teammate('B', "#55ff55", "SmashBoulder", 0, 1400, true));

    private static CatacombsSidebar.Snapshot snapshot(CatacombsLifecycle.Stage stage, int witherKeys, boolean blood) {
        return new CatacombsSidebar.Snapshot(stage, 3, 4, 2, 20, 72, 55, 110, witherKeys, blood, TEAM, 15);
    }

    @Test
    void waitingShowsThePartyAndReadyCounts() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.WAITING, 0, false));
        assertEquals(List.of("<f>Party: <a>3<7>/<a>4", "<f>Ready: <a>2<7>/<a>3", "<f>Auto start in: <e>20s"), lines);
    }

    @Test
    void activeShowsKeysTimeClearedAndTeammatesLikeHypixel() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.ACTIVE, 0, false));
        assertEquals("<f>Keys: <c>■ <c>✖ <8>■ <a>0x", lines.get(0));
        assertEquals("<f>Time Elapsed: <a>01m 12s", lines.get(1));
        assertEquals("<f>Cleared: <6>55% <8>(110)", lines.get(2));
        assertEquals(" ", lines.get(3));
        assertEquals("<e>[B] <color:#55ffff>techmaster54 <a>1,251<c>❤", lines.get(4));
        assertEquals("<e>[M] <color:#55ff55>josomater <c>300<c>❤", lines.get(5));
        assertEquals("<e>[B] <color:#55ff55>SmashBoulder <c>DEAD", lines.get(6));
        assertTrue(lines.size() <= CatacombsSidebar.MAX_LINES);
    }

    @Test
    void bloodKeyAndWitherKeysAreShown() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.ACTIVE, 2, true));
        assertEquals("<f>Keys: <c>■ <a>✔ <8>■ <a>2x", lines.getFirst());
    }

    @Test
    void underAMinuteShowsSecondsOnly() {
        assertEquals("45s", CatacombsSidebar.elapsed(45));
        assertEquals("01m 03s", CatacombsSidebar.elapsed(63));
    }

    @Test
    void endingShowsTheShutdownTimer() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.ENDING, 0, false));
        assertEquals("<f>Instance Shutdown In: <a>00m 15s", lines.get(1));
    }
}
