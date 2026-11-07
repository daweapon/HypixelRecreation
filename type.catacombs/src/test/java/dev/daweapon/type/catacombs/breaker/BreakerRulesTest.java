package dev.daweapon.type.catacombs.breaker;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreakerRulesTest {
    private static RoomTemplateLibrary library;

    @BeforeAll
    static void load() {
        Path rooms = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
        Assumptions.assumeTrue(Files.isDirectory(rooms), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(rooms);
    }

    @Test
    void ordinaryRoomBlocksBreakButRunMechanismsAndHardBlocksDoNot() {
        assertTrue(BreakerRules.breakable(Block.STONE_BRICKS));
        assertTrue(BreakerRules.breakable(Block.OAK_PLANKS));
        assertTrue(BreakerRules.breakable(Block.CRACKED_STONE_BRICKS));
        assertFalse(BreakerRules.breakable(Block.AIR));
        assertFalse(BreakerRules.breakable(Block.WATER));
        assertFalse(BreakerRules.breakable(Block.BEDROCK));
        assertFalse(BreakerRules.breakable(Block.BARRIER));
        assertFalse(BreakerRules.breakable(Block.CHEST));
        assertFalse(BreakerRules.breakable(Block.TRAPPED_CHEST));
        assertFalse(BreakerRules.breakable(Block.PLAYER_HEAD));
        assertFalse(BreakerRules.breakable(Block.SKELETON_SKULL));
        assertFalse(BreakerRules.breakable(Block.LEVER));
        assertFalse(BreakerRules.breakable(Block.OAK_SIGN));
    }

    @Test
    void onlyExploredRoomInteriorsAndTheWitherLordsArenaAllowBreaking() {
        Block stone = Block.STONE_BRICKS;
        assertTrue(BreakerRules.allows(stone, BreakerRules.Area.ROOM, false, CatacombsFloor.FLOOR_ONE));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.ROOM, true, CatacombsFloor.FLOOR_ONE));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.ROOM_EDGE, false, CatacombsFloor.FLOOR_ONE));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.PUZZLE_ROOM, false, CatacombsFloor.FLOOR_ONE));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.UNEXPLORED_ROOM, false, CatacombsFloor.FLOOR_ONE));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.OUTSIDE, false, CatacombsFloor.FLOOR_SEVEN));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.BOSS_ARENA, false, CatacombsFloor.FLOOR_ONE));
        assertFalse(BreakerRules.allows(stone, BreakerRules.Area.BOSS_ARENA, false, CatacombsFloor.FLOOR_SIX));
        assertTrue(BreakerRules.allows(stone, BreakerRules.Area.BOSS_ARENA, false, CatacombsFloor.FLOOR_SEVEN));
        assertFalse(BreakerRules.allows(Block.BEDROCK, BreakerRules.Area.ROOM, false, CatacombsFloor.FLOOR_ONE));
    }

    @Test
    void roomCentresAreOpenButEdgesPuzzlesAndUnexploredRoomsAreNot() {
        for (long seed = 0; seed < 10; seed++) {
            FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_THREE, CatacombsMode.NORMAL, seed, library);
            for (PlacedRoom room : layout.rooms()) {
                for (Cell cell : room.cells()) {
                    CellGeometry.BlockPosition centre = CellGeometry.roomCenter(cell);
                    BreakerRules.Area expected = room.kind() == RoomKind.PUZZLE ? BreakerRules.Area.PUZZLE_ROOM : BreakerRules.Area.ROOM;
                    assertEquals(expected, BreakerRules.area(layout, _ -> true, centre.x(), centre.z()), "seed " + seed + " " + cell);
                    BreakerRules.Area unexplored = BreakerRules.area(layout, _ -> false, centre.x(), centre.z());
                    assertTrue(unexplored == BreakerRules.Area.UNEXPLORED_ROOM || unexplored == BreakerRules.Area.PUZZLE_ROOM);
                    if (room.kind() == RoomKind.PUZZLE) continue;
                    CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(cell);
                    boolean westIsSameRoom = room.occupies(new Cell(cell.col() - 1, cell.row()));
                    BreakerRules.Area west = BreakerRules.area(layout, _ -> true, origin.x(), centre.z());
                    assertEquals(westIsSameRoom ? BreakerRules.Area.ROOM : BreakerRules.Area.ROOM_EDGE, west, "seed " + seed + " " + cell);
                    BreakerRules.Area gap = BreakerRules.area(layout, _ -> true, origin.x() + CellGeometry.ROOM_SIZE, centre.z());
                    boolean eastIsSameRoom = room.occupies(new Cell(cell.col() + 1, cell.row()));
                    assertEquals(eastIsSameRoom ? BreakerRules.Area.ROOM : BreakerRules.Area.ROOM_EDGE, gap, "seed " + seed + " " + cell);
                    assertEquals(BreakerRules.Area.ROOM,
                            BreakerRules.area(layout, _ -> true, origin.x() + BreakerRules.BOUNDARY_MARGIN, origin.z() + BreakerRules.BOUNDARY_MARGIN));
                }
            }
        }
    }

    @Test
    void outsideTheRoomGridIsClosed() {
        FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 1, library);
        assertEquals(BreakerRules.Area.OUTSIDE, BreakerRules.area(layout, _ -> true, CellGeometry.ORIGIN_X - 5, CellGeometry.ORIGIN_Z + 5));
        assertEquals(BreakerRules.Area.OUTSIDE, BreakerRules.area(layout, _ -> true,
                CellGeometry.ORIGIN_X + layout.width() * CellGeometry.CELL_PITCH + 3, CellGeometry.ORIGIN_Z + 5));
    }
}
