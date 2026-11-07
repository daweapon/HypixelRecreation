package dev.daweapon.dungeons.catacombs.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Which walls of each room slot can still take a door: a door is allowed while at least one of the slot's candidate
 * templates and rotations has a socket for it and for every door already given to that room. Once the doors are
 * final, each slot settles on a fitting candidate, real rooms before placeholders and unused rooms before repeats.
 */
final class SocketBook {
    private final List<RoomSlot> slots;
    private final Map<Cell, Integer> roomOfCell;
    private final Map<Integer, Set<DoorSocket>> used = new HashMap<>();

    SocketBook(List<RoomSlot> slots, Map<Cell, Integer> roomOfCell) {
        this.slots = slots;
        this.roomOfCell = roomOfCell;
    }

    boolean allows(Cell from, Cell to) {
        Side side = Side.between(from, to);
        return allows(roomOfCell.get(from), from, side) && allows(roomOfCell.get(to), to, side.opposite());
    }

    void use(Cell from, Cell to) {
        Side side = Side.between(from, to);
        int fromRoom = roomOfCell.get(from);
        int toRoom = roomOfCell.get(to);
        used.computeIfAbsent(fromRoom, key -> new LinkedHashSet<>()).add(slots.get(fromRoom).socket(from, side));
        used.computeIfAbsent(toRoom, key -> new LinkedHashSet<>()).add(slots.get(toRoom).socket(to, side.opposite()));
    }

    List<Cell[]> usable(List<Cell[]> pairs) {
        List<Cell[]> usable = new ArrayList<>();
        for (Cell[] pair : pairs) {
            if (allows(pair[0], pair[1])) usable.add(pair);
        }
        return usable;
    }

    List<PlacedRoom> settle() {
        PlacedRoom[] settled = new PlacedRoom[slots.size()];
        Set<String> placed = new HashSet<>();
        List<RoomSlot> order = slots.stream()
                .sorted(Comparator.comparingLong(this::fittingCount))
                .toList();
        for (RoomSlot slot : order) {
            Optional<RoomSlot.Candidate> choice = choose(slot, placed);
            if (choice.isEmpty()) return null;
            placed.add(choice.get().template().id());
            settled[slot.index()] = slot.place(choice.get());
        }
        return List.of(settled);
    }

    private Optional<RoomSlot.Candidate> choose(RoomSlot slot, Set<String> placed) {
        Set<DoorSocket> doors = used.getOrDefault(slot.index(), Set.of());
        List<RoomSlot.Candidate> fitting = slot.candidates().stream().filter(candidate -> candidate.allows(doors)).toList();
        Optional<RoomSlot.Candidate> fresh = best(fitting.stream().filter(candidate -> !placed.contains(candidate.template().id())).toList());
        if (fresh.isPresent() || slot.kind() != RoomKind.NORMAL) return fresh;
        return best(fitting);
    }

    private static Optional<RoomSlot.Candidate> best(List<RoomSlot.Candidate> candidates) {
        return candidates.stream()
                .filter(candidate -> !candidate.template().placeholder())
                .findFirst()
                .or(() -> candidates.stream().findFirst());
    }

    private long fittingCount(RoomSlot slot) {
        Set<DoorSocket> doors = used.getOrDefault(slot.index(), Set.of());
        return slot.candidates().stream().filter(candidate -> candidate.allows(doors)).count();
    }

    private boolean allows(int roomIndex, Cell cell, Side side) {
        RoomSlot slot = slots.get(roomIndex);
        Set<DoorSocket> wanted = new LinkedHashSet<>(used.getOrDefault(roomIndex, Set.of()));
        wanted.add(slot.socket(cell, side));
        return slot.fits(wanted);
    }
}
