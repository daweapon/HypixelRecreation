package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomColor;

import java.util.Optional;

/**
 * What block goes where in the placeholder dungeon: a flat grid of 31x31 room floors, one-block wall strips between
 * rooms and around the whole dungeon, a doorway wherever the layout has a door, and a barrier ceiling.
 */
public final class CatacombsBlockPlan {
    public enum Tile {AIR, BEDROCK, FLOOR, WALL, CEILING, WITHER_DOOR, BLOOD_DOOR, MARKER_FLOOR, MARKER_PILLAR}

    public static final int WALL_HEIGHT = 12;
    public static final int MARKER_RADIUS = 2;
    public static final int PILLAR_HEIGHT = 4;

    private static final int PITCH = CellGeometry.CELL_PITCH;
    private static final int ROOM = CellGeometry.ROOM_SIZE;
    private static final int CENTRE = ROOM / 2;
    private static final int FLOOR_Y = CellGeometry.FLOOR_Y;

    private enum Column {OUTSIDE, WALL, FLOOR, DOORWAY}

    private final FloorLayout layout;
    private final int[][] roomOf;

    public CatacombsBlockPlan(FloorLayout layout) {
        this.layout = layout;
        this.roomOf = new int[layout.width()][layout.height()];
        for (PlacedRoom room : layout.rooms()) {
            for (Cell cell : room.cells()) roomOf[cell.col()][cell.row()] = room.index();
        }
    }

    public int minX() {
        return CellGeometry.ORIGIN_X - 1;
    }

    public int maxX() {
        return CellGeometry.ORIGIN_X + layout.width() * PITCH - 1;
    }

    public int minZ() {
        return CellGeometry.ORIGIN_Z - 1;
    }

    public int maxZ() {
        return CellGeometry.ORIGIN_Z + layout.height() * PITCH - 1;
    }

    public int minY() {
        return FLOOR_Y - 1;
    }

    public int maxY() {
        return FLOOR_Y + WALL_HEIGHT + 1;
    }

    public Tile tileAt(int x, int y, int z) {
        Column column = column(x, z);
        if (column == Column.OUTSIDE || y < minY() || y > maxY()) return Tile.AIR;
        if (y == minY()) return Tile.BEDROCK;
        if (y == maxY()) return Tile.CEILING;
        if (column == Column.WALL) return Tile.WALL;
        if (y == FLOOR_Y) return isMarkerFloor(x, z) ? Tile.MARKER_FLOOR : Tile.FLOOR;
        if (column == Column.DOORWAY) return doorwayTile(x, y, z);
        return y - FLOOR_Y <= PILLAR_HEIGHT && isPillar(x, z) ? Tile.MARKER_PILLAR : Tile.AIR;
    }

    public RoomColor markerColourAt(int x, int z) {
        return roomAtColumn(x, z).map(PlacedRoom::color).orElse(RoomColor.BROWN);
    }

    public Optional<PlacedRoom> roomAtColumn(int x, int z) {
        int localX = x - CellGeometry.ORIGIN_X;
        int localZ = z - CellGeometry.ORIGIN_Z;
        if (localX < 0 || localZ < 0) return Optional.empty();
        int col = localX / PITCH;
        int row = localZ / PITCH;
        if (col >= layout.width() || row >= layout.height()) return Optional.empty();
        return Optional.of(layout.room(roomOf[col][row]));
    }

    private Tile doorwayTile(int x, int y, int z) {
        if (y - FLOOR_Y > DoorFrame.HEIGHT) return Tile.WALL;
        return doorAt(x, z).map(door -> switch (door.type()) {
            case PLAIN -> Tile.AIR;
            case WITHER -> Tile.WITHER_DOOR;
            case BLOOD -> Tile.BLOOD_DOOR;
        }).orElse(Tile.WALL);
    }

    private Optional<LayoutDoor> doorAt(int x, int z) {
        return layout.doors().stream().filter(door -> DoorFrame.of(door).containsColumn(x, z)).findFirst();
    }

    private boolean isMarkerFloor(int x, int z) {
        int offsetX = Math.floorMod(x - CellGeometry.ORIGIN_X, PITCH);
        int offsetZ = Math.floorMod(z - CellGeometry.ORIGIN_Z, PITCH);
        return offsetX < ROOM && offsetZ < ROOM
                && Math.abs(offsetX - CENTRE) <= MARKER_RADIUS && Math.abs(offsetZ - CENTRE) <= MARKER_RADIUS;
    }

    private boolean isPillar(int x, int z) {
        return Math.floorMod(x - CellGeometry.ORIGIN_X, PITCH) == CENTRE
                && Math.floorMod(z - CellGeometry.ORIGIN_Z, PITCH) == CENTRE;
    }

    private Column column(int x, int z) {
        int localX = x - CellGeometry.ORIGIN_X;
        int localZ = z - CellGeometry.ORIGIN_Z;
        if (localX < -1 || localZ < -1 || x > maxX() || z > maxZ()) return Column.OUTSIDE;
        if (localX == -1 || localZ == -1) return Column.WALL;
        int col = localX / PITCH;
        int row = localZ / PITCH;
        boolean gapX = localX % PITCH == ROOM;
        boolean gapZ = localZ % PITCH == ROOM;
        if (!gapX && !gapZ) return Column.FLOOR;
        if (gapX && gapZ) return cornerColumn(col, row);
        int nearCentre = Math.abs((gapX ? localZ % PITCH : localX % PITCH) - CENTRE);
        Cell self = new Cell(col, row);
        Cell neighbour = gapX ? new Cell(col + 1, row) : new Cell(col, row + 1);
        return stripColumn(self, neighbour, nearCentre <= DoorFrame.WIDTH / 2);
    }

    private Column cornerColumn(int col, int row) {
        if (!inBounds(new Cell(col + 1, row + 1))) return Column.WALL;
        int room = roomOf[col][row];
        boolean sameRoom = roomOf[col + 1][row] == room && roomOf[col][row + 1] == room && roomOf[col + 1][row + 1] == room;
        return sameRoom ? Column.FLOOR : Column.WALL;
    }

    private Column stripColumn(Cell self, Cell neighbour, boolean nearCentre) {
        if (!inBounds(neighbour)) return Column.WALL;
        if (roomOf[self.col()][self.row()] == roomOf[neighbour.col()][neighbour.row()]) return Column.FLOOR;
        return nearCentre && layout.doorBetween(self, neighbour).isPresent() ? Column.DOORWAY : Column.WALL;
    }

    private boolean inBounds(Cell cell) {
        return cell.col() >= 0 && cell.row() >= 0 && cell.col() < layout.width() && cell.row() < layout.height();
    }
}
