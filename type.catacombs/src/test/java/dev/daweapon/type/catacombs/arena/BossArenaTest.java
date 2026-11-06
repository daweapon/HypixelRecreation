package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.type.catacombs.engine.bonzo.BonzoArenaSpots;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossArenaTest {
    private static final Path BOSSES = Path.of("..", "configuration", "skyblock", "dungeons", "bosses");
    private static final Path ROOMS = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
    private static BossArena arena;

    @BeforeAll
    static void load() {
        Assumptions.assumeTrue(Files.isDirectory(BOSSES), "boss arenas not found next to the module");
        arena = BossArena.load(CatacombsFloor.FLOOR_ONE, BOSSES).orElseThrow();
    }

    @Test
    void theArenaSitsWhereHypixelHasIt() {
        assertEquals(-72, arena.minX());
        assertEquals(-14, arena.maxX());
        assertEquals(55, arena.minY());
        assertEquals(145, arena.maxY());
        assertEquals(-40, arena.minZ());
        assertEquals(49, arena.maxZ());
        assertTrue(BossArena.load(CatacombsFloor.FLOOR_TWO, BOSSES).isEmpty());
    }

    @Test
    void capturedLandmarksAreInPlace() {
        assertEquals(Block.SAND, arena.blockAt(-43, 70, 20), "sand floor under Bonzo's spawn");
        assertNull(arena.blockAt(-43, 71, 20), "Bonzo stands in air");
        assertNotNull(arena.blockAt(-43, 70, 34), "solid floor under the arrival spot");
        assertNull(arena.blockAt(-43, 71, 34));
        assertNull(arena.blockAt(-43, 72, 34));
        assertEquals(Block.BLACK_WOOL, arena.blockAt(-43, 71, 5), "the first exit gate is closed");
        assertEquals(Block.BLACK_TERRACOTTA, arena.blockAt(-43, 73, -5), "the second exit gate is closed");
        assertEquals("minecraft:nether_portal", arena.blockAt(-43, 62, -39).key().asString(), "exit portal to the Dungeon Hub");
        for (int[] at : BonzoArenaSpots.FIRST_GATE) assertNotNull(arena.blockAt(at[0], at[1], at[2]), "gate 1 block " + at[1]);
        for (int[] at : BonzoArenaSpots.SECOND_GATE) assertNotNull(arena.blockAt(at[0], at[1], at[2]), "gate 2 block " + at[1]);
        for (int[] at : BonzoArenaSpots.EXIT_PORTAL) {
            Block block = arena.blockAt(at[0], at[1], at[2]);
            if (block != null && block.key().asString().equals("minecraft:nether_portal")) {
                assertTrue(BonzoArenaSpots.inExitPortal(at[0], at[1], at[2]));
            }
        }
    }

    @Test
    void hypixelsMarkerBlocksAreLeftOut() {
        assertNull(arena.blockAt(-1, 1, 0), "the marker bed at the bottom of the world");
        assertNull(arena.blockAt(-71, 146, -21), "the marker blocks above the roof");
    }

    @Test
    void everyFloorOneLayoutLeavesRoomForTheArena() {
        Assumptions.assumeTrue(Files.isDirectory(ROOMS), "room templates not found next to the module");
        RoomTemplateLibrary library = RoomTemplateLibrary.fromDirectory(ROOMS);
        for (long seed = 0; seed < 40; seed++) {
            FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, seed, library);
            assertFalse(arena.overlaps(new CatacombsBlockPlan(layout)), "seed " + seed);
        }
        FloorLayout seven = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, 1, library);
        assertTrue(arena.overlaps(new CatacombsBlockPlan(seven)), "bigger floors would overlap, so they keep the placeholder");
    }
}
