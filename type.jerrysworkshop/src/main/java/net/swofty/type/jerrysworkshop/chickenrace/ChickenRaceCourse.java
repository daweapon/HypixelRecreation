package net.swofty.type.jerrysworkshop.chickenrace;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.swofty.commons.YamlFileUtils;
import net.swofty.type.skyblockgeneric.chickenrace.Cluckpoint;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The hoop course from {@code configuration/skyblock/chicken_race.yml}. */
public record ChickenRaceCourse(double speed, double startingSeconds, double greenHoopSeconds,
                                double cluckpointSeconds, double hoopRadius, Pos start, Pos finish,
                                List<Hoop> hoops) {
    private static final File FILE = new File("./configuration/skyblock/chicken_race.yml");
    private static ChickenRaceCourse loaded;

    public record Hoop(Vec center, Vec normal, boolean green, @Nullable Cluckpoint cluckpoint) {
        /** Through the ring: within a block of its plane and inside its radius. */
        public boolean contains(Vec point, double radius) {
            Vec offset = point.sub(center);
            double along = offset.dot(normal);
            if (Math.abs(along) > 1) return false;
            return offset.sub(normal.mul(along)).length() <= radius;
        }
    }

    public static synchronized ChickenRaceCourse get() {
        if (loaded == null) loaded = load();
        return loaded;
    }

    @SuppressWarnings("unchecked")
    private static ChickenRaceCourse load() {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(FILE);
            Pos start = pos((Map<String, Object>) root.get("start"));
            List<Hoop> hoops = new ArrayList<>();
            Vec previous = start.asVec();
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.getOrDefault("hoops", List.of())) {
                Vec center = new Vec(number(entry, "x", 0), number(entry, "y", 0), number(entry, "z", 0));
                Vec direction = center.sub(previous);
                Vec normal = direction.isZero() ? new Vec(1, 0, 0) : direction.normalize();
                Object cluckpoint = entry.get("cluckpoint");
                hoops.add(new Hoop(center, normal, Boolean.TRUE.equals(entry.get("green")),
                        cluckpoint == null ? null : Cluckpoint.of(((Number) cluckpoint).intValue())));
                previous = center;
            }
            return new ChickenRaceCourse(number(root, "speed", 10), number(root, "starting_seconds", 20),
                    number(root, "green_hoop_seconds", 5), number(root, "cluckpoint_seconds", 10),
                    number(root, "hoop_radius", 3), start, pos((Map<String, Object>) root.get("finish")), List.copyOf(hoops));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load the Chicken Race course");
            Pos gulliver = new Pos(66.5, 105, 33.5, -90, 0);
            return new ChickenRaceCourse(10, 20, 5, 10, 3, gulliver, gulliver, List.of());
        }
    }

    private static Pos pos(Map<String, Object> map) {
        return new Pos(number(map, "x", 0), number(map, "y", 0), number(map, "z", 0),
                (float) number(map, "yaw", 0), (float) number(map, "pitch", 0));
    }

    private static double number(Map<String, Object> map, String key, double fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}
