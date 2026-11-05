package dev.daweapon.dungeons.catacombs.map;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;

import java.util.Arrays;
import java.util.Map;
import java.util.function.IntPredicate;

/** How one room looks on the Magical Map: not drawn, a grey "?", open, or open with its check mark. */
public enum MapRoomView {
    HIDDEN,
    UNKNOWN,
    OPEN,
    CLEARED,
    SECRETS_DONE,
    FAILED;

    public boolean drawn() {
        return this != HIDDEN;
    }

    public boolean explored() {
        return this != HIDDEN && this != UNKNOWN;
    }

    public static MapRoomView of(DungeonRoomState state, boolean secretsDone) {
        return switch (state) {
            case UNDISCOVERED -> HIDDEN;
            case DISCOVERED -> OPEN;
            case CLEARED, COMPLETED -> secretsDone ? SECRETS_DONE : CLEARED;
            case FAILED -> FAILED;
        };
    }

    /** Every room's view; an unexplored room behind a door of an explored one shows as a grey "?". */
    public static MapRoomView[] views(FloorLayout layout, Map<Integer, DungeonRoomState> states, IntPredicate secretsDone) {
        MapRoomView[] views = new MapRoomView[layout.totalRooms()];
        Arrays.fill(views, HIDDEN);
        for (PlacedRoom room : layout.rooms()) {
            views[room.index()] = of(states.getOrDefault(room.index(), DungeonRoomState.UNDISCOVERED), secretsDone.test(room.index()));
        }
        for (LayoutDoor door : layout.doors()) {
            reveal(views, door.fromRoom(), door.toRoom());
            reveal(views, door.toRoom(), door.fromRoom());
        }
        return views;
    }

    private static void reveal(MapRoomView[] views, int explored, int neighbour) {
        if (views[explored].explored() && views[neighbour] == HIDDEN) views[neighbour] = UNKNOWN;
    }
}
