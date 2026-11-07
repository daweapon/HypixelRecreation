package dev.daweapon.dungeons.catacombs.puzzle.creeperbeams;

import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Creeper Beams room: shooting one sea lantern and then another joins them with a beam, shooting a joined lantern
 * breaks its beam again. The room is solved once enough beams cross the creeper's space in the middle of the room.
 */
public final class CreeperBeams {
    public record Beam(RoomBlock first, RoomBlock second) {
    }

    public record Space(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        boolean crossedBy(RoomBlock from, RoomBlock to) {
            double[] start = {from.x() + 0.5, from.y() + 0.5, from.z() + 0.5};
            double[] end = {to.x() + 0.5, to.y() + 0.5, to.z() + 0.5};
            double[] low = {minX, minY, minZ};
            double[] high = {maxX, maxY, maxZ};
            double enter = 0;
            double leave = 1;
            for (int axis = 0; axis < 3; axis++) {
                double delta = end[axis] - start[axis];
                if (Math.abs(delta) < 1e-9) {
                    if (start[axis] < low[axis] || start[axis] > high[axis]) return false;
                    continue;
                }
                double near = (low[axis] - start[axis]) / delta;
                double far = (high[axis] - start[axis]) / delta;
                enter = Math.max(enter, Math.min(near, far));
                leave = Math.min(leave, Math.max(near, far));
                if (enter > leave) return false;
            }
            return true;
        }
    }

    public sealed interface Shot {
        record Missed() implements Shot {
        }

        record Selected(RoomBlock lantern) implements Shot {
        }

        record Deselected(RoomBlock lantern) implements Shot {
        }

        record Joined(Beam beam) implements Shot {
        }

        record Broken(Beam beam) implements Shot {
        }
    }

    private final List<RoomBlock> lanterns;
    private final Space creeper;
    private final int required;
    private final Map<RoomBlock, Beam> beams = new HashMap<>();
    private RoomBlock selected;

    public CreeperBeams(List<RoomBlock> lanterns, Space creeper, int required) {
        this.lanterns = List.copyOf(lanterns);
        this.creeper = creeper;
        this.required = required;
    }

    public Shot shoot(RoomBlock lantern) {
        if (!lanterns.contains(lantern)) return new Shot.Missed();
        Beam joined = beams.get(lantern);
        if (joined != null) {
            beams.remove(joined.first());
            beams.remove(joined.second());
            return new Shot.Broken(joined);
        }
        if (selected == null) {
            selected = lantern;
            return new Shot.Selected(lantern);
        }
        if (selected.equals(lantern)) {
            selected = null;
            return new Shot.Deselected(lantern);
        }
        RoomBlock first = selected;
        selected = null;
        return new Shot.Joined(connect(first, lantern));
    }

    public Beam connect(RoomBlock first, RoomBlock second) {
        Beam beam = new Beam(first, second);
        beams.put(first, beam);
        beams.put(second, beam);
        return beam;
    }

    public boolean crossesCreeper(Beam beam) {
        return creeper.crossedBy(beam.first(), beam.second());
    }

    public List<Beam> beams() {
        return new ArrayList<>(beams.values().stream().distinct().toList());
    }

    public long beamsThroughCreeper() {
        return beams().stream().filter(this::crossesCreeper).count();
    }

    public boolean solved() {
        return beamsThroughCreeper() >= required;
    }

    public Optional<RoomBlock> selected() {
        return Optional.ofNullable(selected);
    }

    public List<RoomBlock> lanterns() {
        return lanterns;
    }

    public int required() {
        return required;
    }
}
