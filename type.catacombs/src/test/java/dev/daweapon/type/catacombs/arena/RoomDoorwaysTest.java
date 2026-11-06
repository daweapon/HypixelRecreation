package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.DoorSocket;
import dev.daweapon.dungeons.catacombs.layout.DoorType;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.RoomShape;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import dev.daweapon.dungeons.catacombs.layout.Side;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomDoorwaysTest {
    static final Path ROOMS = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
    private static RoomTemplateLibrary library;
    private static RoomStructures structures;
    private static DoorPieces pieces;
    private static List<RoomTemplate> real;

    @BeforeAll
    static void load() {
        Assumptions.assumeTrue(Files.isDirectory(ROOMS), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(ROOMS);
        structures = new RoomStructures(ROOMS);
        pieces = DoorPieces.load(ROOMS.getParent().resolve("door_pieces.json"));
        real = library.all().stream().filter(template -> template.blocks().isPresent()).toList();
    }

    @Test
    void capturedDoorPiecesReadWithKnownBlocksAndAnOpenPassage() {
        assertTrue(pieces.pieces().size() >= 50, pieces.pieces().size() + " door pieces");
        Set<String> types = Set.of("PLAIN", "WITHER", "BLOOD", "ENTRANCE");
        assertTrue(pieces.pieces().stream().anyMatch(piece -> piece.type().equals("PLAIN")));
        for (DoorPieces.Piece piece : pieces.pieces()) {
            assertTrue(types.contains(piece.type()), piece.type());
            for (int depth = -DoorPieces.REACH; depth <= DoorPieces.REACH; depth++) {
                for (int along = -DoorPieces.REACH; along <= DoorPieces.REACH; along++) {
                    for (int y = 0; y < DoorPieces.HEIGHT; y++) {
                        Block block = piece.at(depth, along, y);
                        boolean roomFloor = Math.abs(depth) == DoorPieces.REACH && y == 0;
                        if (roomFloor) continue;
                        assertNotNull(block, "unknown block in a door piece at " + depth + "," + along + "," + y);
                        if (Math.abs(depth) <= 1 && Math.abs(along) <= 1 && y >= 1 && y <= 4) assertTrue(block.air(), "passage holds " + block);
                    }
                }
            }
        }
    }

    @Test
    void unusedSocketsShowTheRoomsOwnClosedWallInEveryRotation() {
        for (RoomTemplate template : real) {
            for (Rotation rotation : rotations(template)) {
                FloorLayout layout = surrounded(template, rotation, false);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures, false, pieces);
                RealRoom room = blocks.realRooms().stream().filter(each -> each.room().index() == 0).findFirst().orElseThrow();
                for (DoorSocket socket : template.doorSockets(rotation)) {
                    String context = template.id() + " " + rotation + " " + socket;
                    Doorway doorway = Doorway.of(layout.room(0), socket);
                    int closedColumns = 0;
                    for (int depth = -DoorPieces.REACH; depth <= -1; depth++) {
                        for (int along = -DoorPieces.REACH; along <= DoorPieces.REACH; along++) {
                            for (int y = 0; y < DoorPieces.HEIGHT; y++) {
                                int[] at = doorway.at(depth, along);
                                Block own = room.blockAt(at[0], CellGeometry.FLOOR_Y + y, at[1]);
                                Block shown = blocks.blockAt(at[0], CellGeometry.FLOOR_Y + y, at[1]);
                                assertEquals(own == null || own.air() ? null : own, shown, context + " at " + depth + "," + along + "," + y);
                            }
                        }
                    }
                    for (int along = -1; along <= 1; along++) {
                        int[] at = doorway.at(-1, along);
                        Block feet = blocks.blockAt(at[0], CellGeometry.FLOOR_Y + 1, at[1]);
                        Block head = blocks.blockAt(at[0], CellGeometry.FLOOR_Y + 2, at[1]);
                        if ((feet != null && feet.solid()) || (head != null && head.solid())) closedColumns++;
                    }
                    assertEquals(3, closedColumns, context + ": the outer wall has a gap where no door is");
                }
            }
        }
    }

    @Test
    void usedSocketsGetADoorPieceWithAnOpenPassageInEveryRotation() {
        for (RoomTemplate template : real) {
            for (Rotation rotation : rotations(template)) {
                FloorLayout layout = surrounded(template, rotation, true);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures, false, pieces);
                for (LayoutDoor door : layout.doors()) {
                    String context = template.id() + " " + rotation + " " + door;
                    DoorPieces.Piece piece = pieces.pick(layout, door);
                    Doorway doorway = Doorway.of(door);
                    for (int depth = -DoorPieces.REACH; depth <= DoorPieces.REACH; depth++) {
                        for (int along = -1; along <= 1; along++) {
                            for (int y = 1; y <= 3; y++) {
                                int[] at = doorway.at(depth, along);
                                Block block = blocks.blockAt(at[0], CellGeometry.FLOOR_Y + y, at[1]);
                                assertTrue(block == null || !block.solid(), context + " passage blocked at " + depth + "," + along + "," + y + " by " + block);
                            }
                        }
                        for (int y = 1; y <= 4; y++) {
                            int[] at = doorway.at(depth, DoorPieces.REACH);
                            Block expected = piece.at(depth, DoorPieces.REACH, y);
                            Block shown = blocks.blockAt(at[0], CellGeometry.FLOOR_Y + y, at[1]);
                            assertEquals(expected == null || expected.air() ? null : expected.name(), shown == null ? null : shown.name(), context + " frame");
                        }
                    }
                }
            }
        }
    }

    static List<Rotation> rotations(RoomTemplate template) {
        return template.rotationLock().map(List::of).orElse(List.of(Rotation.values()));
    }

    /** A door's frame of reference: the seam centre, the step out of the lower room, and the step to its right. */
    record Doorway(int seamX, int seamZ, int outX, int outZ) {
        static Doorway of(LayoutDoor door) {
            CellGeometry.BlockPosition seam = CellGeometry.doorPosition(door.from(), door.to());
            boolean eastWest = door.from().row() == door.to().row();
            return new Doorway(seam.x(), seam.z(), eastWest ? 1 : 0, eastWest ? 0 : 1);
        }

        /** The room's socket seen from inside the room: negative depth is into the room. */
        static Doorway of(PlacedRoom room, DoorSocket socket) {
            Cell cell = new Cell(room.origin().col() + socket.cell().col(), room.origin().row() + socket.cell().row());
            Cell next = neighbour(cell, socket.side());
            CellGeometry.BlockPosition seam = CellGeometry.doorPosition(cell, next);
            int outX = next.col() - cell.col(), outZ = next.row() - cell.row();
            return new Doorway(seam.x(), seam.z(), outX, outZ);
        }

        int[] at(int depth, int along) {
            return new int[]{seamX + outX * depth - outZ * along, seamZ + outZ * depth + outX * along};
        }
    }

    static Cell neighbour(Cell cell, Side side) {
        return switch (side) {
            case NORTH -> new Cell(cell.col(), cell.row() - 1);
            case EAST -> new Cell(cell.col() + 1, cell.row());
            case SOUTH -> new Cell(cell.col(), cell.row() + 1);
            case WEST -> new Cell(cell.col() - 1, cell.row());
        };
    }

    /** The template at cell (1,1) with one-cell filler rooms all round it, and a plain door at every socket when asked. */
    static FloorLayout surrounded(RoomTemplate template, Rotation rotation, boolean doors) {
        RoomTemplate filler = library.templates(RoomKind.NORMAL, RoomShape.ONE_BY_ONE).getFirst();
        Cell origin = new Cell(1, 1);
        List<Cell> cells = template.shape().cells(rotation).stream().map(cell -> new Cell(cell.col() + 1, cell.row() + 1)).toList();
        int width = template.shape().widthCells(rotation) + 2;
        int height = template.shape().heightCells(rotation) + 2;
        List<PlacedRoom> rooms = new ArrayList<>();
        rooms.add(new PlacedRoom(0, template, rotation, origin, cells, true));
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = new Cell(col, row);
                if (!cells.contains(cell)) rooms.add(new PlacedRoom(rooms.size(), filler, Rotation.DEG_0, cell, List.of(cell), false));
            }
        }
        List<LayoutDoor> placed = new ArrayList<>();
        if (doors) {
            for (DoorSocket socket : template.doorSockets(rotation)) {
                Cell cell = new Cell(socket.cell().col() + 1, socket.cell().row() + 1);
                Cell next = neighbour(cell, socket.side());
                int other = rooms.stream().filter(room -> room.cells().contains(next)).findFirst().orElseThrow().index();
                boolean forward = next.col() > cell.col() || next.row() > cell.row();
                placed.add(forward ? new LayoutDoor(cell, next, 0, other, DoorType.PLAIN) : new LayoutDoor(next, cell, other, 0, DoorType.PLAIN));
            }
        }
        return new FloorLayout(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 7L, width, height, rooms, placed, List.of(1));
    }

    static RoomTemplateLibrary library() {
        return library;
    }

    static RoomStructures structures() {
        return structures;
    }
}
