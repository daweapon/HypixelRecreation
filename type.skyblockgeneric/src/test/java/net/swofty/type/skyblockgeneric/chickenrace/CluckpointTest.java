package net.swofty.type.skyblockgeneric.chickenrace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CluckpointTest {

    @Test
    void timeLimitsMatchTheWiki() {
        assertEquals(33, Cluckpoint.ONE.getSecondsToBeat());
        assertEquals(52, Cluckpoint.TWO.getSecondsToBeat());
        assertEquals(68, Cluckpoint.THREE.getSecondsToBeat());
        assertEquals(104, Cluckpoint.FOUR.getSecondsToBeat());
    }

    @Test
    void firstArrivalUnderTheLimitClaims() {
        assertEquals(Cluckpoint.Outcome.CLAIM, Cluckpoint.reach(Cluckpoint.ONE, 0, 32_999, false));
        assertEquals(Cluckpoint.Outcome.CLAIM, Cluckpoint.reach(Cluckpoint.FOUR, 3, 103_999, true));
    }

    @Test
    void theLimitItselfIsTooSlow() {
        assertEquals(Cluckpoint.Outcome.TOO_SLOW, Cluckpoint.reach(Cluckpoint.ONE, 0, 33_000, false));
        assertEquals(Cluckpoint.Outcome.TOO_SLOW, Cluckpoint.reach(Cluckpoint.TWO, 1, 60_000, true));
    }

    @Test
    void claimedCluckpointsOnlyPass() {
        assertEquals(Cluckpoint.Outcome.PASS, Cluckpoint.reach(Cluckpoint.ONE, 2, 90_000, true));
        assertEquals(Cluckpoint.Outcome.PASS, Cluckpoint.reach(Cluckpoint.TWO, 2, 90_000, true));
    }

    @Test
    void frostyGatesEverythingPastCluckpointOne() {
        assertEquals(Cluckpoint.Outcome.NEEDS_FROSTY, Cluckpoint.reach(Cluckpoint.ONE, 1, 10_000, false));
        assertEquals(Cluckpoint.Outcome.PASS, Cluckpoint.reach(Cluckpoint.ONE, 1, 10_000, true));
    }

    @Test
    void nextTargetFollowsClaimedCount() {
        assertEquals(Cluckpoint.ONE, Cluckpoint.next(0));
        assertEquals(Cluckpoint.FOUR, Cluckpoint.next(3));
        assertNull(Cluckpoint.next(4));
    }
}
