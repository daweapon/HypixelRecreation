package dev.daweapon.type.catacombs.run;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsSidebarTest {
    private static CatacombsSidebar.Snapshot snapshot(CatacombsLifecycle.Stage stage, int witherKeys, boolean blood) {
        return new CatacombsSidebar.Snapshot(stage, 3, 4, 2, 20, 125, 56, 4, 18, 1, witherKeys, blood, 2, 15);
    }

    @Test
    void waitingShowsThePartyAndReadyCounts() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.WAITING, 0, false));
        assertEquals(List.of("<f>Party: <a>3<7>/<a>4", "<f>Ready: <a>2<7>/<a>3", "<f>Auto start in: <e>20s"), lines);
    }

    @Test
    void activeShowsTimeClearedKeysAndSecrets() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.ACTIVE, 1, false));
        assertEquals("<f>Time Elapsed: <a>2m 05s", lines.get(0));
        assertEquals("<f>Dungeon Cleared: <e>56%", lines.get(1));
        assertTrue(lines.contains("<f>Keys: <a>✔ <8>Wither <c>✖ <c>Blood"));
        assertTrue(lines.contains("<f>Secrets Found: <a>4<7>/<a>18"));
        assertTrue(lines.contains("<f>Crypts: <a>1"));
        assertTrue(lines.size() <= CatacombsSidebar.MAX_LINES);
    }

    @Test
    void bloodKeyAndStackedWitherKeysAreShown() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.ACTIVE, 2, true));
        assertTrue(lines.contains("<f>Keys: <a>✔ <8>Wither x2 <a>✔ <c>Blood"));
    }

    @Test
    void endingShowsTheShutdownTimer() {
        List<String> lines = CatacombsSidebar.lines(snapshot(CatacombsLifecycle.Stage.ENDING, 0, false));
        assertEquals("<f>Instance Shutdown In: <a>00m 15s", lines.get(1));
    }
}
