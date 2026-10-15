package dev.daweapon.dungeons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DungeonUtilitiesTest {
    @Test
    void aStarFindsShortestPathIncludingEndpoints() {
        List<int[]> path = DungeonUtilities.aStar(new ArrayList<>(), 0, 0, 2, 0, 3, 3);
        assertEquals(3, path.size());
        assertArrayEquals(new int[]{0, 0}, path.getFirst());
        assertEquals(2, path.getLast()[0]);
        assertEquals(0, path.getLast()[1]);
    }

    @Test
    void aStarRoutesAroundVisitedCells() {
        List<int[]> visited = new ArrayList<>(List.of(new int[]{1, 0}));
        List<int[]> path = DungeonUtilities.aStar(visited, 0, 0, 2, 0, 3, 3);
        assertEquals(5, path.size());
        assertTrue(path.stream().noneMatch(c -> c[0] == 1 && c[1] == 0));
    }

    @Test
    void aStarReturnsEmptyWhenWalledOff() {
        List<int[]> wall = new ArrayList<>(List.of(new int[]{1, 0}, new int[]{1, 1}, new int[]{1, 2}));
        assertTrue(DungeonUtilities.aStar(wall, 0, 0, 2, 0, 3, 3).isEmpty());
    }

    @Test
    void aStarIgnoresStartBeingMarkedVisited() {
        List<int[]> visited = new ArrayList<>(List.of(new int[]{0, 0}));
        assertEquals(2, DungeonUtilities.aStar(visited, 0, 0, 1, 0, 2, 2).size());
    }

    @Test
    void centerPadsBothSides() {
        assertEquals("--ab--", DungeonUtilities.center("ab", 6, '-'));
        assertEquals("-ab--", DungeonUtilities.center("ab", 5, '-'));
        assertEquals("abcdef", DungeonUtilities.center("abcdef", 3, '-'));
    }
}
