package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;

import java.util.ArrayList;
import java.util.List;

/** The 3 wide, 4 tall doorway a door fills in the one-block gap between two rooms. */
public record DoorFrame(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 4;

    public static DoorFrame of(LayoutDoor door) {
        CellGeometry.BlockPosition centre = CellGeometry.doorPosition(door.from(), door.to());
        int reach = WIDTH / 2;
        boolean eastWest = door.from().row() == door.to().row();
        int minY = centre.y() + 1;
        return eastWest
                ? new DoorFrame(centre.x(), minY, centre.z() - reach, centre.x(), minY + HEIGHT - 1, centre.z() + reach)
                : new DoorFrame(centre.x() - reach, minY, centre.z(), centre.x() + reach, minY + HEIGHT - 1, centre.z());
    }

    public boolean containsColumn(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public double centreX() {
        return (minX + maxX) / 2.0 + 0.5;
    }

    public double centreY() {
        return (minY + maxY) / 2.0 + 0.5;
    }

    public double centreZ() {
        return (minZ + maxZ) / 2.0 + 0.5;
    }

    public List<int[]> blocks() {
        List<int[]> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new int[]{x, y, z});
                }
            }
        }
        return blocks;
    }
}
