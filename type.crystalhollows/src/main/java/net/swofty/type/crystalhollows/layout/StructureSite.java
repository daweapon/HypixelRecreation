package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Point;

public record StructureSite(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public StructureSite {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            int x = minX, y = minY, z = minZ;
            minX = Math.min(x, maxX);
            maxX = Math.max(x, maxX);
            minY = Math.min(y, maxY);
            maxY = Math.max(y, maxY);
            minZ = Math.min(z, maxZ);
            maxZ = Math.max(z, maxZ);
        }
    }

    public int width() {
        return maxX - minX + 1;
    }

    public int height() {
        return maxY - minY + 1;
    }

    public int length() {
        return maxZ - minZ + 1;
    }

    public boolean startsAt(Point origin) {
        return origin.blockX() == minX && origin.blockY() == minY && origin.blockZ() == minZ;
    }

    public Point min() {
        return new net.minestom.server.coordinate.Vec(minX, minY, minZ);
    }
}
