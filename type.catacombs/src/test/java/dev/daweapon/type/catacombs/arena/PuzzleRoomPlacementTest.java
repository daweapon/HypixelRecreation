package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.RoomSpot;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillBoard;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceTile;
import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.MazePad;
import dev.daweapon.dungeons.catacombs.puzzle.waterboard.WaterBoard;
import dev.daweapon.type.catacombs.puzzle.RoomFrame;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every puzzle position in {@code puzzles.json} lands on the block it describes, whichever way the room is turned. */
class PuzzleRoomPlacementTest {
    private static final int MAZE_TOP = 10;
    private static PuzzleCatalogue catalogue;

    @BeforeAll
    static void load() throws IOException {
        RoomDoorwaysTest.load();
        catalogue = PuzzleCatalogue.load(RoomDoorwaysTest.ROOMS.getParent().resolve("puzzles.json"));
    }

    private static void inEveryRotation(String templateId, BiConsumer<RoomFrame, CatacombsArenaBlocks> check) {
        RoomTemplate template = RoomDoorwaysTest.library().find(templateId).orElseThrow();
        for (Rotation rotation : RoomDoorwaysTest.rotations(template)) {
            FloorLayout layout = RoomDoorwaysTest.surrounded(template, rotation, true);
            CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, RoomDoorwaysTest.structures(), false);
            PlacedRoom room = layout.room(0);
            assertTrue(blocks.isReal(room), templateId);
            check.accept(new RoomFrame(room), blocks);
        }
    }

    private static Block at(CatacombsArenaBlocks blocks, int[] at) {
        return blocks.blockAt(at[0], at[1], at[2]);
    }

    private static void assertBlock(CatacombsArenaBlocks blocks, int[] at, Block expected, String context) {
        Block found = at(blocks, at);
        assertNotNull(found, context + " is air");
        assertTrue(found.compare(expected), context + " is " + found.name());
    }

    @Test
    void iceFillTilesAreIceWithAirAboveAndTheGateIsIronBars() {
        PuzzleCatalogue.IceFillRoom data = catalogue.iceFill("ice_fill").orElseThrow();
        inEveryRotation("ice_fill", (frame, blocks) -> {
            for (IceFillBoard board : data.boards()) {
                for (IceTile tile : board.tiles()) {
                    assertBlock(blocks, frame.block(new RoomBlock(tile.x(), board.y(), tile.z())), Block.ICE, "tile " + tile);
                    assertNull(at(blocks, frame.block(new RoomBlock(tile.x(), board.y() + 1, tile.z()))), "above " + tile);
                }
            }
            data.gate().forEach(local -> assertBlock(blocks, frame.block(local), Block.IRON_BARS, "gate " + local));
            data.chests().forEach(local -> assertBlock(blocks, frame.block(local), Block.CHEST, "chest " + local));
        });
    }

    @Test
    void theThreeWeirdosChestsStandBesideTheirSpots() {
        PuzzleCatalogue.WeirdosRoom data = catalogue.weirdos("three_weirdos").orElseThrow();
        inEveryRotation("three_weirdos", (frame, blocks) -> {
            for (PuzzleCatalogue.Weirdo weirdo : data.weirdos()) {
                assertBlock(blocks, frame.block(weirdo.chest()), Block.CHEST, "chest " + weirdo.chest());
                var spot = frame.spot(weirdo.spot());
                assertNull(blocks.blockAt(spot.blockX(), spot.blockY(), spot.blockZ()), "weirdo stands in a block at " + spot);
                assertNotNull(blocks.blockAt(spot.blockX(), spot.blockY() - 1, spot.blockZ()), "weirdo floats at " + spot);
            }
        });
    }

    @Test
    void ticTacToeButtonsFaceTheRoomAndTheChestSitsBehindTheWall() {
        PuzzleCatalogue.TicTacToeRoom data = catalogue.ticTacToe("tic_tac_toe").orElseThrow();
        inEveryRotation("tic_tac_toe", (frame, blocks) -> {
            String facing = null;
            int buttons = 0;
            for (RoomBlock cell : data.cells()) {
                Block button = at(blocks, frame.block(cell));
                if (button == null) continue;
                assertTrue(button.compare(Block.STONE_BUTTON), cell + " " + button.name());
                if (facing == null) facing = button.getProperty("facing");
                assertEquals(facing, button.getProperty("facing"));
                buttons++;
            }
            assertTrue(buttons >= 8, buttons + " buttons");
            data.wall().forEach(local -> assertNotNull(at(blocks, frame.block(local)), "wall " + local));
            assertBlock(blocks, frame.block(data.chest()), Block.CHEST, "chest");
            int[] wallBlock = frame.block(data.wall().get(7));
            int[] buttonBlock = frame.block(data.cells().get(4));
            int dx = buttonBlock[0] - wallBlock[0], dz = buttonBlock[2] - wallBlock[2];
            String expected = dx == 1 ? "east" : dx == -1 ? "west" : dz == 1 ? "south" : "north";
            assertEquals(expected, facing.toLowerCase(Locale.ROOT), "buttons face away from the wall");
        });
    }

    @Test
    void teleportPadsAreEndPortalFramesWithLandingSpotsBesideThem() {
        PuzzleCatalogue.TeleportMazeRoom data = catalogue.teleportMaze("teleport_maze").orElseThrow();
        inEveryRotation("teleport_maze", (frame, blocks) -> {
            for (MazePad pad : data.pads()) {
                assertBlock(blocks, frame.block(new RoomBlock(pad.x(), data.y(), pad.z())), Block.END_PORTAL_FRAME, "pad " + pad);
                var landing = frame.spot(new RoomSpot(pad.landX(), data.y() + 0.5, pad.landZ(), 0));
                assertNull(blocks.blockAt(landing.blockX(), landing.blockY() + 1, landing.blockZ()), "landing blocked for " + pad);
                Block floor = blocks.blockAt(landing.blockX(), landing.blockY(), landing.blockZ());
                assertTrue(floor == null || !floor.compare(Block.END_PORTAL_FRAME), "landing on a pad for " + pad);
            }
            assertBlock(blocks, frame.block(data.chest()), Block.CHEST, "chest");
        });
    }

    @Test
    void creeperBeamLanternsAreSeaLanternsAndThePedestalHidesTheChest() {
        PuzzleCatalogue.CreeperBeamsRoom data = catalogue.creeperBeams("creeper_beams").orElseThrow();
        inEveryRotation("creeper_beams", (frame, blocks) -> {
            data.lanterns().forEach(local -> assertBlock(blocks, frame.block(local), Block.SEA_LANTERN, "lantern " + local));
            assertBlock(blocks, frame.block(data.chest()), Block.CHEST, "chest");
            long solid = data.pedestal().stream().filter(local -> at(blocks, frame.block(local)) != null).count();
            assertTrue(solid >= data.pedestal().size() / 2, solid + " pedestal blocks");
        });
    }

    @Test
    void waterBoardGatesLeversDoorsAndMazeSitOnTheirBlocks() {
        PuzzleCatalogue.WaterBoardRoom data = catalogue.waterBoard("water_board").orElseThrow();
        WaterBoard.Layout layout = data.layout();
        inEveryRotation("water_board", (frame, blocks) -> {
            for (WaterBoard.Gate gate : layout.gates()) {
                int[] at = frame.block(new RoomBlock(data.planeX() - (gate.extended() ? 0 : 1), gate.y(), gate.z()));
                assertBlock(blocks, at, Block.fromKey(gate.type()), "gate " + gate);
                assertBlock(blocks, frame.block(new RoomBlock(data.planeX() - 2, gate.y(), gate.z())), Block.STICKY_PISTON, "piston " + gate);
            }
            data.levers().forEach(lever -> assertBlock(blocks, frame.block(lever.at()), Block.LEVER, "lever " + lever));
            for (int row = 0; row < layout.height(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    if (layout.rows().get(row).charAt(column) != '#' || layout.maxY() - row > MAZE_TOP) continue;
                    int[] at = frame.block(new RoomBlock(data.planeX(), layout.maxY() - row, layout.minZ() + column));
                    assertNotNull(at(blocks, at), "maze wall at row " + row + " column " + column);
                }
            }
            for (PuzzleCatalogue.WaterDoor door : data.doors()) {
                Block wool = Block.fromKey("minecraft:" + door.colour() + "_wool");
                PuzzleCatalogue.DoorPart side = data.doorHeads().getFirst();
                assertBlock(blocks, frame.block(new RoomBlock(door.x(), side.y(), side.z())), wool, door.colour() + " door");
                for (PuzzleCatalogue.DoorPart fill : data.doorFill()) {
                    assertNull(at(blocks, frame.block(new RoomBlock(door.x(), fill.y(), fill.z()))), door.colour() + " doorway");
                }
            }
            assertBlock(blocks, frame.block(data.chest()), Block.CHEST, "chest");
        });
    }

    @Test
    void blazesHangInAirBesideTheChestColumn() {
        PuzzleCatalogue.HigherOrLowerRoom data = catalogue.higherOrLower("blaze").orElseThrow();
        inEveryRotation("blaze", (frame, blocks) -> {
            for (RoomSpot spot : data.blazes()) {
                var at = frame.spot(spot);
                assertNull(blocks.blockAt(at.blockX(), at.blockY(), at.blockZ()), "blaze inside a block at " + spot);
            }
            for (int y = data.bottom(); y <= data.top(); y++) {
                assertNotNull(at(blocks, frame.block(new RoomBlock(data.columnX(), y, data.columnZ()))), "column at " + y);
            }
        });
    }

    @Test
    void boulderBoxesFillTheGridOverItsFloorAndTheChestSitsBehind() {
        PuzzleCatalogue.BoulderRoom data = catalogue.boulder("boulder").orElseThrow();
        inEveryRotation("boulder", (frame, blocks) -> {
            for (int column = 0; column < data.columns(); column++) {
                for (int row = 0; row < data.rows(); row++) {
                    RoomBlock floor = new RoomBlock(data.gridX() + column * data.cellSize() + 1, data.gridY() - 1,
                            data.gridZ() + row * data.cellSize() + 1);
                    assertNotNull(at(blocks, frame.block(floor)), "floor under cell " + column + "," + row);
                }
            }
            assertBlock(blocks, frame.block(data.chest()), Block.CHEST, "chest");
        });
    }

    @Test
    void quizPedestalsCarryTheirAnswerButtons() {
        PuzzleCatalogue.QuizRoom data = catalogue.quiz("quiz").orElseThrow();
        inEveryRotation("quiz", (frame, blocks) -> {
            for (PuzzleCatalogue.QuizPedestal pedestal : data.pedestals()) {
                RoomBlock block = pedestal.block();
                assertNotNull(at(blocks, frame.block(block)), "pedestal " + pedestal.option());
                for (int[] side : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    assertBlock(blocks, frame.block(new RoomBlock(block.x() + side[0], block.y(), block.z() + side[1])), Block.STONE_BUTTON,
                            "pedestal " + pedestal.option() + " button");
                }
            }
        });
    }

    @Test
    void everyCataloguedPuzzleRoomIsARealTemplate() {
        for (String id : List.of("ice_fill", "three_weirdos", "tic_tac_toe", "teleport_maze", "creeper_beams", "water_board", "blaze",
                "boulder", "quiz")) {
            assertTrue(catalogue.has(id), id);
            assertTrue(RoomDoorwaysTest.library().find(id).orElseThrow().blocks().isPresent(), id);
        }
    }
}
