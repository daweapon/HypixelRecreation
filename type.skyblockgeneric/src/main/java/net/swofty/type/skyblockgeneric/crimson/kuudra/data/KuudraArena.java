package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.minestom.server.coordinate.Pos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Kuudra's Hollow coordinates from {@code arena.yml}: the single place every run reads its layout from. */
public record KuudraArena(String world, int floorY, int lavaSurfaceY, Pos spawn, Pos elle, Pos respawn, Pos ballista,
                          Box platform, Box bridge, Box sea, Map<String, Pos> piles,
                          Map<String, SupplySpot> supplySpots, Pos exit) {

    static KuudraArena parse(Map<String, Object> yaml) {
        Map<String, Pos> piles = new LinkedHashMap<>();
        Yamls.map(yaml.get("piles")).forEach((name, position) -> piles.put(name, pos(position)));
        Map<String, SupplySpot> spots = new LinkedHashMap<>();
        Yamls.map(yaml.get("supply_spots")).forEach((name, spot) -> {
            Map<String, Object> entry = Yamls.map(spot);
            spots.put(name, new SupplySpot(name, pos(entry.get("position")), Yamls.decimal(entry, "radius", 0)));
        });
        return new KuudraArena(
                Yamls.string(yaml, "world"),
                Yamls.integer(yaml, "floor_y", 0),
                Yamls.integer(yaml, "lava_surface_y", 0),
                pos(yaml.get("spawn")),
                pos(yaml.get("elle")),
                pos(yaml.get("respawn")),
                pos(yaml.get("ballista")),
                Box.parse(yaml.get("platform")),
                Box.parse(yaml.get("bridge")),
                Box.parse(yaml.get("sea")),
                Map.copyOf(piles),
                Map.copyOf(spots),
                pos(yaml.get("exit")));
    }

    static Pos pos(Object value) {
        List<Double> numbers = Yamls.list(value).stream().map(each -> ((Number) each).doubleValue()).toList();
        if (numbers.size() != 3 && numbers.size() != 5) {
            throw new IllegalArgumentException("A Kuudra arena position needs 3 or 5 numbers, got " + value);
        }
        return numbers.size() == 3
                ? new Pos(numbers.get(0), numbers.get(1), numbers.get(2))
                : new Pos(numbers.get(0), numbers.get(1), numbers.get(2), numbers.get(3).floatValue(), numbers.get(4).floatValue());
    }

    public boolean onPlatform(Pos position) {
        return platform.containsColumn(position.blockX(), position.blockZ()) && position.y() >= floorY - 1;
    }

    public record SupplySpot(String name, Pos position, double radius) {
    }

    public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public Box {
            if (minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Kuudra arena box min must not exceed max");
            }
        }

        static Box parse(Object value) {
            Map<String, Object> box = Yamls.map(value);
            Pos min = pos(box.get("min"));
            Pos max = pos(box.get("max"));
            return new Box(min.blockX(), min.blockY(), min.blockZ(), max.blockX(), max.blockY(), max.blockZ());
        }

        public boolean contains(int x, int y, int z) {
            return y >= minY && y <= maxY && containsColumn(x, z);
        }

        public boolean containsColumn(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }
}
