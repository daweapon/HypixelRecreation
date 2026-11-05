package dev.daweapon.dungeons.catacombs.map;

import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;

/**
 * Paints the Magical Map as the 128x128 map-colour bytes a filled map shows. Room and mark colours are the bytes the
 * client mods read off Hypixel's map: 30 green start, 63 brown, 66 purple puzzle, 62 orange trap, 74 yellow miniboss,
 * 82 pink fairy, 18 red blood, 85 grey unknown, 119 black wither door, 34 white check.
 */
public final class DungeonMapCanvas {
    public static final int SIZE = 128;
    public static final byte EMPTY = 0;
    public static final byte ENTRANCE = 30;
    public static final byte NORMAL = 63;
    public static final byte PUZZLE = 66;
    public static final byte TRAP = 62;
    public static final byte MINIBOSS = 74;
    public static final byte FAIRY = 82;
    public static final byte BLOOD = 18;
    public static final byte UNKNOWN = 85;
    public static final byte BLACK = 119;
    public static final byte WHITE = 34;
    public static final byte GREEN = 30;
    public static final byte RED = 18;

    private static final int GAP = 4;
    private static final String[] CHECK = {
            "......#",
            ".....##",
            "#...##.",
            "##.##..",
            ".###...",
            "..#...."};
    private static final String[] CROSS = {
            "#...#",
            ".#.#.",
            "..#..",
            ".#.#.",
            "#...#"};
    private static final String[] QUESTION = {
            ".###.",
            "#...#",
            "....#",
            "...#.",
            "..#..",
            ".....",
            "..#.."};

    private final FloorLayout layout;
    private final int roomSize;
    private final int startX;
    private final int startZ;

    public DungeonMapCanvas(FloorLayout layout) {
        this.layout = layout;
        this.roomSize = Math.max(layout.width(), layout.height()) >= 6 ? 16 : 18;
        this.startX = (SIZE - span(layout.width())) / 2;
        this.startZ = (SIZE - span(layout.height())) / 2;
    }

    private int span(int cells) {
        return cells * roomSize + (cells - 1) * GAP;
    }

    public int roomSize() {
        return roomSize;
    }

    public int cellX(int col) {
        return startX + col * (roomSize + GAP);
    }

    public int cellZ(int row) {
        return startZ + row * (roomSize + GAP);
    }

    public double pixelX(double worldX) {
        return startX + (worldX - CellGeometry.ORIGIN_X) * (roomSize + GAP) / CellGeometry.CELL_PITCH;
    }

    public double pixelZ(double worldZ) {
        return startZ + (worldZ - CellGeometry.ORIGIN_Z) * (roomSize + GAP) / CellGeometry.CELL_PITCH;
    }

    public static byte colour(RoomKind kind) {
        return switch (kind) {
            case ENTRANCE -> ENTRANCE;
            case NORMAL, RARE -> NORMAL;
            case PUZZLE -> PUZZLE;
            case MINIBOSS -> MINIBOSS;
            case TRAP -> TRAP;
            case FAIRY -> FAIRY;
            case BLOOD -> BLOOD;
        };
    }

    public byte[] render(MapRoomView[] views) {
        byte[] pixels = new byte[SIZE * SIZE];
        for (LayoutDoor door : layout.doors()) {
            if (shown(door, views)) drawDoor(pixels, door, views);
        }
        for (PlacedRoom room : layout.rooms()) {
            MapRoomView view = views[room.index()];
            if (!view.drawn()) continue;
            drawRoom(pixels, room, view == MapRoomView.UNKNOWN ? UNKNOWN : colour(room.kind()));
            drawMark(pixels, room, view);
        }
        return pixels;
    }

    private static boolean shown(LayoutDoor door, MapRoomView[] views) {
        MapRoomView from = views[door.fromRoom()];
        MapRoomView to = views[door.toRoom()];
        return from.drawn() && to.drawn() && (from.explored() || to.explored());
    }

    private void drawRoom(byte[] pixels, PlacedRoom room, byte colour) {
        for (Cell cell : room.cells()) {
            fill(pixels, cellX(cell.col()), cellZ(cell.row()), roomSize, roomSize, colour);
            if (room.occupies(cell.translate(1, 0))) fill(pixels, cellX(cell.col()) + roomSize, cellZ(cell.row()), GAP, roomSize, colour);
            if (room.occupies(cell.translate(0, 1))) fill(pixels, cellX(cell.col()), cellZ(cell.row()) + roomSize, roomSize, GAP, colour);
            if (room.occupies(cell.translate(1, 0)) && room.occupies(cell.translate(0, 1)) && room.occupies(cell.translate(1, 1))) {
                fill(pixels, cellX(cell.col()) + roomSize, cellZ(cell.row()) + roomSize, GAP, GAP, colour);
            }
        }
    }

    private void drawDoor(byte[] pixels, LayoutDoor door, MapRoomView[] views) {
        Cell first = door.from().col() + door.from().row() <= door.to().col() + door.to().row() ? door.from() : door.to();
        boolean horizontal = door.from().row() == door.to().row();
        int width = roomSize / 3;
        int offset = (roomSize - width) / 2;
        byte colour = doorColour(door, views);
        if (horizontal) fill(pixels, cellX(first.col()) + roomSize, cellZ(first.row()) + offset, GAP, width, colour);
        else fill(pixels, cellX(first.col()) + offset, cellZ(first.row()) + roomSize, width, GAP, colour);
    }

    private byte doorColour(LayoutDoor door, MapRoomView[] views) {
        return switch (door.type()) {
            case WITHER -> BLACK;
            case BLOOD -> BLOOD;
            case PLAIN -> {
                if (!views[door.fromRoom()].explored() || !views[door.toRoom()].explored()) yield UNKNOWN;
                boolean fairy = layout.room(door.fromRoom()).kind() == RoomKind.FAIRY || layout.room(door.toRoom()).kind() == RoomKind.FAIRY;
                yield fairy ? FAIRY : NORMAL;
            }
        };
    }

    private void drawMark(byte[] pixels, PlacedRoom room, MapRoomView view) {
        if (room.kind() == RoomKind.ENTRANCE && view != MapRoomView.FAILED) return;
        switch (view) {
            case UNKNOWN -> stamp(pixels, room, QUESTION, BLACK);
            case CLEARED -> stamp(pixels, room, CHECK, WHITE);
            case SECRETS_DONE -> stamp(pixels, room, CHECK, GREEN);
            case FAILED -> stamp(pixels, room, CROSS, RED);
            default -> {
            }
        }
    }

    private void stamp(byte[] pixels, PlacedRoom room, String[] glyph, byte colour) {
        Cell cell = room.cells().getFirst();
        int left = cellX(cell.col()) + (roomSize - glyph[0].length()) / 2;
        int top = cellZ(cell.row()) + (roomSize - glyph.length) / 2;
        for (int row = 0; row < glyph.length; row++) {
            for (int col = 0; col < glyph[row].length(); col++) {
                if (glyph[row].charAt(col) == '#') set(pixels, left + col, top + row, colour);
            }
        }
    }

    private static void fill(byte[] pixels, int x, int z, int width, int height, byte colour) {
        for (int dz = 0; dz < height; dz++) {
            for (int dx = 0; dx < width; dx++) set(pixels, x + dx, z + dz, colour);
        }
    }

    private static void set(byte[] pixels, int x, int z, byte colour) {
        if (x < 0 || z < 0 || x >= SIZE || z >= SIZE) return;
        pixels[x + z * SIZE] = colour;
    }

    public static byte at(byte[] pixels, int x, int z) {
        return pixels[x + z * SIZE];
    }
}
