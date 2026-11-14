package dev.daweapon.type.hub.carnival;

import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.List;

/** One terrace walkway of the Zombie Shootout, as the captured zombies walk it from end to end. */
final class ZombieLane {
    static final ZombieLane UPPER = new ZombieLane(72, List.of(
            new Vec(-115.6, 72, 37.8), new Vec(-116.8, 72, 42.1), new Vec(-117, 72, 46.5), new Vec(-116, 72, 50.5),
            new Vec(-113.5, 72, 54.5), new Vec(-111.8, 72, 56.7), new Vec(-110, 72, 58), new Vec(-106.5, 72, 60),
            new Vec(-103.5, 72, 61), new Vec(-99.5, 72, 61), new Vec(-97, 72, 60.5), new Vec(-92.8, 72, 58.7)));
    static final ZombieLane LOWER = new ZombieLane(71, List.of(
            new Vec(-111.7, 71, 37.8), new Vec(-113.5, 71, 41), new Vec(-114, 71, 45.5), new Vec(-112, 71, 51.5),
            new Vec(-110, 71, 54), new Vec(-107.5, 71, 56), new Vec(-103.5, 71, 58), new Vec(-99.5, 71, 58),
            new Vec(-93.9, 71, 55.7)));

    private final double row;
    private final List<Vec> points;
    private final double[] distances;
    private final double length;

    private ZombieLane(double row, List<Vec> points) {
        this.row = row;
        this.points = points;
        this.distances = new double[points.size()];
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            total += points.get(i).distance(points.get(i - 1));
            distances[i] = total;
        }
        this.length = total;
    }

    static ZombieLane forRow(double row) {
        return row >= 72 ? UPPER : LOWER;
    }

    double length() {
        return length;
    }

    double nearestDistance(Vec spot) {
        double best = 0;
        double bestGap = Double.MAX_VALUE;
        for (int i = 1; i < points.size(); i++) {
            Vec from = points.get(i - 1);
            Vec to = points.get(i);
            Vec segment = to.sub(from);
            double along = Math.max(0, Math.min(1, spot.sub(from).dot(segment) / segment.lengthSquared()));
            double gap = from.add(segment.mul(along)).distance(spot);
            if (gap < bestGap) {
                bestGap = gap;
                best = distances[i - 1] + along * segment.length();
            }
        }
        return best;
    }

    Vec at(double distance) {
        double clamped = Math.max(0, Math.min(length, distance));
        int i = 1;
        while (i < points.size() - 1 && distances[i] < clamped) i++;
        Vec from = points.get(i - 1);
        Vec to = points.get(i);
        double span = distances[i] - distances[i - 1];
        double along = span == 0 ? 0 : (clamped - distances[i - 1]) / span;
        return from.add(to.sub(from).mul(along)).withY(row);
    }
}
