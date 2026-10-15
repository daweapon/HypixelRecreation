package dev.daweapon.dungeons.catacombs.boss;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatcherSpawnsTest {
    @Test
    void mobCountIsNinePlusFloorTier() {
        assertEquals(9, WatcherSpawns.mobCount(CatacombsFloor.ENTRANCE));
        assertEquals(10, WatcherSpawns.mobCount(CatacombsFloor.FLOOR_ONE));
        assertEquals(16, WatcherSpawns.mobCount(CatacombsFloor.FLOOR_SEVEN));
    }

    @Test
    void onlyEntranceCompletesTheRun() {
        assertTrue(WatcherSpawns.completesRunWhenCleared(CatacombsFloor.ENTRANCE));
        assertFalse(WatcherSpawns.completesRunWhenCleared(CatacombsFloor.FLOOR_ONE));
        assertFalse(WatcherSpawns.cleared(CatacombsFloor.ENTRANCE, 8));
        assertTrue(WatcherSpawns.cleared(CatacombsFloor.ENTRANCE, 9));
    }
}
