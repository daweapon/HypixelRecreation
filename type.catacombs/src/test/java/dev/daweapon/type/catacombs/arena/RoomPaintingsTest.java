package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomPaintingsTest {
    private static final Path DUNGEONS = Path.of("..", "configuration", "skyblock", "dungeons");
    private static final Path ROOMS = DUNGEONS.resolve("rooms");
    private static RoomTemplateLibrary library;
    private static RoomStructures structures;
    private static RoomPaintings paintings;

    @BeforeAll
    static void load() {
        Assumptions.assumeTrue(Files.isDirectory(ROOMS), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(ROOMS);
        structures = new RoomStructures(ROOMS);
        paintings = RoomPaintings.load(DUNGEONS.resolve("room_features.json"));
    }

    private static boolean open(Block block) {
        return block == null || !block.collisionShape().intersectBox(new Vec(0.5, 0, 0.5), new BoundingBox(0.6, 1, 0.6));
    }

    private static boolean solid(Block block) {
        return block != null && block.isSolid();
    }

    @Test
    void everyPaintingHangsInItsWallInEveryTurn() {
        int checked = 0;
        assertFalse(paintings.templateIds().isEmpty());
        for (String id : paintings.templateIds()) {
            RoomTemplate template = library.find(id).orElseThrow(() -> new AssertionError(id + " is not a room template"));
            for (Rotation rotation : Rotation.values()) {
                PlacedRoom room = RoomFeaturesTest.around(library, template, rotation).room(0);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(RoomFeaturesTest.around(library, template, rotation), structures);
                for (RoomPaintings.Painting painting : paintings.of(room)) {
                    checked++;
                    String context = template.id() + " " + rotation + " " + painting;
                    Direction facing = painting.facing();
                    Set<String> footprint = new HashSet<>();
                    for (int[] at : painting.covered()) {
                        assertEquals(room.origin(), CellGeometry.cellAt(at[0], at[2]).orElseThrow(), context);
                        assertTrue(open(blocks.blockAt(at[0], at[1], at[2])), context + " hangs inside a block at " + at[0] + "," + at[1] + "," + at[2]);
                        footprint.add((at[0] - facing.normalX()) + "," + at[1] + "," + (at[2] - facing.normalZ()));
                    }
                    for (String wall : footprint) {
                        String[] parts = wall.split(",");
                        int x = Integer.parseInt(parts[0]), y = Integer.parseInt(parts[1]), z = Integer.parseInt(parts[2]);
                        int[][] around = {{0, 1, 0}, {0, -1, 0}, {facing.normalZ(), 0, facing.normalX()}, {-facing.normalZ(), 0, -facing.normalX()}};
                        for (int[] step : around) {
                            int nx = x + step[0], ny = y + step[1], nz = z + step[2];
                            if (footprint.contains(nx + "," + ny + "," + nz)) continue;
                            assertTrue(solid(blocks.blockAt(nx, ny, nz)), context + " wall around it is open at " + nx + "," + ny + "," + nz);
                        }
                    }
                }
            }
        }
        assertTrue(checked > 0, "no paintings were placed");
    }

    @Test
    void thePaintingRoomsPaintingCoversTheSecretPassageInEveryTurn() {
        RoomTemplate template = library.find("painting").orElseThrow();
        for (Rotation rotation : Rotation.values()) {
            PlacedRoom room = RoomFeaturesTest.around(library, template, rotation).room(0);
            CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(RoomFeaturesTest.around(library, template, rotation), structures);
            List<RoomPaintings.Painting> placed = paintings.of(room);
            assertEquals(1, placed.size(), rotation.toString());
            RoomPaintings.Painting painting = placed.getFirst();
            assertEquals("minecraft:graham", painting.variant());
            List<Direction> clockwise = List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH);
            assertEquals(clockwise.get(rotation.quarterTurns()), painting.facing(), rotation.toString());
            List<int[]> covered = painting.covered();
            assertEquals(2, covered.size());
            for (int[] at : covered) {
                int x = at[0] - painting.facing().normalX(), z = at[2] - painting.facing().normalZ();
                Block behind = blocks.blockAt(x, at[1], z);
                assertTrue(open(behind), rotation + " the hole behind the painting is filled with " + behind);
                assertFalse(solid(blocks.blockAt(x - painting.facing().normalX(), at[1], z - painting.facing().normalZ())),
                        rotation + " the passage behind the hole is blocked");
            }
        }
    }
}
