package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard.Cell;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard.Face;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard.Push;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoulderTest {
    private static PuzzleCatalogue.BoulderRoom room() {
        return PuzzleCatalogueFixture.load().boulder("boulder").orElseThrow();
    }

    private static BoulderBoard board(int variant) {
        PuzzleCatalogue.BoulderRoom room = room();
        return new BoulderBoard(room.columns(), room.rows(), room.variants().get(variant));
    }

    @Test
    void threeCapturedLayoutsOnASevenBySixGrid() {
        PuzzleCatalogue.BoulderRoom room = room();
        assertEquals(3, room.variants().size());
        assertEquals(7, room.columns());
        assertEquals(6, room.rows());
        assertEquals(List.of(22, 19, 14), room.variants().stream().map(Map::size).toList());
    }

    @Test
    void everyCapturedLayoutStartsShutAndCanBeOpened() {
        for (int variant = 0; variant < room().variants().size(); variant++) {
            BoulderBoard board = board(variant);
            assertFalse(board.pathOpen(), "layout " + variant + " starts open");
            List<BoulderBoard.Press> solution = board.shortestSolution(200_000).orElseThrow();
            for (BoulderBoard.Press press : solution) assertTrue(board.press(press.cell(), press.face()) != Push.BLOCKED);
            assertTrue(board.pathOpen(), "layout " + variant);
        }
    }

    @Test
    void theCapturedFirstPushMovesTheBoxAwayFromTheButton() {
        BoulderBoard board = board(0);
        assertEquals(Push.MOVED, board.press(new Cell(5, 2), Face.NORTH));
        assertTrue(board.at(new Cell(5, 2)).isEmpty());
        assertTrue(board.at(new Cell(5, 3)).isPresent());
        assertEquals(Set.of(Face.NORTH, Face.SOUTH), board.at(new Cell(5, 3)).orElseThrow().buttons());
    }

    @Test
    void boxesStopAgainstBoxesAndFallOffTheEdge() {
        BoulderBoard board = new BoulderBoard(3, 3, Map.of(
                new Cell(1, 1), new BoulderBoard.Box("minecraft:birch_planks", Set.of(Face.NORTH, Face.WEST)),
                new Cell(1, 2), new BoulderBoard.Box("minecraft:jungle_planks", Set.of(Face.NORTH))));
        assertEquals(Push.BLOCKED, board.press(new Cell(1, 1), Face.NORTH));
        assertEquals(Push.NO_BUTTON, board.press(new Cell(1, 1), Face.SOUTH));
        assertEquals(Push.REMOVED, board.press(new Cell(1, 2), Face.NORTH));
        assertEquals(Push.MOVED, board.press(new Cell(1, 1), Face.NORTH));
        assertEquals(Push.MOVED, board.press(new Cell(1, 2), Face.WEST));
        assertTrue(board.at(new Cell(2, 2)).isPresent());
    }

    @Test
    void onlyButtonsAPlayerCanReachArePressable() {
        BoulderBoard board = board(0);
        Set<Cell> reachable = board.reachable();
        assertTrue(board.pressable(new Cell(3, 1), Face.NORTH, reachable));
        assertFalse(board.pressable(new Cell(0, 5), Face.SOUTH, reachable));
    }
}
