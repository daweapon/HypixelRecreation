package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.creeperbeams.CreeperBeams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreeperBeamsTest {
    private static final RoomBlock[][] CAPTURED = {
            {new RoomBlock(15, 6, 15), new RoomBlock(15, 16, 13)},
            {new RoomBlock(27, 10, 14), new RoomBlock(2, 7, 16)},
            {new RoomBlock(7, 13, 20), new RoomBlock(26, 2, 7)},
            {new RoomBlock(25, 8, 23), new RoomBlock(4, 4, 8)}};

    private static CreeperBeams puzzle() {
        PuzzleCatalogue.CreeperBeamsRoom room = PuzzleCatalogueFixture.load().creeperBeams("creeper_beams").orElseThrow();
        return new CreeperBeams(room.lanterns(), room.space(), room.required());
    }

    @Test
    void theFourCapturedBeamsSolveTheRoom() {
        CreeperBeams beams = puzzle();
        for (RoomBlock[] pair : CAPTURED) {
            assertInstanceOf(CreeperBeams.Shot.Selected.class, beams.shoot(pair[0]));
            CreeperBeams.Shot joined = beams.shoot(pair[1]);
            assertInstanceOf(CreeperBeams.Shot.Joined.class, joined);
            assertTrue(beams.crossesCreeper(((CreeperBeams.Shot.Joined) joined).beam()));
        }
        assertEquals(4, beams.beamsThroughCreeper());
        assertTrue(beams.solved());
    }

    @Test
    void theBeamHypixelShowedOnArrivalMissesTheCreeper() {
        CreeperBeams beams = puzzle();
        beams.shoot(new RoomBlock(5, 5, 24));
        CreeperBeams.Shot shot = beams.shoot(new RoomBlock(8, 12, 8));
        assertFalse(beams.crossesCreeper(((CreeperBeams.Shot.Joined) shot).beam()));
        assertEquals(0, beams.beamsThroughCreeper());
    }

    @Test
    void shootingAJoinedLanternBreaksItsBeam() {
        CreeperBeams beams = puzzle();
        beams.shoot(CAPTURED[0][0]);
        beams.shoot(CAPTURED[0][1]);
        assertInstanceOf(CreeperBeams.Shot.Broken.class, beams.shoot(CAPTURED[0][1]));
        assertEquals(0, beams.beams().size());
    }

    @Test
    void shootingTheSelectedLanternAgainLetsGo() {
        CreeperBeams beams = puzzle();
        beams.shoot(CAPTURED[1][0]);
        assertInstanceOf(CreeperBeams.Shot.Deselected.class, beams.shoot(CAPTURED[1][0]));
        assertTrue(beams.selected().isEmpty());
    }

    @Test
    void blocksThatAreNotLanternsAreIgnored() {
        assertInstanceOf(CreeperBeams.Shot.Missed.class, puzzle().shoot(new RoomBlock(0, 0, 0)));
    }

    @Test
    void theRoomHasItsCapturedLanternsPedestalAndChest() {
        PuzzleCatalogue.CreeperBeamsRoom room = PuzzleCatalogueFixture.load().creeperBeams("creeper_beams").orElseThrow();
        assertEquals(22, room.lanterns().size());
        assertEquals(new RoomBlock(15, 1, 15), room.chest());
        assertFalse(room.pedestal().contains(room.chest()));
    }
}
