package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.DoorSocket;
import dev.daweapon.dungeons.catacombs.layout.DoorType;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The captured room spots (Mort, the Entrance gate, the Blood room portal, the fairies) land right in every turn. */
class RoomFeaturesTest {
    private static final Path DUNGEONS = Path.of("..", "configuration", "skyblock", "dungeons");
    private static final Path ROOMS = DUNGEONS.resolve("rooms");
    private static RoomTemplateLibrary library;
    private static RoomStructures structures;
    private static RoomFeatures features;

    @BeforeAll
    static void load() {
        Assumptions.assumeTrue(Files.isDirectory(ROOMS), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(ROOMS);
        structures = new RoomStructures(ROOMS);
        features = RoomFeatures.load(DUNGEONS.resolve("room_features.json"));
    }

    private static FloorLayout around(RoomTemplate template, Rotation rotation) {
        return around(library, template, rotation);
    }

    static FloorLayout around(RoomTemplateLibrary library, RoomTemplate template, Rotation rotation) {
        RoomTemplate filler = library.templates(RoomKind.NORMAL, RoomShape.ONE_BY_ONE).getFirst();
        Cell centre = new Cell(1, 1);
        List<PlacedRoom> rooms = new ArrayList<>();
        rooms.add(new PlacedRoom(0, template, rotation, centre, List.of(centre), true));
        DoorSocket socket = template.doorSockets(rotation).getFirst();
        Cell neighbour = step(centre, socket.side());
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Cell cell = new Cell(col, row);
                if (!cell.equals(centre)) rooms.add(new PlacedRoom(rooms.size(), filler, Rotation.DEG_0, cell, List.of(cell), cell.equals(neighbour)));
            }
        }
        int neighbourIndex = rooms.stream().filter(room -> room.occupies(neighbour)).findFirst().orElseThrow().index();
        List<LayoutDoor> doors = List.of(new LayoutDoor(centre, neighbour, 0, neighbourIndex, DoorType.PLAIN));
        return new FloorLayout(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 3L, 3, 3, rooms, doors, List.of(0, neighbourIndex));
    }

    private static Cell step(Cell cell, Side side) {
        return switch (side) {
            case NORTH -> new Cell(cell.col(), cell.row() - 1);
            case SOUTH -> new Cell(cell.col(), cell.row() + 1);
            case EAST -> new Cell(cell.col() + 1, cell.row());
            case WEST -> new Cell(cell.col() - 1, cell.row());
        };
    }

    @Test
    void theEntranceExitIsGatedThroughBothWallsInEveryTurnOfBothEntranceRooms() {
        for (String id : List.of("entrance_room", "start")) {
            RoomTemplate template = library.find(id).orElseThrow();
            for (Rotation rotation : Rotation.values()) {
                FloorLayout layout = around(template, rotation);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
                LayoutDoor exit = layout.doors().getFirst();
                assertEquals(List.of(exit), blocks.gates().entranceDoors(), id + " " + rotation);
                DoorFrame passage = DoorFrame.passage(exit);
                assertEquals(DoorFrame.WIDTH * DoorFrame.HEIGHT * 3, passage.blocks().size());
                for (int[] at : passage.blocks()) {
                    assertEquals(DoorGates.ENTRANCE, blocks.blockAt(at[0], at[1], at[2]), id + " " + rotation + " gate at " + at[0] + "," + at[1] + "," + at[2]);
                }
                assertEquals(CellGeometry.FLOOR_Y + 1, passage.minY());
                assertEquals(CellGeometry.FLOOR_Y + 4, passage.maxY());
            }
        }
    }

    @Test
    void keyDoorsFillTheirWholePassageAndPlainDoorsStayOpen() {
        FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 42L, library);
        CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
        int start = layout.startRoom().index();
        for (LayoutDoor door : layout.doors()) {
            Block expected = switch (door.type()) {
                case WITHER -> DoorGates.WITHER;
                case BLOOD -> DoorGates.BLOOD;
                case PLAIN -> door.connects(start) ? DoorGates.ENTRANCE : null;
            };
            for (int[] at : DoorFrame.passage(door).blocks()) {
                Block block = blocks.blockAt(at[0], at[1], at[2]);
                if (expected == null) assertTrue(block == null || !block.isSolid(), door + " blocked by " + block);
                else assertEquals(expected, block, door.toString());
            }
        }
    }

    @Test
    void mortStandsByTheExitFacingIntoTheRoomInEveryTurn() {
        for (String id : List.of("entrance_room", "start")) {
            RoomTemplate template = library.find(id).orElseThrow();
            for (Rotation rotation : Rotation.values()) {
                FloorLayout layout = around(template, rotation);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
                PlacedRoom room = layout.startRoom();
                RoomFeatures.Spot mort = features.mort(room).orElseThrow();
                String context = id + " " + rotation;
                assertEquals(room.origin(), CellGeometry.cellAt((int) Math.floor(mort.x()), (int) Math.floor(mort.z())).orElseThrow(), context);
                int x = (int) Math.floor(mort.x()), y = (int) Math.floor(mort.y()), z = (int) Math.floor(mort.z());
                Block feet = blocks.blockAt(x, y, z);
                Block below = blocks.blockAt(x, y - 1, z);
                assertTrue(feet == null || !feet.isSolid(), context + " feet in " + feet);
                assertNotNull(below, context);
                assertTrue(below.isSolid(), context + " stands on " + below);

                DoorFrame gate = DoorFrame.of(layout.doors().getFirst());
                double toGate = Math.hypot(gate.centreX() - mort.x(), gate.centreZ() - mort.z());
                assertTrue(toGate > 3 && toGate < 6, context + " is " + toGate + " from the gate");
                CellGeometry.BlockPosition centre = CellGeometry.roomCenter(room.origin());
                double yaw = Math.toRadians(mort.yaw());
                double lookX = -Math.sin(yaw), lookZ = Math.cos(yaw);
                double inX = centre.x() + 0.5 - mort.x(), inZ = centre.z() + 0.5 - mort.z();
                assertTrue(lookX * inX + lookZ * inZ > 0, context + " faces away from the room at yaw " + mort.yaw());
            }
        }
    }

    @Test
    void theBossPortalFillsTheObsidianFrameOppositeTheBloodDoorInEveryTurn() {
        RoomTemplate blood = library.templates(RoomKind.BLOOD).getFirst();
        for (Rotation rotation : Rotation.values()) {
            FloorLayout layout = around(blood, rotation);
            CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
            PlacedRoom room = layout.room(0);
            List<int[]> portal = features.portal(room);
            assertEquals(12, portal.size(), rotation.toString());
            boolean alongX = RoomFeatures.portalAlongX(room);
            for (int[] at : portal) {
                assertNull(blocks.blockAt(at[0], at[1], at[2]), rotation + " portal space is filled");
            }
            int minX = portal.stream().mapToInt(at -> at[0]).min().orElseThrow(), maxX = portal.stream().mapToInt(at -> at[0]).max().orElseThrow();
            int minZ = portal.stream().mapToInt(at -> at[2]).min().orElseThrow(), maxZ = portal.stream().mapToInt(at -> at[2]).max().orElseThrow();
            int minY = portal.stream().mapToInt(at -> at[1]).min().orElseThrow();
            assertEquals(alongX ? 2 : 0, maxX - minX, rotation.toString());
            assertEquals(alongX ? 0 : 2, maxZ - minZ, rotation.toString());
            Block before = alongX ? blocks.blockAt(minX - 1, minY, minZ) : blocks.blockAt(minX, minY, minZ - 1);
            Block after = alongX ? blocks.blockAt(maxX + 1, minY, minZ) : blocks.blockAt(minX, minY, maxZ + 1);
            assertEquals(Block.OBSIDIAN, before, rotation + " portal frame side");
            assertEquals(Block.OBSIDIAN, after, rotation + " portal frame side");

            CellGeometry.BlockPosition centre = CellGeometry.roomCenter(room.origin());
            double portalX = portal.stream().mapToDouble(at -> at[0] + 0.5).average().orElseThrow();
            double portalZ = portal.stream().mapToDouble(at -> at[2] + 0.5).average().orElseThrow();
            DoorFrame door = DoorFrame.of(layout.doors().getFirst());
            double towardsPortalX = portalX - (centre.x() + 0.5), towardsPortalZ = portalZ - (centre.z() + 0.5);
            double towardsDoorX = door.centreX() - (centre.x() + 0.5), towardsDoorZ = door.centreZ() - (centre.z() + 0.5);
            assertTrue(towardsPortalX * towardsDoorX + towardsPortalZ * towardsDoorZ < 0, rotation + " door and portal are on the same side");
        }
    }

    @Test
    void everyGeneratedBloodRoomIsEnteredFromTheSideOppositeItsPortal() {
        for (long seed = 0; seed < 40; seed++) {
            FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, seed, library);
            PlacedRoom blood = layout.bloodRoom();
            List<LayoutDoor> doors = layout.doorsOf(blood.index());
            assertEquals(1, doors.size(), "seed " + seed);
            List<int[]> portal = features.portal(blood);
            assertFalse(portal.isEmpty(), "seed " + seed);
            CellGeometry.BlockPosition centre = CellGeometry.roomCenter(blood.origin());
            double portalX = portal.stream().mapToDouble(at -> at[0] + 0.5).average().orElseThrow() - (centre.x() + 0.5);
            double portalZ = portal.stream().mapToDouble(at -> at[2] + 0.5).average().orElseThrow() - (centre.z() + 0.5);
            DoorFrame door = DoorFrame.of(doors.getFirst());
            double doorX = door.centreX() - (centre.x() + 0.5), doorZ = door.centreZ() - (centre.z() + 0.5);
            double cosine = (portalX * doorX + portalZ * doorZ) / (Math.hypot(portalX, portalZ) * Math.hypot(doorX, doorZ));
            assertTrue(cosine < -0.99, "seed " + seed + " door is not opposite the portal\n" + layout.render());
        }
    }

    @Test
    void theWatcherAndTheFairiesStayInsideTheirRoomsInEveryTurn() {
        RoomTemplate blood = library.templates(RoomKind.BLOOD).getFirst();
        RoomTemplate fairy = library.templates(RoomKind.FAIRY).getFirst();
        for (Rotation rotation : Rotation.values()) {
            PlacedRoom bloodRoom = around(blood, rotation).room(0);
            RoomFeatures.Spot watcher = features.watcher(bloodRoom).orElseThrow();
            CellGeometry.BlockPosition centre = CellGeometry.roomCenter(bloodRoom.origin());
            assertEquals(centre.x() + 0.5, watcher.x(), 1e-9);
            assertEquals(centre.z() + 0.5, watcher.z(), 1e-9);
            for (RoomFeatures.Spot stop : features.watcherStops(bloodRoom)) {
                assertEquals(bloodRoom.origin(), CellGeometry.cellAt((int) Math.floor(stop.x()), (int) Math.floor(stop.z())).orElseThrow());
            }
            PlacedRoom fairyRoom = around(fairy, rotation).room(0);
            RoomFeatures.Box zone = features.fairies(fairyRoom).orElseThrow();
            assertTrue(zone.maxX() - zone.minX() > 3 && zone.maxZ() - zone.minZ() > 3, rotation.toString());
            assertEquals(fairyRoom.origin(), CellGeometry.cellAt((int) zone.minX(), (int) zone.minZ()).orElseThrow());
            assertEquals(fairyRoom.origin(), CellGeometry.cellAt((int) zone.maxX(), (int) zone.maxZ()).orElseThrow());
        }
    }
}
