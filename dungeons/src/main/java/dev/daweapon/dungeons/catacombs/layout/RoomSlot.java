package dev.daweapon.dungeons.catacombs.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A room whose cells are decided but whose template and rotation are not yet: every template of its kind and shape
 * that covers exactly those cells, in each rotation that does. Doors are only placed where some candidate has a
 * socket, and the room settles on a candidate once all its doors are known.
 */
record RoomSlot(int index, RoomKind kind, RoomShape shape, Cell origin, List<Cell> cells, boolean onCriticalPath,
                List<Candidate> candidates) {

    record Candidate(RoomTemplate template, Rotation rotation) {
        boolean allows(Collection<DoorSocket> sockets) {
            for (DoorSocket socket : sockets) {
                if (!template.allowsDoor(rotation, socket.cell(), socket.side())) return false;
            }
            return true;
        }
    }

    static RoomSlot of(int index, RoomKind kind, RoomShape shape, List<Cell> cells, boolean onCriticalPath,
                       List<RoomTemplate> templates, Random random) {
        int minCol = cells.stream().mapToInt(Cell::col).min().orElseThrow();
        int minRow = cells.stream().mapToInt(Cell::row).min().orElseThrow();
        Cell origin = new Cell(minCol, minRow);
        Set<Cell> footprint = new HashSet<>(cells);
        List<Candidate> candidates = new ArrayList<>();
        for (RoomTemplate template : templates) {
            for (Rotation rotation : Rotation.values()) {
                if (template.rotationLock().isPresent() && template.rotationLock().get() != rotation) continue;
                if (covers(shape, rotation, origin, footprint)) candidates.add(new Candidate(template, rotation));
            }
        }
        Collections.shuffle(candidates, random);
        return new RoomSlot(index, kind, shape, origin, List.copyOf(cells), onCriticalPath, List.copyOf(candidates));
    }

    private static boolean covers(RoomShape shape, Rotation rotation, Cell origin, Set<Cell> footprint) {
        List<Cell> rotated = shape.cells(rotation);
        if (rotated.size() != footprint.size()) return false;
        for (Cell cell : rotated) {
            if (!footprint.contains(cell.translate(origin.col(), origin.row()))) return false;
        }
        return true;
    }

    DoorSocket socket(Cell cell, Side side) {
        return new DoorSocket(new Cell(cell.col() - origin.col(), cell.row() - origin.row()), side);
    }

    boolean fits(Collection<DoorSocket> sockets) {
        return candidates.stream().anyMatch(candidate -> candidate.allows(sockets));
    }

    PlacedRoom place(Candidate candidate) {
        List<Cell> placed = shape.cells(candidate.rotation()).stream()
                .map(cell -> cell.translate(origin.col(), origin.row()))
                .toList();
        return new PlacedRoom(index, candidate.template(), candidate.rotation(), origin, placed, onCriticalPath);
    }
}
