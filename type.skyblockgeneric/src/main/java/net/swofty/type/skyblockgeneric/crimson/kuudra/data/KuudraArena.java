package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Kuudra's Hollow coordinates from {@code arena.yml} (read from the captured runs): every run's layout. */
public record KuudraArena(String world, int floorY, int lavaSurfaceY, Pos spawn, Pos elle, Pos respawn, Pos ballista,
                          Box platform, Box bridge, Box sea, Map<String, Pos> piles,
                          Map<String, SupplySpot> supplySpots, Pos ellePlatform, Pos perkShop, List<Pos> cannons,
                          Map<String, Pos> kuudraSides, Pos exit) {

    static KuudraArena parse(Map<String, Object> yaml) {
        Map<String, Pos> piles = new LinkedHashMap<>();
        Yamls.map(yaml.get("piles")).forEach((name, position) -> piles.put(name, pos(position)));
        Map<String, Pos> sides = new LinkedHashMap<>();
        Yamls.map(yaml.get("kuudra_sides")).forEach((name, position) -> sides.put(name, pos(position)));
        Map<String, SupplySpot> spots = new LinkedHashMap<>();
        Yamls.map(yaml.get("supply_spots")).forEach((name, spot) -> {
            Map<String, Object> entry = Yamls.map(spot);
            Object crate = entry.get("crate");
            spots.put(name, new SupplySpot(name, pos(entry.get("position")), Yamls.decimal(entry, "radius", 0),
                    crate == null ? null : pos(crate)));
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
                pos(yaml.get("elle_platform")),
                pos(yaml.get("perk_shop")),
                Yamls.list(yaml.get("cannons")).stream().map(KuudraArena::pos).toList(),
                Map.copyOf(sides),
                pos(yaml.get("exit")));
    }

    /** The middle of the platform's top, where the Ballista stands. */
    public Pos centre() {
        return new Pos(ballista.x(), floorY, ballista.z());
    }

    /**
     * The point {@code distance} blocks out into the lava from the platform face nearest {@code near}, on the
     * lava surface. Placeholder-friendly: the platform is treated as its bounding box.
     */
    public Pos lavaBeside(Pos near, double distance) {
        double toWest = near.x() - platform.minX();
        double toEast = platform.maxX() + 1 - near.x();
        double toNorth = near.z() - platform.minZ();
        double toSouth = platform.maxZ() + 1 - near.z();
        double nearest = Math.min(Math.min(toWest, toEast), Math.min(toNorth, toSouth));
        double y = lavaSurfaceY;
        if (nearest == toWest) return new Pos(platform.minX() - distance, y, near.z());
        if (nearest == toEast) return new Pos(platform.maxX() + 1 + distance, y, near.z());
        if (nearest == toNorth) return new Pos(near.x(), y, platform.minZ() - distance);
        return new Pos(near.x(), y, platform.maxZ() + 1 + distance);
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

    /** A named supply spot; {@code crate} is where its crate floats, when the map gives one. */
    public record SupplySpot(String name, Pos position, double radius, @Nullable Pos crate) {
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
