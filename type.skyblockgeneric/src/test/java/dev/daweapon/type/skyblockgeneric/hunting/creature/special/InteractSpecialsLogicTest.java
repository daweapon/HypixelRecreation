package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractSpecialsLogicTest {
    @Test
    void hopCounterExhaustsOnFourthHop() {
        HopCounter counter = new HopCounter(4);
        for (int i = 0; i < 3; i++) assertFalse(counter.hop());
        assertTrue(counter.hop());
        assertTrue(counter.exhausted());
    }

    @Test
    void bambooTargetStaysInRangeAndCompletesOnTarget() {
        Random random = new Random(7);
        for (int i = 0; i < 200; i++) {
            BambooFeeding feeding = BambooFeeding.roll(40, 44, random);
            assertTrue(feeding.target() >= 40 && feeding.target() <= 44);
        }
        BambooFeeding feeding = new BambooFeeding(40);
        for (int i = 0; i < 39; i++) assertFalse(feeding.feed());
        assertTrue(feeding.feed());
        assertEquals(1.0, feeding.fraction());
    }

    @Test
    void bulletCapturedOnThirdReturn() {
        BulletExchange exchange = new BulletExchange(3);
        assertFalse(exchange.returned());
        assertFalse(exchange.returned());
        assertTrue(exchange.returned());
    }

    @Test
    void clickRotatesClickedHeadAndOneAbove() {
        TotemPuzzle puzzle = new TotemPuzzle(0, 1, 2);
        assertArrayEquals(new boolean[]{true, true, false}, puzzle.click(0, true));
        assertEquals(1, puzzle.facing(0));
        assertEquals(2, puzzle.facing(1));
        assertEquals(2, puzzle.facing(2));
    }

    @Test
    void topClickRotatesTopAndBottom() {
        TotemPuzzle puzzle = new TotemPuzzle(0, 1, 2);
        assertArrayEquals(new boolean[]{true, false, true}, puzzle.click(2, true));
        assertEquals(1, puzzle.facing(0));
        assertEquals(3, puzzle.facing(2));
    }

    @Test
    void rightClickRotatesTheOtherWay() {
        TotemPuzzle puzzle = new TotemPuzzle(0, 1, 2);
        puzzle.click(1, false);
        assertEquals(0, puzzle.facing(1));
        assertEquals(1, puzzle.facing(2));
        puzzle.click(0, false);
        assertEquals(3, puzzle.facing(0));
    }

    @Test
    void alignedTopHeadsStopRotatingAndBottomAlignsToAwaken() {
        TotemPuzzle puzzle = new TotemPuzzle(0, 2, 2);
        assertTrue(puzzle.topLocked());
        assertArrayEquals(new boolean[]{false, false, false}, puzzle.click(1, true));
        assertArrayEquals(new boolean[]{true, false, false}, puzzle.click(0, true));
        assertFalse(puzzle.awake());
        puzzle.click(2, true);
        assertTrue(puzzle.awake());
        assertEquals(2, puzzle.facing(0));
    }

    @Test
    void randomPuzzleNeverStartsAwakeOrLocked() {
        Random random = new Random(3);
        for (int i = 0; i < 200; i++) {
            TotemPuzzle puzzle = TotemPuzzle.random(random);
            assertFalse(puzzle.topLocked());
            assertFalse(puzzle.awake());
        }
    }

    @Test
    void headIsPickedByHeight() {
        assertEquals(0, TotemPuzzle.headAt(0.2, 1.0));
        assertEquals(1, TotemPuzzle.headAt(1.5, 1.0));
        assertEquals(2, TotemPuzzle.headAt(2.9, 1.0));
        assertEquals(2, TotemPuzzle.headAt(5, 1.0));
        assertEquals(0, TotemPuzzle.headAt(-1, 1.0));
    }
}
