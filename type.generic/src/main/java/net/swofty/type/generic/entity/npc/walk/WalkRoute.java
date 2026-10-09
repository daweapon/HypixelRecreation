package net.swofty.type.generic.entity.npc.walk;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.List;

/**
 * A polyline an NPC glides along at a fixed pace, facing its direction of travel and turning to
 * the destination's own facing on arrival.
 */
public final class WalkRoute {
    private final List<Vec> points;
    private final double[] cumulative;
    private final Pos destination;

    public WalkRoute(List<Vec> points, Pos destination) {
        if (points.isEmpty()) throw new IllegalArgumentException("A route needs at least one point");
        this.points = List.copyOf(points);
        this.destination = destination;
        this.cumulative = new double[this.points.size()];
        for (int i = 1; i < this.points.size(); i++) {
            cumulative[i] = cumulative[i - 1] + this.points.get(i - 1).distance(this.points.get(i));
        }
    }

    public double length() {
        return cumulative[cumulative.length - 1];
    }

    public Pos destination() {
        return destination;
    }

    public Vec end() {
        return points.getLast();
    }

    public static int ticksToWalk(double length, double blocksPerTick) {
        return (int) Math.ceil(length / blocksPerTick);
    }

    /**
     * Where the NPC is after walking {@code distance} blocks along the route.
     */
    public Pos at(double distance, float fallbackYaw) {
        if (distance <= 0 || points.size() == 1) {
            Vec start = points.getFirst();
            return new Pos(start.x(), start.y(), start.z(), points.size() == 1 ? fallbackYaw : yawOf(0), 0);
        }
        int segment = 1;
        while (segment < points.size() - 1 && cumulative[segment] < distance) segment++;
        Vec from = points.get(segment - 1);
        Vec to = points.get(segment);
        double span = cumulative[segment] - cumulative[segment - 1];
        double progress = span == 0 ? 1 : Math.min(1, (distance - cumulative[segment - 1]) / span);
        Vec point = from.lerp(to, progress);
        return new Pos(point.x(), point.y(), point.z(), yawOf(segment - 1), 0);
    }

    private float yawOf(int segment) {
        for (int i = segment; i < points.size() - 1; i++) {
            Vec from = points.get(i);
            Vec to = points.get(i + 1);
            double dx = to.x() - from.x();
            double dz = to.z() - from.z();
            if (Math.abs(dx) > 1.0E-6 || Math.abs(dz) > 1.0E-6) {
                return (float) Math.toDegrees(Math.atan2(-dx, dz));
            }
        }
        return destination.yaw();
    }
}
