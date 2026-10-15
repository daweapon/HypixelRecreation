package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.DoorType;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.RoomShape;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyDropPlanTest {
    private static PlacedRoom room(int index, RoomKind kind, Cell cell) {
        RoomTemplate template = new RoomTemplate("t" + index, "t" + index, RoomShape.ONE_BY_ONE, kind, 0, true,
                List.of(), List.of(), List.of(), List.of());
        return new PlacedRoom(index, template, Rotation.DEG_0, cell, List.of(cell), true);
    }

    private static FloorLayout layout(DoorType... doorTypes) {
        List<PlacedRoom> rooms = new ArrayList<>();
        List<LayoutDoor> doors = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        for (int index = 0; index <= doorTypes.length; index++) {
            RoomKind kind = index == 0 ? RoomKind.ENTRANCE : index == doorTypes.length ? RoomKind.BLOOD : RoomKind.NORMAL;
            rooms.add(room(index, kind, new Cell(index, 0)));
            path.add(index);
        }
        for (int index = 0; index < doorTypes.length; index++) {
            doors.add(new LayoutDoor(new Cell(index, 0), new Cell(index + 1, 0), index, index + 1, doorTypes[index]));
        }
        return new FloorLayout(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 1L, doorTypes.length + 1, 1, rooms, doors, path);
    }

    @Test
    void theRoomBeforeAWitherDoorGivesAWitherKey() {
        KeyDropPlan plan = KeyDropPlan.of(layout(DoorType.PLAIN, DoorType.WITHER, DoorType.WITHER, DoorType.BLOOD));
        assertEquals(Optional.empty(), plan.keyFor(0));
        assertEquals(Optional.of(KeyType.WITHER), plan.keyFor(1));
        assertEquals(Optional.of(KeyType.WITHER), plan.keyFor(2));
    }

    @Test
    void theRoomBeforeTheBloodRoomGivesTheBloodKey() {
        KeyDropPlan plan = KeyDropPlan.of(layout(DoorType.PLAIN, DoorType.WITHER, DoorType.BLOOD));
        assertEquals(Optional.of(KeyType.BLOOD), plan.keyFor(2));
        assertEquals(Optional.empty(), plan.keyFor(3));
    }

    @Test
    void oneKeyPerClosedDoorOnThePath() {
        KeyDropPlan plan = KeyDropPlan.of(layout(DoorType.PLAIN, DoorType.WITHER, DoorType.WITHER, DoorType.BLOOD));
        assertEquals(3, plan.all().size());
    }
}
