package net.swofty.type.crystalhollows.temple;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.config.Box;

import java.util.ArrayList;
import java.util.List;

public record Parkour(String id, Box start, Pos respawn, List<Checkpoint> checkpoints, Box finish) {

    public record Checkpoint(Box box, Pos respawn) {
    }

    public Parkour shift(Point origin) {
        List<Checkpoint> shifted = new ArrayList<>();
        for (Checkpoint checkpoint : checkpoints) {
            shifted.add(new Checkpoint(checkpoint.box().shift(origin), offset(checkpoint.respawn(), origin)));
        }
        return new Parkour(id, start.shift(origin), offset(respawn, origin), List.copyOf(shifted), finish.shift(origin));
    }

    private static Pos offset(Pos pos, Point origin) {
        return new Pos(pos.x() + origin.x(), pos.y() + origin.y(), pos.z() + origin.z(), pos.yaw(), pos.pitch());
    }
}
