package dev.daweapon.dungeons.catacombs.map;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonMapCanvasTest {
    private static FloorLayout layout;
    private static DungeonMapCanvas canvas;

    @BeforeAll
    static void generate() {
        Path rooms = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
        Assumptions.assumeTrue(Files.isDirectory(rooms), "room templates not found next to the module");
        layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 42L, RoomTemplateLibrary.fromDirectory(rooms));
        canvas = new DungeonMapCanvas(layout);
    }

    private static Map<Integer, DungeonRoomState> undiscovered() {
        Map<Integer, DungeonRoomState> states = new HashMap<>();
        layout.rooms().forEach(room -> states.put(room.index(), DungeonRoomState.UNDISCOVERED));
        return states;
    }

    private static byte corner(byte[] pixels, PlacedRoom room) {
        Cell cell = room.cells().getFirst();
        return DungeonMapCanvas.at(pixels, canvas.cellX(cell.col()) + 1, canvas.cellZ(cell.row()) + 1);
    }

    @Test
    void floorOneUsesHypixelsGeometry() {
        assertEquals(18, canvas.roomSize());
        assertEquals(22, canvas.cellX(0));
        assertEquals(11, canvas.cellZ(0));
    }

    @Test
    void onlyTheStartRoomAndItsDoorNeighboursShowAtFirst() {
        Map<Integer, DungeonRoomState> states = undiscovered();
        PlacedRoom start = layout.startRoom();
        states.put(start.index(), DungeonRoomState.DISCOVERED);
        MapRoomView[] views = MapRoomView.views(layout, states, room -> false);
        byte[] pixels = canvas.render(views);

        assertEquals(DungeonMapCanvas.ENTRANCE, corner(pixels, start));
        for (PlacedRoom room : layout.rooms()) {
            if (room == start) continue;
            boolean neighbour = layout.doorsOf(start.index()).stream().anyMatch(door -> door.otherRoom(start.index()) == room.index());
            assertEquals(neighbour ? MapRoomView.UNKNOWN : MapRoomView.HIDDEN, views[room.index()]);
            assertEquals(neighbour ? DungeonMapCanvas.UNKNOWN : DungeonMapCanvas.EMPTY, corner(pixels, room));
        }
    }

    @Test
    void clearedRoomsGetAWhiteCheckAndGreenOnceEverySecretIsFound() {
        PlacedRoom room = layout.rooms().stream().filter(each -> each.kind() == RoomKind.NORMAL).findFirst().orElseThrow();
        Map<Integer, DungeonRoomState> states = undiscovered();
        states.put(room.index(), DungeonRoomState.COMPLETED);

        byte[] cleared = canvas.render(MapRoomView.views(layout, states, index -> false));
        byte[] allSecrets = canvas.render(MapRoomView.views(layout, states, index -> index == room.index()));

        assertTrue(contains(cleared, room, DungeonMapCanvas.WHITE));
        assertTrue(contains(allSecrets, room, DungeonMapCanvas.GREEN));
        assertEquals(DungeonMapCanvas.NORMAL, corner(cleared, room));
    }

    @Test
    void failedPuzzleShowsARedCross() {
        PlacedRoom puzzle = layout.roomsOfKind(RoomKind.PUZZLE).getFirst();
        Map<Integer, DungeonRoomState> states = undiscovered();
        states.put(puzzle.index(), DungeonRoomState.FAILED);
        byte[] pixels = canvas.render(MapRoomView.views(layout, states, index -> false));
        assertEquals(DungeonMapCanvas.PUZZLE, corner(pixels, puzzle));
        assertTrue(contains(pixels, puzzle, DungeonMapCanvas.RED));
    }

    @Test
    void witherDoorsAreBlackOnceEitherSideIsExplored() {
        LayoutDoor wither = layout.doors().stream().filter(LayoutDoor::witherDoor).findFirst().orElseThrow();
        Map<Integer, DungeonRoomState> states = undiscovered();
        states.put(wither.fromRoom(), DungeonRoomState.DISCOVERED);
        byte[] pixels = canvas.render(MapRoomView.views(layout, states, index -> false));
        Cell first = wither.from().col() + wither.from().row() <= wither.to().col() + wither.to().row() ? wither.from() : wither.to();
        boolean horizontal = wither.from().row() == wither.to().row();
        int x = horizontal ? canvas.cellX(first.col()) + canvas.roomSize() + 1 : canvas.cellX(first.col()) + canvas.roomSize() / 2;
        int z = horizontal ? canvas.cellZ(first.row()) + canvas.roomSize() / 2 : canvas.cellZ(first.row()) + canvas.roomSize() + 1;
        assertEquals(DungeonMapCanvas.BLACK, DungeonMapCanvas.at(pixels, x, z));
    }

    @Test
    void playerPositionsMapIntoTheirCell() {
        PlacedRoom start = layout.startRoom();
        Cell cell = start.cells().getFirst();
        double worldX = -200 + cell.col() * 32 + 15;
        double worldZ = -200 + cell.row() * 32 + 15;
        double x = canvas.pixelX(worldX);
        double z = canvas.pixelZ(worldZ);
        assertTrue(x >= canvas.cellX(cell.col()) && x < canvas.cellX(cell.col()) + canvas.roomSize());
        assertTrue(z >= canvas.cellZ(cell.row()) && z < canvas.cellZ(cell.row()) + canvas.roomSize());
    }

    private static boolean contains(byte[] pixels, PlacedRoom room, byte colour) {
        Cell cell = room.cells().getFirst();
        for (int dz = 0; dz < canvas.roomSize(); dz++) {
            for (int dx = 0; dx < canvas.roomSize(); dx++) {
                if (DungeonMapCanvas.at(pixels, canvas.cellX(cell.col()) + dx, canvas.cellZ(cell.row()) + dz) == colour) return true;
            }
        }
        return false;
    }
}
