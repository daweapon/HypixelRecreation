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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeySourceTest {
    private static final int ENTRANCE = 0;
    private static final int WITHER_ROOM = 1;
    private static final int BLOOD_KEY_ROOM = 2;

    private static PlacedRoom room(int index, RoomKind kind) {
        Cell cell = new Cell(index, 0);
        RoomTemplate template = new RoomTemplate("t" + index, "t" + index, RoomShape.ONE_BY_ONE, kind, 0, true,
                List.of(), List.of(), List.of(), List.of());
        return new PlacedRoom(index, template, Rotation.DEG_0, cell, List.of(cell), true);
    }

    private static KeySource source() {
        List<PlacedRoom> rooms = List.of(room(0, RoomKind.ENTRANCE), room(1, RoomKind.NORMAL), room(2, RoomKind.MINIBOSS), room(3, RoomKind.BLOOD));
        List<LayoutDoor> doors = List.of(
                new LayoutDoor(new Cell(0, 0), new Cell(1, 0), 0, 1, DoorType.PLAIN),
                new LayoutDoor(new Cell(1, 0), new Cell(2, 0), 1, 2, DoorType.WITHER),
                new LayoutDoor(new Cell(2, 0), new Cell(3, 0), 2, 3, DoorType.BLOOD));
        FloorLayout layout = new FloorLayout(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 1L, 4, 1, rooms, doors, List.of(0, 1, 2, 3));
        return new KeySource(KeyDropPlan.of(layout));
    }

    @Test
    void theLastStarredMobKilledInTheKeyRoomDropsItsKey() {
        KeySource keys = source();
        assertEquals(Optional.empty(), keys.killed(WITHER_ROOM, false, false));
        assertEquals(Optional.of(KeyType.WITHER), keys.killed(WITHER_ROOM, false, true));
        assertEquals(Optional.empty(), keys.completed(WITHER_ROOM));
    }

    @Test
    void aMinibossRoomGivesItsKeyFromTheMinibossNotTheLastStarredMob() {
        KeySource keys = source();
        keys.minibossSpawned(BLOOD_KEY_ROOM);
        assertEquals(Optional.empty(), keys.killed(BLOOD_KEY_ROOM, false, true));
        assertEquals(Optional.empty(), keys.completed(BLOOD_KEY_ROOM));
        assertEquals(Optional.of(KeyType.BLOOD), keys.killed(BLOOD_KEY_ROOM, true, false));
        assertEquals(Optional.empty(), keys.killed(BLOOD_KEY_ROOM, true, true));
    }

    @Test
    void theMinibossDropsTheKeyEvenWhenItIsNotTheLastStarredKill() {
        KeySource keys = source();
        keys.minibossSpawned(BLOOD_KEY_ROOM);
        assertEquals(Optional.of(KeyType.BLOOD), keys.killed(BLOOD_KEY_ROOM, true, false));
        assertEquals(Optional.empty(), keys.killed(BLOOD_KEY_ROOM, false, true));
    }

    @Test
    void aKeyRoomClearedWithoutAKillStillGivesItsKeyOnce() {
        KeySource keys = source();
        assertEquals(Optional.of(KeyType.WITHER), keys.completed(WITHER_ROOM));
        assertEquals(Optional.empty(), keys.completed(WITHER_ROOM));
        assertEquals(Optional.empty(), keys.killed(WITHER_ROOM, false, true));
    }

    @Test
    void roomsWithoutAKeyNeverDropOne() {
        KeySource keys = source();
        assertEquals(Optional.empty(), keys.killed(ENTRANCE, false, true));
        assertEquals(Optional.empty(), keys.killed(ENTRANCE, true, true));
        assertEquals(Optional.empty(), keys.completed(ENTRANCE));
    }
}
