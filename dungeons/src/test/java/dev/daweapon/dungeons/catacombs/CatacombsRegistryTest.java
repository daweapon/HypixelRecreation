package dev.daweapon.dungeons.catacombs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatacombsRegistryTest {
    private final CatacombsRegistry registry = CatacombsRegistry.defaults();

    @Test
    void everyFloorHasAtLeastTwoPuzzleRooms() {
        for (CatacombsMode mode : CatacombsMode.values()) {
            for (CatacombsFloor floor : CatacombsFloor.values()) {
                if (floor.supports(mode)) {
                    assertTrue(registry.floor(floor, mode).puzzleRooms() >= 2);
                }
            }
        }
    }

    @Test
    void entranceHasNoMasterDefinition() {
        assertThrows(IllegalArgumentException.class, () -> registry.floor(CatacombsFloor.ENTRANCE, CatacombsMode.MASTER));
    }

    @Test
    void trapRoomsStartAtFloorThree() {
        assertEquals(0, registry.floor(CatacombsFloor.FLOOR_TWO, CatacombsMode.NORMAL).trapRooms());
        assertEquals(1, registry.floor(CatacombsFloor.FLOOR_THREE, CatacombsMode.NORMAL).trapRooms());
        assertEquals(1, registry.floor(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER).trapRooms());
    }

    @Test
    void autoReviveOnlyInNormalMode() {
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            assertTrue(registry.floor(floor, CatacombsMode.NORMAL).rules().automaticGhostRevive());
        }
        assertFalse(registry.floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.MASTER).rules().automaticGhostRevive());
    }

    @Test
    void speedTimeLimitsComeFromTheScoreTable() {
        assertEquals(600, registry.floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL).rules().speedTimeLimitSeconds());
        assertEquals(840, registry.floor(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL).rules().speedTimeLimitSeconds());
        assertEquals(600, registry.floor(CatacombsFloor.FLOOR_SIX, CatacombsMode.MASTER).rules().speedTimeLimitSeconds());
        assertEquals(3600, registry.floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL).rules().maximumDurationSeconds());
    }
}
