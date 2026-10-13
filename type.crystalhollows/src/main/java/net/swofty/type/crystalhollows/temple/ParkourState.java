package net.swofty.type.crystalhollows.temple;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ParkourState {
    public sealed interface Event permits Started, CheckpointReached, Completed {
    }

    public record Started(String parkour) implements Event {
    }

    public record CheckpointReached(String parkour, int index) implements Event {
    }

    public record Completed(String parkour) implements Event {
    }

    private static final class Run {
        private final Set<Integer> reached = new HashSet<>();
        private Pos respawn;
    }

    private final Map<String, Run> active = new HashMap<>();
    private final Set<String> completed = new HashSet<>();

    public List<Event> update(List<Parkour> parkours, Point at) {
        List<Event> events = new ArrayList<>();
        for (Parkour parkour : parkours) {
            if (completed.contains(parkour.id())) continue;
            Run run = active.get(parkour.id());
            if (run == null) {
                if (parkour.start().contains(at)) {
                    Run started = new Run();
                    started.respawn = parkour.respawn();
                    active.put(parkour.id(), started);
                    events.add(new Started(parkour.id()));
                }
                continue;
            }
            for (int i = 0; i < parkour.checkpoints().size(); i++) {
                Parkour.Checkpoint checkpoint = parkour.checkpoints().get(i);
                if (checkpoint.box().contains(at) && run.reached.add(i)) {
                    run.respawn = checkpoint.respawn();
                    events.add(new CheckpointReached(parkour.id(), i));
                }
            }
            if (run.reached.size() == parkour.checkpoints().size() && parkour.finish().contains(at)) {
                active.remove(parkour.id());
                completed.add(parkour.id());
                events.add(new Completed(parkour.id()));
            }
        }
        return events;
    }

    public Pos respawn() {
        Pos best = null;
        for (Run run : active.values()) best = run.respawn;
        return best;
    }

    public boolean isComplete(String id) {
        return completed.contains(id);
    }

    public int completedCount() {
        return completed.size();
    }

    public boolean allComplete(List<Parkour> parkours) {
        return parkours.stream().allMatch(parkour -> completed.contains(parkour.id()));
    }
}
