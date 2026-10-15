package dev.daweapon.type.ravengarddungeon.config;

import net.minestom.server.coordinate.Pos;

import java.util.List;

// Deserialized JSON config for a Ravengard dungeon map: its id/name, world file, spawn point, and placed objects.
public record RavengardDungeonConfig(String id, String name, String polar, Position spawn,
                                     List<DungeonObject> objects) {
    public RavengardDungeonConfig {
        objects = objects == null ? List.of() : List.copyOf(objects);
    }

    public Pos spawnPosition() {
        return spawn == null ? new Pos(0.5, 65, 0.5) : spawn.toPos();
    }

    public record Position(double x, double y, double z, float yaw, float pitch) {
        public Pos toPos() {
            return new Pos(x, y, z, yaw, pitch);
        }
    }

    public record DungeonObject(String category, String type, double x, double y, double z,
                                float yaw, float pitch) {
    }
}
