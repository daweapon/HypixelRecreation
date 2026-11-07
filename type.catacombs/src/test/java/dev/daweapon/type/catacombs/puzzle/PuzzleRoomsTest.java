package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuzzleRoomsTest {
    private static final Path DUNGEONS = Path.of("..", "configuration", "skyblock", "dungeons");
    private static RoomTemplateLibrary library;
    private static PuzzleCatalogue catalogue;

    @BeforeAll
    static void load() throws IOException {
        library = RoomTemplateLibrary.fromDirectory(DUNGEONS.resolve("rooms"));
        catalogue = PuzzleCatalogue.load(DUNGEONS.resolve("puzzles.json"));
    }

    private static PuzzleRoom create(String templateId) {
        PlacedRoom room = new PlacedRoom(0, library.find(templateId).orElseThrow(), Rotation.DEG_0, new Cell(0, 0), List.of(new Cell(0, 0)), false);
        PuzzleContext context = new PuzzleContext(null, CatacombsFloor.FLOOR_SEVEN, null, _ -> {
        }, (_, _) -> {
        });
        return PuzzleRooms.create(context, room, catalogue).orElseThrow();
    }

    @Test
    void everyCapturedPuzzleRoomGetsItsPuzzle() {
        Map<String, CatacombsPuzzle> expected = Map.of(
                "ice_fill", CatacombsPuzzle.ICE_FILL,
                "three_weirdos", CatacombsPuzzle.THREE_WEIRDOS,
                "tic_tac_toe", CatacombsPuzzle.TIC_TAC_TOE,
                "teleport_maze", CatacombsPuzzle.TELEPORT_MAZE,
                "creeper_beams", CatacombsPuzzle.CREEPER_BEAMS,
                "water_board", CatacombsPuzzle.WATER_BOARD,
                "blaze", CatacombsPuzzle.HIGHER_OR_LOWER,
                "boulder", CatacombsPuzzle.BOULDER,
                "quiz", CatacombsPuzzle.QUIZ);
        expected.forEach((template, puzzle) -> {
            PuzzleRoom room = create(template);
            assertEquals(puzzle, room.puzzle(), template);
            assertEquals(PuzzleState.READY, room.state(), template);
            assertEquals(CatacombsPuzzle.ofTemplate(template).orElseThrow(), room.puzzle(), template);
        });
    }

    @Test
    void roomsWithoutPuzzleDataStayPlainRooms() {
        PlacedRoom room = new PlacedRoom(0, library.find("placeholder_puzzle_ice_path").orElseThrow(), Rotation.DEG_0, new Cell(0, 0),
                List.of(new Cell(0, 0)), false);
        assertTrue(PuzzleRooms.create(null, room, catalogue).isEmpty());
    }
}
