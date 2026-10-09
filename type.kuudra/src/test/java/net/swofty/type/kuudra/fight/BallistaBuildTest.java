package net.swofty.type.kuudra.fight;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BallistaBuildTest {
    private static final List<String> PILES = List.of("A", "B", "C", "D", "E", "F");
    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void oneBuilderFillsAPileInThePileTime() {
        BallistaBuild build = new BallistaBuild(PILES, 5);
        for (long tick = 1; tick <= 100; tick++) {
            build.click(alice, "A", tick, 10);
            build.tick(tick, _ -> 1);
        }
        assertEquals(100, build.progress("A"), 1e-6);
        assertEquals(100 / 6.0, build.overall(), 1e-6);
        assertEquals(16, build.overallPercent());
        assertFalse(build.complete());
        assertFalse(build.click(alice, "A", 101, 10));
    }

    @Test
    void speedMultipliersAndHelpersAddUp() {
        BallistaBuild build = new BallistaBuild(PILES, 5);
        build.click(alice, "A", 0, 10);
        build.click(bob, "A", 0, 10);
        assertEquals(2, build.helping());
        List<String> finished = List.of();
        long tick = 0;
        while (finished.isEmpty() && tick < 100) {
            tick++;
            build.click(alice, "A", tick, 10);
            build.click(bob, "A", tick, 10);
            finished = build.tick(tick, player -> player.equals(alice) ? 2 : 1);
        }
        assertEquals(List.of("A"), finished);
        assertEquals(34, tick);
        assertEquals(0, build.helping());
    }

    @Test
    void aClickOnlyBuildsForTheHoldTime() {
        BallistaBuild build = new BallistaBuild(PILES, 5);
        build.click(alice, "B", 0, 10);
        for (long tick = 1; tick <= 40; tick++) build.tick(tick, _ -> 1);
        assertEquals(10, build.progress("B"), 1e-6);
        assertFalse(build.isBuilding(alice));
    }

    @Test
    void hitsAndIdlenessKnockUnfinishedPilesBack() {
        BallistaBuild build = new BallistaBuild(PILES, 5);
        assertNull(build.knockAny(5, new Random(1)));
        build.click(alice, "C", 0, 10);
        for (long tick = 1; tick <= 10; tick++) build.tick(tick, _ -> 1);
        build.tick(20, _ -> 1);
        assertEquals("C", build.knockAny(4, new Random(1)));
        assertEquals(6, build.progress("C"), 1e-6);
        build.decayIdle(3);
        assertEquals(3, build.progress("C"), 1e-6);
        build.decayIdle(3);
        build.decayIdle(3);
        assertEquals(0, build.progress("C"), 1e-6);
    }

    @Test
    void finishedPilesStayFinished() {
        BallistaBuild build = new BallistaBuild(PILES, 5);
        build.completeAll();
        build.knock("A", 50);
        build.decayIdle(50);
        assertTrue(build.complete());
        assertEquals(100, build.overallPercent());
    }
}
