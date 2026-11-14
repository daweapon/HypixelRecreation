package dev.daweapon.type.murdermysterygame.maphandler;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InteractiveElementTest {
    @Test
    void builderDefaultsToButtonWithNoCost() {
        InteractiveElement element = InteractiveElement.builder("door").position(1, 2, 3).build();
        assertEquals("door", element.id());
        assertEquals(InteractionType.BUTTON, element.type());
        assertEquals(0, element.goldCost());
        assertEquals(1, element.positions().size());
    }

    @Test
    void matchesPositionUsesBlockCoordinates() {
        InteractiveElement element = InteractiveElement.builder("lever").position(10, 64, -5).build();
        assertTrue(element.matchesPosition(new Pos(10.7, 64.2, -4.5)));
        assertFalse(element.matchesPosition(new Pos(11, 64, -5)));
    }

    @Test
    void matchesAnyOfMultiplePositions() {
        InteractiveElement element = InteractiveElement.builder("gate")
                .position(0, 0, 0).position(5, 5, 5).goldCost(2).build();
        assertTrue(element.matchesPosition(new Pos(5, 5, 5)));
        assertFalse(element.matchesPosition(new Pos(1, 1, 1)));
        assertEquals(2, element.goldCost());
    }
}
