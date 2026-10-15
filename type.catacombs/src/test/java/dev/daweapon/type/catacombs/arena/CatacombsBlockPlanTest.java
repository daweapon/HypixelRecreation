package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsBlockPlanTest {
    private static FloorLayout layout;
    private static CatacombsBlockPlan plan;

    @BeforeAll
    static void generate() {
        Path rooms = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
        Assumptions.assumeTrue(Files.isDirectory(rooms), "room templates not found next to the module");
        layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 42L, RoomTemplateLibrary.fromDirectory(rooms));
        plan = new CatacombsBlockPlan(layout);
    }

    private static final int FLOOR_Y = CellGeometry.FLOOR_Y;

    @Test
    void doorFrameIsThreeWideAndFourTall() {
        LayoutDoor door = layout.doors().getFirst();
        DoorFrame frame = DoorFrame.of(door);
        assertEquals(DoorFrame.WIDTH * DoorFrame.HEIGHT, frame.blocks().size());
        assertEquals(FLOOR_Y + 1, frame.minY());
        assertEquals(FLOOR_Y + DoorFrame.HEIGHT, frame.maxY());
    }

    @Test
    void witherAndBloodDoorsAreFilledAndPlainDoorsAreOpen() {
        for (LayoutDoor door : layout.doors()) {
            DoorFrame frame = DoorFrame.of(door);
            CatacombsBlockPlan.Tile expected = switch (door.type()) {
                case PLAIN -> CatacombsBlockPlan.Tile.AIR;
                case WITHER -> CatacombsBlockPlan.Tile.WITHER_DOOR;
                case BLOOD -> CatacombsBlockPlan.Tile.BLOOD_DOOR;
            };
            for (int[] at : frame.blocks()) assertEquals(expected, plan.tileAt(at[0], at[1], at[2]), door.type() + " at " + at[0] + "," + at[2]);
        }
    }

    @Test
    void theWallAboveADoorwayStaysClosed() {
        LayoutDoor door = layout.doors().getFirst();
        DoorFrame frame = DoorFrame.of(door);
        assertEquals(CatacombsBlockPlan.Tile.WALL, plan.tileAt(frame.minX(), frame.maxY() + 1, frame.minZ()));
    }

    @Test
    void roomFloorsAreStoneBrickWithAMarkerAtTheCentre() {
        PlacedRoom start = layout.startRoom();
        CellGeometry.BlockPosition centre = CellGeometry.roomCenter(start.cells().getFirst());
        assertEquals(CatacombsBlockPlan.Tile.MARKER_FLOOR, plan.tileAt(centre.x(), FLOOR_Y, centre.z()));
        assertEquals(CatacombsBlockPlan.Tile.MARKER_PILLAR, plan.tileAt(centre.x(), FLOOR_Y + 1, centre.z()));
        assertEquals(CatacombsBlockPlan.Tile.FLOOR, plan.tileAt(centre.x() + 10, FLOOR_Y, centre.z()));
        assertEquals(CatacombsBlockPlan.Tile.AIR, plan.tileAt(centre.x() + 10, FLOOR_Y + 1, centre.z()));
    }

    @Test
    void theDungeonIsWalledAndRoofed() {
        assertEquals(CatacombsBlockPlan.Tile.WALL, plan.tileAt(plan.minX(), FLOOR_Y + 1, plan.minZ() + 5));
        assertEquals(CatacombsBlockPlan.Tile.WALL, plan.tileAt(plan.maxX(), FLOOR_Y + 1, plan.maxZ() - 5));
        assertEquals(CatacombsBlockPlan.Tile.CEILING, plan.tileAt(CellGeometry.ORIGIN_X + 5, plan.maxY(), CellGeometry.ORIGIN_Z + 5));
        assertEquals(CatacombsBlockPlan.Tile.BEDROCK, plan.tileAt(CellGeometry.ORIGIN_X + 5, plan.minY(), CellGeometry.ORIGIN_Z + 5));
        assertEquals(CatacombsBlockPlan.Tile.AIR, plan.tileAt(plan.maxX() + 5, FLOOR_Y, CellGeometry.ORIGIN_Z + 5));
    }

    @Test
    void everyRoomHasAMarkerColour() {
        for (PlacedRoom room : layout.rooms()) {
            CellGeometry.BlockPosition centre = CellGeometry.roomCenter(room.cells().getFirst());
            assertEquals(room.color(), plan.markerColourAt(centre.x(), centre.z()));
        }
    }

    @Test
    void theEntranceSpawnIsClearOfTheMarkerPillar() {
        PlacedRoom start = layout.startRoom();
        CellGeometry.BlockPosition centre = CellGeometry.roomCenter(start.cells().getFirst());
        int spawnZ = centre.z() + CatacombsBlockPlan.MARKER_RADIUS + 2;
        assertTrue(plan.tileAt(centre.x(), FLOOR_Y + 1, spawnZ) == CatacombsBlockPlan.Tile.AIR);
    }
}
