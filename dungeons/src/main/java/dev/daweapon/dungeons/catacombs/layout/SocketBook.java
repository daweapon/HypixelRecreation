package dev.daweapon.dungeons.catacombs.layout;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Which walls of each placed room can take a door. A one-cell room whose rotation is not locked turns to fit the doors it
 * is given, so its sockets are checked against every rotation; every other room keeps the rotation it was placed with.
 */
final class SocketBook {
    private final List<PlacedRoom> rooms;
    private final Map<Cell, Integer> roomOfCell;
    private final Map<Integer, Set<Side>> used = new HashMap<>();

    SocketBook(List<PlacedRoom> rooms, Map<Cell, Integer> roomOfCell) {
        this.rooms = rooms;
        this.roomOfCell = roomOfCell;
    }

    boolean allows(Cell from, Cell to) {
        Side side = Side.between(from, to);
        return allows(roomOfCell.get(from), from, side) && allows(roomOfCell.get(to), to, side.opposite());
    }

    void use(Cell from, Cell to) {
        Side side = Side.between(from, to);
        used.computeIfAbsent(roomOfCell.get(from), key -> EnumSet.noneOf(Side.class)).add(side);
        used.computeIfAbsent(roomOfCell.get(to), key -> EnumSet.noneOf(Side.class)).add(side.opposite());
    }

    List<Cell[]> usable(List<Cell[]> pairs) {
        List<Cell[]> usable = new ArrayList<>();
        for (Cell[] pair : pairs) {
            if (allows(pair[0], pair[1])) usable.add(pair);
        }
        return usable;
    }

    List<PlacedRoom> settle(Random random) {
        List<PlacedRoom> settled = new ArrayList<>(rooms);
        for (PlacedRoom room : rooms) {
            if (!turnsFreely(room)) continue;
            List<Rotation> fits = room.template().rotationsAllowing(used.getOrDefault(room.index(), Set.of()));
            if (fits.isEmpty()) return null;
            settled.set(room.index(), room.withRotation(fits.get(random.nextInt(fits.size()))));
        }
        return settled;
    }

    private boolean allows(int roomIndex, Cell cell, Side side) {
        PlacedRoom room = rooms.get(roomIndex);
        if (!turnsFreely(room)) return room.allowsDoor(cell, side);
        Set<Side> wanted = EnumSet.of(side);
        wanted.addAll(used.getOrDefault(roomIndex, Set.of()));
        return !room.template().rotationsAllowing(wanted).isEmpty();
    }

    private static boolean turnsFreely(PlacedRoom room) {
        return room.cells().size() == 1 && room.template().fixedRotation() == null && room.template().restrictsDoors();
    }
}
