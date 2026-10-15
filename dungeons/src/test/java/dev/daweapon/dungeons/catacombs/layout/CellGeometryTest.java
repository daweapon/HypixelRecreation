package dev.daweapon.dungeons.catacombs.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellGeometryTest {
    @Test
    void blockOriginUsesPitchFromDungeonCorner() {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(new Cell(2, 3));
        assertEquals(-200 + 64, origin.x());
        assertEquals(68, origin.y());
        assertEquals(-200 + 96, origin.z());
    }

    @Test
    void doorSitsInTheGapBetweenRooms() {
        CellGeometry.BlockPosition east = CellGeometry.doorPosition(new Cell(0, 0), new Cell(1, 0));
        assertEquals(-200 + 31, east.x());
        assertEquals(-200 + 15, east.z());
        CellGeometry.BlockPosition south = CellGeometry.doorPosition(new Cell(1, 1), new Cell(1, 0));
        assertEquals(-200 + 32 + 15, south.x());
        assertEquals(-200 + 31, south.z());
    }

    @Test
    void cellLookupSkipsTheDoorGap() {
        assertEquals(new Cell(0, 0), CellGeometry.cellAt(-200, -200).orElseThrow());
        assertEquals(new Cell(1, 0), CellGeometry.cellAt(-200 + 32, -200).orElseThrow());
        assertTrue(CellGeometry.cellAt(-200 + 31, -200).isEmpty());
        assertTrue(CellGeometry.cellAt(-201, -200).isEmpty());
    }

    @Test
    void halfTurnTwiceReturnsTheOriginalPoint() {
        for (RoomShape shape : RoomShape.values()) {
            CellGeometry.PlanarPoint once = CellGeometry.rotateLocal(7.5, 20.5, shape, Rotation.DEG_180);
            CellGeometry.PlanarPoint twice = CellGeometry.rotateLocal(once.x(), once.z(), shape, Rotation.DEG_180);
            assertEquals(7.5, twice.x(), 1e-9, shape.name());
            assertEquals(20.5, twice.z(), 1e-9, shape.name());
        }
    }

    @Test
    void rotatedPointStaysInsideTheRotatedFootprint() {
        RoomShape shape = RoomShape.ONE_BY_THREE;
        for (Rotation rotation : Rotation.values()) {
            CellGeometry.PlanarPoint point = CellGeometry.rotateLocal(90.5, 10.5, shape, rotation);
            assertTrue(point.x() >= 0 && point.x() <= CellGeometry.widthBlocks(shape, rotation), rotation.name());
            assertTrue(point.z() >= 0 && point.z() <= CellGeometry.depthBlocks(shape, rotation), rotation.name());
        }
    }
}
