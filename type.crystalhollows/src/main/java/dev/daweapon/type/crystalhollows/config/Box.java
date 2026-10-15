package dev.daweapon.type.crystalhollows.config;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

public record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

    public boolean contains(Point point) {
        return point.x() >= minX && point.x() <= maxX && point.y() >= minY && point.y() <= maxY
                && point.z() >= minZ && point.z() <= maxZ;
    }

    public Box shift(Point origin) {
        return new Box(minX + origin.x(), minY + origin.y(), minZ + origin.z(),
                maxX + origin.x(), maxY + origin.y(), maxZ + origin.z());
    }

    public static Box of(Point a, Point b) {
        return new Box(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z()),
                Math.max(a.x(), b.x()) + 1, Math.max(a.y(), b.y()) + 1, Math.max(a.z(), b.z()) + 1);
    }

    public Pos centre() {
        return new Pos((minX + maxX) / 2, minY, (minZ + maxZ) / 2);
    }
}
