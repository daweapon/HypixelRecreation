package dev.daweapon.dungeons.catacombs.layout;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

import java.util.List;
import java.util.Optional;

public final class FloorLayout {
    private final CatacombsFloor floor;
    private final CatacombsMode mode;
    private final long seed;
    private final int width;
    private final int height;
    private final List<PlacedRoom> rooms;
    private final List<LayoutDoor> doors;
    private final List<Integer> criticalPath;

    public FloorLayout(CatacombsFloor floor, CatacombsMode mode, long seed, int width, int height,
                       List<PlacedRoom> rooms, List<LayoutDoor> doors, List<Integer> criticalPath) {
        this.floor = floor;
        this.mode = mode;
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.rooms = List.copyOf(rooms);
        this.doors = List.copyOf(doors);
        this.criticalPath = List.copyOf(criticalPath);
    }

    public CatacombsFloor floor() {
        return floor;
    }

    public CatacombsMode mode() {
        return mode;
    }

    public long seed() {
        return seed;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<PlacedRoom> rooms() {
        return rooms;
    }

    public List<LayoutDoor> doors() {
        return doors;
    }

    public List<Integer> criticalPath() {
        return criticalPath;
    }

    public PlacedRoom room(int index) {
        return rooms.get(index);
    }

    public Optional<PlacedRoom> roomAt(Cell cell) {
        return rooms.stream().filter(room -> room.occupies(cell)).findFirst();
    }

    public List<PlacedRoom> roomsOfKind(RoomKind kind) {
        return rooms.stream().filter(room -> room.kind() == kind).toList();
    }

    public PlacedRoom startRoom() {
        return rooms.get(criticalPath.getFirst());
    }

    public PlacedRoom bloodRoom() {
        return rooms.get(criticalPath.getLast());
    }

    public PlacedRoom fairyRoom() {
        return roomsOfKind(RoomKind.FAIRY).getFirst();
    }

    public List<LayoutDoor> doorsOf(int roomIndex) {
        return doors.stream().filter(door -> door.connects(roomIndex)).toList();
    }

    public Optional<LayoutDoor> doorBetween(Cell first, Cell second) {
        return doors.stream()
                .filter(door -> door.from().equals(first) && door.to().equals(second)
                        || door.from().equals(second) && door.to().equals(first))
                .findFirst();
    }

    public int totalRooms() {
        return rooms.size();
    }

    public int totalSecrets() {
        return rooms.stream().mapToInt(PlacedRoom::secretCount).sum();
    }

    public List<PlacedRoom> requiredMobRooms() {
        return rooms.stream().filter(room -> room.template().hasStarredMobs()).toList();
    }

    public int requiredMobRoomCount() {
        return requiredMobRooms().size();
    }

    public String render() {
        StringBuilder out = new StringBuilder();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = new Cell(col, row);
                out.append(cellText(cell));
                if (col < width - 1) {
                    out.append(link(cell, new Cell(col + 1, row)));
                }
            }
            out.append('\n');
            if (row < height - 1) {
                for (int col = 0; col < width; col++) {
                    out.append(' ').append(link(new Cell(col, row), new Cell(col, row + 1))).append(' ');
                    if (col < width - 1) {
                        out.append(' ');
                    }
                }
                out.append('\n');
            }
        }
        return out.toString();
    }

    private String cellText(Cell cell) {
        PlacedRoom room = roomAt(cell).orElseThrow();
        return "%c%02d".formatted(room.kind().symbol(), room.index());
    }

    private char link(Cell first, Cell second) {
        if (roomAt(first).orElseThrow().index() == roomAt(second).orElseThrow().index()) {
            return '=';
        }
        return doorBetween(first, second).map(door -> switch (door.type()) {
            case PLAIN -> '+';
            case WITHER -> 'W';
            case BLOOD -> 'X';
        }).orElse(' ');
    }
}
