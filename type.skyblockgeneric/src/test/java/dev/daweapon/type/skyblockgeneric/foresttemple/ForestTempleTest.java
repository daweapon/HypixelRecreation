package dev.daweapon.type.skyblockgeneric.foresttemple;

import dev.daweapon.type.skyblockgeneric.torrhus.StarbornTemple;
import dev.daweapon.type.skyblockgeneric.torrhus.TempleDoorState;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestTempleTest {
    @Test
    void lanternsAreClickedFromNoPicklesUpToFour() {
        LanternPuzzle puzzle = new LanternPuzzle();
        for (int i = 0; i < LanternPuzzle.SEQUENCE.size() - 1; i++) {
            assertEquals(LanternPuzzle.Result.PROGRESS, puzzle.press(i));
        }
        assertEquals(LanternPuzzle.Result.SOLVED, puzzle.press(4));
        assertEquals(0, puzzle.progress());
    }

    @Test
    void picklesAscendAlongTheSequence() {
        for (int i = 0; i < LanternPuzzle.SEQUENCE.size(); i++) {
            assertEquals(i, LanternPuzzle.SEQUENCE.get(i).pickles());
        }
    }

    @Test
    void wrongLanternResetsAndTheFirstLanternRestarts() {
        LanternPuzzle puzzle = new LanternPuzzle();
        puzzle.press(0);
        puzzle.press(1);
        assertEquals(LanternPuzzle.Result.WRONG, puzzle.press(3));
        assertEquals(0, puzzle.progress());
        puzzle.press(0);
        puzzle.press(1);
        assertEquals(LanternPuzzle.Result.WRONG, puzzle.press(0));
        assertEquals(1, puzzle.progress());
    }

    @Test
    void lanternsAreFoundByPosition() {
        assertEquals(2, LanternPuzzle.indexOf(-618, 66, 54));
        assertEquals(-1, LanternPuzzle.indexOf(0, 0, 0));
    }

    @Test
    void scrambledFloorIsNeverSolved() {
        Random random = new Random(7);
        for (int i = 0; i < 500; i++) assertFalse(FloorPuzzle.scrambled(random).solved());
    }

    @Test
    void rotatingEveryCellIntoTheLayoutSolvesTheFloor() {
        FloorPuzzle puzzle = FloorPuzzle.scrambled(new Random(3));
        FloorPuzzle.Facing[] target = FloorPuzzle.solvedLayout();
        for (int cell = 0; cell < FloorPuzzle.CELLS; cell++) {
            while (puzzle.facing(cell) != target[cell]) puzzle.rotate(cell);
        }
        assertTrue(puzzle.solved());
    }

    @Test
    void floorCellsFollowTheCapturedPosition() {
        assertEquals(0, FloorPuzzle.cellAt(-640, 75));
        assertEquals(15, FloorPuzzle.cellAt(-633, 76));
        assertEquals(-1, FloorPuzzle.cellAt(-632, 75));
        assertEquals(-1, FloorPuzzle.cellAt(-640, 77));
        assertEquals(-640, FloorPuzzle.x(8));
        assertEquals(76, FloorPuzzle.z(8));
    }

    @Test
    void forestWillMatchesTheReferenceReadings() {
        assertEquals(7.81, StarbornTemple.FOREST.will(233), 0.3);
        assertEquals(10.85, StarbornTemple.FOREST.will(1_850), 0.4);
        assertEquals("Foraging Fortune", StarbornTemple.FOREST.statName());
        assertEquals(StarbornTemple.Benefit.AXES, StarbornTemple.FOREST.benefit());
    }

    @Test
    void doorSlamsBehindThePlayerAndReopensOnTheWayBack() {
        TempleDoorState door = new TempleDoorState(72.5, 76, 70.5);
        assertEquals(TempleDoorState.Change.OPENED, door.update(true, false, 60));
        assertEquals(TempleDoorState.Change.SLAMMED, door.update(true, false, 73));
        assertEquals(TempleDoorState.Change.NONE, door.update(true, false, 71));
        door.update(true, false, 77);
        assertEquals(TempleDoorState.Change.OPENED, door.update(true, false, 70));
    }
}
