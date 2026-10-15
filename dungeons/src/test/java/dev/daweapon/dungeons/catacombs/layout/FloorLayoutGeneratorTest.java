package dev.daweapon.dungeons.catacombs.layout;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FloorLayoutGeneratorTest {
    private final RoomTemplateLibrary library = PlaceholderLibrary.load();

    private FloorLayout generate(CatacombsFloor floor, long seed) {
        return FloorLayoutGenerator.generate(floor, CatacombsMode.NORMAL, seed, library);
    }

    private static Set<Integer> reachableWithoutLockedDoors(FloorLayout layout) {
        Set<Integer> reached = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        int start = layout.startRoom().index();
        reached.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            int room = queue.remove();
            for (LayoutDoor door : layout.doorsOf(room)) {
                if (door.plain() && reached.add(door.otherRoom(room))) {
                    queue.add(door.otherRoom(room));
                }
            }
        }
        return reached;
    }

    private static Set<Integer> reachableThroughAllDoors(FloorLayout layout) {
        Set<Integer> reached = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        int start = layout.startRoom().index();
        reached.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            int room = queue.remove();
            for (LayoutDoor door : layout.doorsOf(room)) {
                if (reached.add(door.otherRoom(room))) {
                    queue.add(door.otherRoom(room));
                }
            }
        }
        return reached;
    }

    private void assertValid(FloorLayout layout) {
        String context = layout.floor() + " seed " + layout.seed() + "\n" + layout.render();
        int width = layout.floor().dungeonSize().width();
        int height = layout.floor().dungeonSize().height();
        assertEquals(width, layout.width(), context);
        assertEquals(height, layout.height(), context);

        Set<Cell> covered = new HashSet<>();
        for (PlacedRoom room : layout.rooms()) {
            assertEquals(room.template().shape().cellCount(), room.cells().size(), context);
            for (Cell cell : room.cells()) {
                assertTrue(cell.col() >= 0 && cell.col() < width && cell.row() >= 0 && cell.row() < height, context);
                assertTrue(covered.add(cell), "overlap at " + cell + "\n" + context);
            }
            List<Cell> expected = room.shape().cells(room.rotation()).stream()
                    .map(cell -> cell.translate(room.origin().col(), room.origin().row()))
                    .toList();
            assertEquals(expected, room.cells(), context);
        }
        assertEquals(width * height, covered.size(), context);

        List<PlacedRoom> puzzles = layout.roomsOfKind(RoomKind.PUZZLE);
        assertTrue(puzzles.size() >= 2, context);
        assertEquals(puzzles.size(), puzzles.stream().map(room -> room.template().id()).distinct().count(), context);
        assertEquals(1, layout.roomsOfKind(RoomKind.MINIBOSS).size(), context);
        assertEquals(layout.floor().ordinal() >= 3 ? 1 : 0, layout.roomsOfKind(RoomKind.TRAP).size(), context);
        assertEquals(1, layout.roomsOfKind(RoomKind.FAIRY).size(), context);
        assertEquals(1, layout.roomsOfKind(RoomKind.BLOOD).size(), context);
        assertEquals(1, layout.roomsOfKind(RoomKind.ENTRANCE).size(), context);

        PlacedRoom start = layout.startRoom();
        assertEquals(RoomKind.ENTRANCE, start.kind(), context);
        assertEquals(height - 1, start.origin().row(), context);
        assertEquals(RoomKind.BLOOD, layout.bloodRoom().kind(), context);

        int fairyPosition = layout.criticalPath().indexOf(layout.fairyRoom().index());
        assertTrue(fairyPosition > 0 && fairyPosition < layout.criticalPath().size() - 1, context);

        List<LayoutDoor> bloodDoors = layout.doorsOf(layout.bloodRoom().index());
        assertEquals(1, bloodDoors.size(), context);
        assertTrue(bloodDoors.getFirst().bloodDoor(), context);
        assertEquals((int) layout.criticalPath().get(layout.criticalPath().size() - 2), bloodDoors.getFirst().otherRoom(layout.bloodRoom().index()), context);
        assertTrue(layout.doors().stream().filter(LayoutDoor::witherDoor).count() >= 1, context);

        Set<Integer> unlocked = reachableWithoutLockedDoors(layout);
        assertFalse(unlocked.contains(layout.bloodRoom().index()), context);
        assertFalse(unlocked.contains(layout.fairyRoom().index()), context);
        assertEquals(layout.totalRooms(), reachableThroughAllDoors(layout).size(), context);

        for (int step = 0; step < layout.criticalPath().size() - 1; step++) {
            int from = layout.criticalPath().get(step);
            int to = layout.criticalPath().get(step + 1);
            boolean firstDoor = step == 0;
            assertTrue(layout.doorsOf(from).stream().anyMatch(door -> door.connects(to) && (firstDoor || !door.plain())), context);
        }
        for (LayoutDoor door : layout.doors()) {
            assertTrue(door.from().adjacentTo(door.to()), context);
            assertNotEquals(door.fromRoom(), door.toRoom(), context);
        }

        assertEquals(layout.rooms().size(), layout.totalRooms());
        assertTrue(layout.totalSecrets() > 0, context);
        assertTrue(layout.requiredMobRoomCount() > 0, context);
    }

    @Test
    void entranceLayoutsAreValidForOneHundredSeeds() {
        for (long seed = 0; seed < 100; seed++) {
            assertValid(generate(CatacombsFloor.ENTRANCE, seed));
        }
    }

    @Test
    void floorOneLayoutsAreValidForOneHundredSeeds() {
        for (long seed = 0; seed < 100; seed++) {
            FloorLayout layout = generate(CatacombsFloor.FLOOR_ONE, seed);
            assertEquals(4, layout.width());
            assertEquals(5, layout.height());
            assertValid(layout);
        }
    }

    @Test
    void everyFloorGeneratesValidLayouts() {
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            for (long seed = 0; seed < 20; seed++) {
                assertValid(generate(floor, seed));
            }
        }
    }

    @Test
    void floorSevenLayoutsAreValidAndHaveMorePuzzles() {
        for (long seed = 0; seed < 25; seed++) {
            FloorLayout layout = generate(CatacombsFloor.FLOOR_SEVEN, seed);
            assertValid(layout);
            assertTrue(layout.roomsOfKind(RoomKind.PUZZLE).size() >= 3, layout.render());
        }
    }

    @Test
    void sameSeedProducesIdenticalLayout() {
        for (long seed = 0; seed < 20; seed++) {
            FloorLayout first = generate(CatacombsFloor.FLOOR_ONE, seed);
            FloorLayout second = generate(CatacombsFloor.FLOOR_ONE, seed);
            assertEquals(first.render(), second.render());
            assertEquals(first.rooms(), second.rooms());
            assertEquals(first.doors(), second.doors());
            assertEquals(first.criticalPath(), second.criticalPath());
        }
    }

    @Test
    void differentSeedsProduceDifferentLayouts() {
        List<String> renders = new ArrayList<>();
        for (long seed = 0; seed < 100; seed++) {
            renders.add(generate(CatacombsFloor.FLOOR_ONE, seed).render());
        }
        assertTrue(renders.stream().distinct().count() > 50);
    }

    @Test
    void largerRoomsAppearAcrossSeeds() {
        Set<RoomShape> shapes = new HashSet<>();
        for (long seed = 0; seed < 100; seed++) {
            generate(CatacombsFloor.FLOOR_SEVEN, seed).rooms().forEach(room -> shapes.add(room.shape()));
        }
        assertEquals(Set.of(RoomShape.values()), shapes);
    }

    @Test
    void masterModeUsesTheSameGrid() {
        FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_FIVE, CatacombsMode.MASTER, 7L, library);
        assertEquals(6, layout.width());
        assertEquals(5, layout.height());
        assertEquals(CatacombsMode.MASTER, layout.mode());
    }
}
