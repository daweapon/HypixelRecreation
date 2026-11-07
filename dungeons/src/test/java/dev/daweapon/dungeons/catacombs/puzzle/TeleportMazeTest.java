package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.MazePad;
import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.TeleportMaze;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportMazeTest {
    private static List<MazePad> pads() {
        return PuzzleCatalogueFixture.load().teleportMaze("teleport_maze").orElseThrow().pads();
    }

    @Test
    void theRoomHasSevenRoomsOfFourPadsPlusTheEntryAndCentre() {
        List<MazePad> pads = pads();
        assertEquals(30, pads.size());
        for (int room = 0; room < 7; room++) {
            int current = room;
            assertEquals(4, pads.stream().filter(pad -> pad.kind() == MazePad.Kind.ROOM && pad.room() == current).count());
        }
    }

    @Test
    void everyDealPairsAllPadsAcrossRoomsAndCanBeSolved() {
        Random random = new Random(11);
        Set<MazePad> finals = new HashSet<>();
        for (int deal = 0; deal < 200; deal++) {
            TeleportMaze maze = TeleportMaze.deal(pads(), random);
            for (MazePad pad : maze.pads()) {
                MazePad partner = maze.partner(pad);
                assertEquals(pad, maze.partner(partner));
                assertNotEquals(pad, partner);
                if (pad.kind() == MazePad.Kind.ROOM && partner.kind() == MazePad.Kind.ROOM) assertNotEquals(pad.room(), partner.room());
            }
            assertEquals(maze.centre(), maze.partner(maze.finalPad()));
            assertNotEquals(maze.finalPad().room(), maze.partner(maze.entry()).room());
            assertTrue(maze.solvable());
            finals.add(maze.finalPad());
        }
        assertTrue(finals.size() > 10, "the final pad should change from run to run");
    }

    @Test
    void theAimMatchesTheCapturedTurns() {
        assertEquals(31.0, TeleportMaze.yawToward(21.5, 3.5, 9.5, 23.5), 0.1);
        assertEquals(76.0, TeleportMaze.yawToward(25.5, 19.5, 9.5, 23.5), 0.1);
        assertEquals(0.0, TeleportMaze.yawToward(9.5, 15.5, 9.5, 23.5), 0.1);
        assertEquals(-90.0, TeleportMaze.yawToward(21.5, 19.5, 25.5, 19.5), 0.1);
        assertEquals(-33.7, TeleportMaze.yawToward(17.5, 7.5, 25.5, 19.5), 0.1);
    }

    @Test
    void padsAreFoundByTheirBlock() {
        TeleportMaze maze = TeleportMaze.deal(pads(), new Random(2));
        assertEquals(MazePad.Kind.ENTRY, maze.padAt(15, 18).orElseThrow().kind());
        assertEquals(MazePad.Kind.CENTRE, maze.padAt(15, 16).orElseThrow().kind());
        assertTrue(maze.padAt(15, 15).isEmpty());
    }
}
