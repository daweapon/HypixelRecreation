package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsPuzzleTest {
    @Test
    void iceFillOnlyAppearsOnFloorSeven() {
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            assertEquals(floor == CatacombsFloor.FLOOR_SEVEN, CatacombsPuzzle.templateAllowedOn("ice_fill", floor), floor.name());
        }
    }

    @Test
    void floorOneOffersTheFiveEarlyPuzzles() {
        for (String template : new String[]{"creeper_beams", "three_weirdos", "tic_tac_toe", "water_board", "teleport_maze"}) {
            assertTrue(CatacombsPuzzle.templateAllowedOn(template, CatacombsFloor.FLOOR_ONE), template);
        }
        for (String template : new String[]{"blaze", "boulder", "ice_fill", "placeholder_puzzle_ice_path", "quiz"}) {
            assertFalse(CatacombsPuzzle.templateAllowedOn(template, CatacombsFloor.FLOOR_ONE), template);
        }
    }

    @Test
    void teleportMazeStartsOnFloorOne() {
        assertFalse(CatacombsPuzzle.templateAllowedOn("teleport_maze", CatacombsFloor.ENTRANCE));
        assertTrue(CatacombsPuzzle.templateAllowedOn("teleport_maze", CatacombsFloor.FLOOR_ONE));
    }

    @Test
    void laterPuzzlesFollowTheWikiFloors() {
        assertEquals(CatacombsFloor.FLOOR_THREE, CatacombsPuzzle.HIGHER_OR_LOWER.minimumFloor());
        assertEquals(CatacombsFloor.FLOOR_THREE, CatacombsPuzzle.BOULDER.minimumFloor());
        assertEquals(CatacombsFloor.FLOOR_THREE, CatacombsPuzzle.ICE_PATH.minimumFloor());
        assertEquals(CatacombsFloor.FLOOR_FOUR, CatacombsPuzzle.QUIZ.minimumFloor());
    }

    @Test
    void templatesMapToTheirPuzzle() {
        assertEquals(CatacombsPuzzle.HIGHER_OR_LOWER, CatacombsPuzzle.ofTemplate("blaze").orElseThrow());
        assertEquals(CatacombsPuzzle.ICE_FILL, CatacombsPuzzle.ofTemplate("ice_fill").orElseThrow());
        assertTrue(CatacombsPuzzle.ofTemplate("unknown_room").isEmpty());
        assertTrue(CatacombsPuzzle.templateAllowedOn("unknown_room", CatacombsFloor.ENTRANCE));
    }
}
