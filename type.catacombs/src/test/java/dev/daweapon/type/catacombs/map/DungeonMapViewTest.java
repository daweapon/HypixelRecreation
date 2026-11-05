package dev.daweapon.type.catacombs.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DungeonMapViewTest {
    @Test
    void iconCoordinatesSpanTheWholeMap() {
        assertEquals(-128, DungeonMapView.coordinate(0));
        assertEquals(0, DungeonMapView.coordinate(64));
        assertEquals(127, DungeonMapView.coordinate(200));
    }

    @Test
    void iconDirectionFollowsYawInSixteenths() {
        assertEquals(0, DungeonMapView.direction(0));
        assertEquals(4, DungeonMapView.direction(90));
        assertEquals(8, DungeonMapView.direction(180));
        assertEquals(12, DungeonMapView.direction(-90));
    }
}
