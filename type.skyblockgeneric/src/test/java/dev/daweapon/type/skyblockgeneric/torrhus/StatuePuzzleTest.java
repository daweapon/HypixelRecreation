package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.type.skyblockgeneric.torrhus.StatuePuzzle.Animal;
import dev.daweapon.type.skyblockgeneric.torrhus.StatuePuzzle.Facing;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatuePuzzleTest {
    private static StatuePuzzle puzzle(Facing bird, Facing snake, Facing frog, Facing fly) {
        Map<Animal, Facing> map = new EnumMap<>(Animal.class);
        map.put(Animal.BIRD, bird);
        map.put(Animal.SNAKE, snake);
        map.put(Animal.FROG, frog);
        map.put(Animal.FLY, fly);
        return new StatuePuzzle(map);
    }

    @Test
    void theChainBirdSnakeFrogFlyThenWallSolves() {
        assertTrue(puzzle(Facing.EAST, Facing.SOUTH, Facing.WEST, Facing.WEST).solved());
        assertTrue(puzzle(Facing.EAST, Facing.SOUTH, Facing.WEST, Facing.SOUTH).solved());
    }

    @Test
    void flyFacingAnotherStatueDoesNotSolve() {
        assertFalse(puzzle(Facing.EAST, Facing.SOUTH, Facing.WEST, Facing.NORTH).solved());
        assertFalse(puzzle(Facing.EAST, Facing.SOUTH, Facing.WEST, Facing.EAST).solved());
        assertFalse(puzzle(Facing.EAST, Facing.SOUTH, Facing.NORTH, Facing.WEST).solved());
    }

    @Test
    void targetIsTheStatueOnTheFacedLine() {
        StatuePuzzle puzzle = puzzle(Facing.EAST, Facing.NORTH, Facing.WEST, Facing.SOUTH);
        assertEquals(Optional.of(Animal.SNAKE), puzzle.target(Animal.BIRD));
        assertEquals(Optional.empty(), puzzle.target(Animal.SNAKE));
        assertEquals(Optional.of(Animal.FLY), puzzle.target(Animal.FROG));
        assertEquals(Optional.empty(), puzzle.target(Animal.FLY));
    }

    @Test
    void rotatingFourTimesIsTheIdentityAndTurnsClockwise() {
        StatuePuzzle puzzle = puzzle(Facing.NORTH, Facing.NORTH, Facing.NORTH, Facing.NORTH);
        puzzle.rotate(Animal.BIRD);
        assertEquals(Facing.EAST, puzzle.facing(Animal.BIRD));
        for (int i = 0; i < 3; i++) puzzle.rotate(Animal.BIRD);
        assertEquals(Facing.NORTH, puzzle.facing(Animal.BIRD));
    }

    @Test
    void scrambledPuzzlesStartUnsolvedAndCanBeSolvedByRotating() {
        Random random = new Random(11);
        for (int i = 0; i < 50; i++) {
            StatuePuzzle puzzle = StatuePuzzle.scrambled(random);
            assertFalse(puzzle.solved());
            solveByRotation(puzzle);
            assertTrue(puzzle.solved());
        }
    }

    private static void solveByRotation(StatuePuzzle puzzle) {
        for (int i = 0; i < 256 && !puzzle.solved(); i++) {
            puzzle.rotate(Animal.FLY);
            if (i % 4 == 3) puzzle.rotate(Animal.FROG);
            if (i % 16 == 15) puzzle.rotate(Animal.SNAKE);
            if (i % 64 == 63) puzzle.rotate(Animal.BIRD);
        }
    }
}
