package dev.daweapon.type.catacombs.breaker;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrokenBlocksTest {
    private static final UUID ALICE = UUID.randomUUID();
    private static final UUID BOB = UUID.randomUUID();

    @Test
    void blocksComeBackTenSecondsAfterTheyBreak() {
        BrokenBlocks<String> broken = new BrokenBlocks<>();
        assertTrue(broken.record(ALICE, 1, 70, 1, "stone", 0).isEmpty());
        assertTrue(broken.record(ALICE, 2, 70, 1, "dirt", 4_000).isEmpty());
        assertTrue(broken.isBroken(1, 70, 1));
        assertTrue(broken.due(9_999).isEmpty());
        List<BrokenBlocks.Broken<String>> first = broken.due(10_000);
        assertEquals(1, first.size());
        assertEquals("stone", first.getFirst().original());
        assertFalse(broken.isBroken(1, 70, 1));
        assertEquals("dirt", broken.due(14_000).getFirst().original());
        assertEquals(0, broken.brokenBy(ALICE));
    }

    @Test
    void theTwentyFirstBreakBringsTheOldestBlockBack() {
        BrokenBlocks<Integer> broken = new BrokenBlocks<>();
        for (int i = 0; i < BrokenBlocks.MAX_PER_PLAYER; i++) {
            assertTrue(broken.record(ALICE, i, 70, 0, i, i).isEmpty());
        }
        List<BrokenBlocks.Broken<Integer>> evicted = broken.record(ALICE, 99, 70, 0, 99, 50);
        assertEquals(1, evicted.size());
        assertEquals(0, evicted.getFirst().original());
        assertEquals(BrokenBlocks.MAX_PER_PLAYER, broken.brokenBy(ALICE));
        assertTrue(broken.record(BOB, 500, 70, 0, 1, 50).isEmpty(), "the cap is per player");
    }

    @Test
    void staffHaveNoTwentyBlockCapButTheirBlocksStillComeBack() {
        BrokenBlocks<Integer> broken = new BrokenBlocks<>();
        int staffCap = BrokenBlocks.capFor(true);
        for (int i = 0; i < 60; i++) {
            assertTrue(broken.record(ALICE, i, 70, 0, i, 0, staffCap).isEmpty());
        }
        assertEquals(60, broken.brokenBy(ALICE));
        assertEquals(60, broken.due(BrokenBlocks.RESTORE_MILLIS).size());
        assertEquals(BrokenBlocks.MAX_PER_PLAYER, BrokenBlocks.capFor(false));
    }

    @Test
    void everythingStillBrokenComesBackWhenTheRunEnds() {
        BrokenBlocks<String> broken = new BrokenBlocks<>();
        broken.record(ALICE, 1, 70, 1, "a", 0);
        broken.record(BOB, 2, 70, 1, "b", 0);
        assertEquals(2, broken.drain().size());
        assertFalse(broken.isBroken(1, 70, 1));
        assertTrue(broken.due(Long.MAX_VALUE).isEmpty());
    }
}
