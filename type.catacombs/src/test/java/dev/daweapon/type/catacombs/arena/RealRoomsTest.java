package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.MobSpawnPoint;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.RoomShape;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import dev.daweapon.dungeons.catacombs.layout.SecretAnchor;
import dev.daweapon.dungeons.catacombs.layout.Side;
import dev.daweapon.type.catacombs.crypt.CryptPlan;
import dev.daweapon.type.catacombs.secret.SecretSpot;
import dev.daweapon.type.catacombs.secret.SecretSpots;
import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicRotation;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealRoomsTest {
    private static final Path ROOMS = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
    private static RoomTemplateLibrary library;
    private static RoomStructures structures;
    private static List<RoomTemplate> real;

    @BeforeAll
    static void load() {
        Assumptions.assumeTrue(Files.isDirectory(ROOMS), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(ROOMS);
        structures = new RoomStructures(ROOMS);
        real = library.all().stream().filter(template -> template.blocks().isPresent()).toList();
    }

    @Test
    void everyRoomFileReadsWithOnlyKnownBlocksAndCoversItsCells() {
        assertEquals(16, real.size());
        for (RoomTemplate template : real) {
            Schematic schematic = structures.load(template.blocks().orElseThrow());
            assertEquals(0, schematic.unknownBlocks(), template.id());
            int width = CellGeometry.widthBlocks(template.shape(), Rotation.DEG_0);
            int depth = CellGeometry.depthBlocks(template.shape(), Rotation.DEG_0);
            assertTrue(template.structure().x() <= 0 && template.structure().x() + schematic.width() >= width, template.id());
            assertTrue(template.structure().z() <= 0 && template.structure().z() + schematic.length() >= depth, template.id());
        }
    }

    @Test
    void secretsStandPointsSpawnsAndCryptsLandOnTheRightBlocksInEveryRotation() {
        for (RoomTemplate template : real) {
            Schematic schematic = structures.load(template.blocks().orElseThrow());
            for (Rotation rotation : Rotation.values()) {
                FloorLayout layout = singleRoom(template, rotation);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
                PlacedRoom room = layout.room(0);
                assertTrue(blocks.isReal(room), template.id());
                String context = template.id() + " " + rotation;

                List<SecretSpot> spots = SecretSpots.of(layout).stream().filter(spot -> spot.room() == 0).toList();
                assertEquals(template.secretAnchors().size(), spots.size(), context);
                for (int index = 0; index < spots.size(); index++) {
                    SecretSpot spot = spots.get(index);
                    SecretAnchor anchor = template.secretAnchors().get(index);
                    Block world = blocks.blockAt(spot.x(), spot.y(), spot.z());
                    Block base = schematic.blockAt(anchor.x() - template.structure().x(),
                            CellGeometry.FLOOR_Y + anchor.y() - blocks.realRooms().getFirst().minY(),
                            anchor.z() - template.structure().z());
                    switch (anchor.type()) {
                        case CHEST -> {
                            assertNotNull(world, context + " chest " + index);
                            assertTrue(world.compare(Block.CHEST) || world.compare(Block.TRAPPED_CHEST), context + " chest " + index + " is " + world);
                            assertEquals(SchematicRotation.rotate(base, rotation.quarterTurns()).properties(), world.properties(), context);
                        }
                        case ESSENCE -> {
                            assertNotNull(world, context + " essence " + index);
                            assertTrue(world.name().contains("skull") || world.name().contains("head"), context + " essence is " + world);
                        }
                        case ITEM, BAT, REDSTONE_KEY -> assertTrue(world == null || !world.solid(), context + " " + anchor.type() + " inside " + world);
                    }
                }

                Pos stand = blocks.standPoint(room);
                assertStandable(blocks, stand.x(), stand.y(), stand.z(), context + " stand point");
                for (MobSpawnPoint spawn : template.mobSpawns()) {
                    CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
                    CellGeometry.PlanarPoint local = CellGeometry.rotateLocal(spawn.x(), spawn.z(), room.shape(), room.rotation());
                    assertStandable(blocks, origin.x() + local.x(), origin.y() + spawn.y(), origin.z() + local.z(), context + " " + spawn.id());
                }
                for (CryptPlan.Crypt crypt : CryptPlan.of(layout)) {
                    if (crypt.room() != 0) continue;
                    for (int dx = 0; dx < CryptPlan.TOMB_LENGTH; dx++) {
                        assertStandable(blocks, crypt.x() + dx + 0.5, crypt.y(), crypt.z() + 0.5, context + " crypt " + crypt.id());
                    }
                }
            }
        }
    }

    @Test
    void entranceAndFloorOneLayoutsPasteWithOpenDoorsAndNoErrors() {
        long blocksChecked = 0;
        long slowestNanos = 0;
        int realCells = 0;
        int cells = 0;
        for (CatacombsFloor floor : List.of(CatacombsFloor.ENTRANCE, CatacombsFloor.FLOOR_ONE)) {
            for (long seed = 0; seed < 60; seed++) {
                FloorLayout layout = FloorLayoutGenerator.generate(floor, CatacombsMode.NORMAL, seed, library);
                long started = System.nanoTime();
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
                for (int x = blocks.minX(); x <= blocks.maxX(); x++) {
                    for (int z = blocks.minZ(); z <= blocks.maxZ(); z++) {
                        for (int y = blocks.minY(); y <= blocks.maxY(); y++) {
                            blocks.blockAt(x, y, z);
                            blocksChecked++;
                        }
                    }
                }
                slowestNanos = Math.max(slowestNanos, System.nanoTime() - started);
                String context = floor + " seed " + seed + "\n" + layout.render();
                for (PlacedRoom room : layout.rooms()) {
                    cells += room.cells().size();
                    if (room.template().blocks().isPresent()) {
                        assertTrue(blocks.isReal(room), context);
                        realCells += room.cells().size();
                    }
                }
                for (RoomKind kind : List.of(RoomKind.ENTRANCE, RoomKind.FAIRY, RoomKind.BLOOD)) {
                    assertTrue(layout.roomsOfKind(kind).stream().allMatch(blocks::isReal), kind + "\n" + context);
                }
                for (LayoutDoor door : layout.doors()) assertDoorwayOpen(blocks, layout, door, context);
                Pos spawn = blocks.standPoint(layout.startRoom());
                assertStandable(blocks, spawn.x(), spawn.y(), spawn.z(), "entrance spawn " + context);
            }
        }
        assertTrue(realCells * 2 > cells, realCells + " of " + cells + " cells are real rooms");
        assertTrue(slowestNanos < 5_000_000_000L, "slowest full dungeon took " + slowestNanos / 1_000_000 + " ms for " + blocksChecked + " blocks");
    }

    private static void assertDoorwayOpen(CatacombsArenaBlocks blocks, FloorLayout layout, LayoutDoor door, String context) {
        CellGeometry.BlockPosition seam = CellGeometry.doorPosition(door.from(), door.to());
        boolean eastWest = door.from().row() == door.to().row();
        for (int wall = -1; wall <= 1; wall++) {
            for (int dy = 1; dy <= DoorFrame.HEIGHT; dy++) {
                int x = eastWest ? seam.x() + wall : seam.x();
                int z = eastWest ? seam.z() : seam.z() + wall;
                Block block = blocks.blockAt(x, CellGeometry.FLOOR_Y + dy, z);
                if (wall == 0 && !door.plain()) continue;
                assertNull(block, "doorway blocked at " + x + "," + (CellGeometry.FLOOR_Y + dy) + "," + z + " by " + block + " for " + door + "\n" + context);
            }
        }
        Side side = Side.between(door.from(), door.to());
        for (Cell cell : List.of(door.from(), door.to())) {
            PlacedRoom room = layout.roomAt(cell).orElseThrow();
            assertTrue(room.allowsDoor(cell, cell.equals(door.from()) ? side : side.opposite()), context);
        }
    }

    private static void assertStandable(CatacombsArenaBlocks blocks, double x, double y, double z, String context) {
        int bx = (int) Math.floor(x), by = (int) Math.floor(y), bz = (int) Math.floor(z);
        Block feet = blocks.blockAt(bx, by, bz);
        Block head = blocks.blockAt(bx, by + 1, bz);
        Block below = blocks.blockAt(bx, by - 1, bz);
        assertTrue(feet == null || !feet.solid(), context + " feet in " + feet + " at " + bx + "," + by + "," + bz);
        assertTrue(head == null || !head.solid(), context + " head in " + head);
        assertNotNull(below, context + " has nothing below at " + bx + "," + (by - 1) + "," + bz);
        assertFalse(below.air(), context);
    }

    private FloorLayout singleRoom(RoomTemplate template, Rotation rotation) {
        RoomTemplate filler = library.templates(RoomKind.NORMAL, RoomShape.ONE_BY_ONE).getFirst();
        List<Cell> cells = template.shape().cells(rotation);
        int width = template.shape().widthCells(rotation);
        int height = template.shape().heightCells(rotation);
        List<PlacedRoom> rooms = new ArrayList<>();
        rooms.add(new PlacedRoom(0, template, rotation, new Cell(0, 0), cells, true));
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = new Cell(col, row);
                if (!cells.contains(cell)) rooms.add(new PlacedRoom(rooms.size(), filler, Rotation.DEG_0, cell, List.of(cell), false));
            }
        }
        return new FloorLayout(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 7L, width, height, rooms, List.of(), List.of(0));
    }
}
