package dev.daweapon.type.generic.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionArrowTest {

    @Test
    void picksOneOfEightArrowsByRelativeAngle() {
        assertEquals("⬆", DirectionArrow.arrow(0));
        assertEquals("⬈", DirectionArrow.arrow(45));
        assertEquals("➡", DirectionArrow.arrow(90));
        assertEquals("⬊", DirectionArrow.arrow(135));
        assertEquals("⬇", DirectionArrow.arrow(180));
        assertEquals("⬇", DirectionArrow.arrow(-180));
        assertEquals("⬋", DirectionArrow.arrow(-135));
        assertEquals("⬅", DirectionArrow.arrow(-90));
        assertEquals("⬉", DirectionArrow.arrow(-45));
    }

    @Test
    void sectorBoundariesSitHalfwayBetweenArrows() {
        assertEquals("⬆", DirectionArrow.arrow(22.4));
        assertEquals("⬈", DirectionArrow.arrow(22.6));
        assertEquals("⬆", DirectionArrow.arrow(-22.4));
        assertEquals("⬉", DirectionArrow.arrow(-22.6));
        assertEquals("⬆", DirectionArrow.arrow(359));
    }

    @Test
    void relativeYawFollowsMinecraftYaw() {
        assertEquals(0, DirectionArrow.relativeYaw(0, 0, 0, 0, 10), 1e-9);
        assertEquals(90, DirectionArrow.relativeYaw(0, 0, 0, -10, 0), 1e-9);
        assertEquals(-90, DirectionArrow.relativeYaw(0, 0, 0, 10, 0), 1e-9);
        assertEquals(180, Math.abs(DirectionArrow.relativeYaw(0, 0, 0, 0, -10)), 1e-9);
        assertEquals(0, DirectionArrow.relativeYaw(0, 0, 90, -10, 0), 1e-9);
        assertEquals(0, DirectionArrow.relativeYaw(0, 0, 450, -10, 0), 1e-9);
    }

    @Test
    void greenOnlyWhileFacingTheDestination() {
        assertTrue(DirectionArrow.isFacing(0));
        assertTrue(DirectionArrow.isFacing(22.5));
        assertTrue(DirectionArrow.isFacing(-20));
        assertTrue(DirectionArrow.isFacing(350));
        assertFalse(DirectionArrow.isFacing(23));
        assertFalse(DirectionArrow.isFacing(-90));
        assertFalse(DirectionArrow.isFacing(180));
        assertEquals("<a>", DirectionArrow.colour(5));
        assertEquals("<c>", DirectionArrow.colour(60));
    }

    @Test
    void markupCombinesColourAndArrow() {
        assertEquals("<a><l>⬆", DirectionArrow.markup(0, 0, 0, 0, 20));
        assertEquals("<c><l>➡", DirectionArrow.markup(0, 0, 0, -20, 0));
        assertEquals("<c><l>⬇", DirectionArrow.markup(0, 0, 0, 0, -20));
        assertEquals("<a><l>" + DirectionArrow.HERE, DirectionArrow.markup(0, 0, 0, 0.5, 0.5));
    }
}
