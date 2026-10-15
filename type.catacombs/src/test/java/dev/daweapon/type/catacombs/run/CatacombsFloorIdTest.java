package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsFloorIdTest {
    @Test
    void parsesNormalFloors() {
        assertEquals(new CatacombsFloorId(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL), CatacombsFloorId.parse("ENTRANCE"));
        assertEquals(new CatacombsFloorId(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL), CatacombsFloorId.parse("F1"));
        assertEquals(new CatacombsFloorId(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL), CatacombsFloorId.parse("f7"));
    }

    @Test
    void parsesMasterFloors() {
        CatacombsFloorId id = CatacombsFloorId.parse("MASTER_F3");
        assertEquals(CatacombsFloor.FLOOR_THREE, id.floor());
        assertEquals(CatacombsMode.MASTER, id.mode());
        assertEquals("MASTER_F3", id.encode());
    }

    @Test
    void encodingRoundTrips() {
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            CatacombsFloorId id = new CatacombsFloorId(floor, CatacombsMode.NORMAL);
            assertEquals(id, CatacombsFloorId.parse(id.encode()));
        }
    }

    @Test
    void rejectsUnknownFloorsAndMasterEntrance() {
        assertThrows(IllegalArgumentException.class, () -> CatacombsFloorId.parse("F8"));
        assertThrows(IllegalArgumentException.class, () -> CatacombsFloorId.parse("KUUDRA"));
        assertThrows(IllegalArgumentException.class, () -> CatacombsFloorId.parse("MASTER_ENTRANCE"));
    }

    @Test
    void capacityIsFourRuns() {
        assertEquals(4, CatacombsRuns.MAX_RUNS);
        assertEquals(4, CatacombsRunSlots.remaining(CatacombsRuns.MAX_RUNS, 0));
        assertEquals(1, CatacombsRunSlots.remaining(CatacombsRuns.MAX_RUNS, 3));
        assertEquals(0, CatacombsRunSlots.remaining(CatacombsRuns.MAX_RUNS, 7));
        assertFalse(CatacombsRunSlots.full(CatacombsRuns.MAX_RUNS, 3));
        assertTrue(CatacombsRunSlots.full(CatacombsRuns.MAX_RUNS, 4));
    }

    @Test
    void rosterNeedsOneToFivePlayers() {
        List<UUID> one = List.of(UUID.randomUUID());
        assertEquals(one, CatacombsRoster.validated(one));
        assertThrows(IllegalArgumentException.class, () -> CatacombsRoster.validated(List.of()));
        List<UUID> six = java.util.stream.Stream.generate(UUID::randomUUID).limit(6).toList();
        assertThrows(IllegalArgumentException.class, () -> CatacombsRoster.validated(six));
    }
}
