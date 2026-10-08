package net.swofty.type.jerrysworkshop.chickenrace;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.swofty.commons.YamlFileUtils;
import net.swofty.type.skyblockgeneric.chickenrace.ChickenFlight;
import net.swofty.type.skyblockgeneric.chickenrace.Cluckpoint;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The hoop course from {@code configuration/skyblock/chicken_race.yml}. */
public record ChickenRaceCourse(ChickenFlight.Physics physics, int launchGraceTicks, double tintStartSeconds,
                                double tintFullSeconds, Pos start, Pos returnTo, List<Hoop> hoops, Hoop finish) {
    private static final File FILE = new File("./configuration/skyblock/chicken_race.yml");
    private static final ChickenFlight.Physics DEFAULT_PHYSICS = new ChickenFlight.Physics(8, 0.9, 0.06, 0.75, 20);
    private static ChickenRaceCourse loaded;

    public record Hoop(Vec center, Vec normal, double radius, @Nullable Cluckpoint cluckpoint) {
        /** Whether moving from {@code from} to {@code to} flew forwards through the ring. */
        public boolean crossed(Vec from, Vec to) {
            double before = from.sub(center).dot(normal);
            double after = to.sub(center).dot(normal);
            if (before > 0 || after < 0) return false;
            double fraction = before == after ? 0 : before / (before - after);
            Vec through = from.add(to.sub(from).mul(fraction));
            return through.distance(center) <= radius;
        }

        public boolean timeHoop() {
            return cluckpoint != null;
        }
    }

    /** The hoops in order followed by the finish. */
    public List<Hoop> gates() {
        List<Hoop> gates = new ArrayList<>(hoops);
        gates.add(finish);
        return gates;
    }

    public static synchronized ChickenRaceCourse get() {
        if (loaded == null) loaded = load();
        return loaded;
    }

    @SuppressWarnings("unchecked")
    private static ChickenRaceCourse load() {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(FILE);
            Map<String, Object> physics = (Map<String, Object>) root.getOrDefault("physics", Map.of());
            Map<String, Object> tint = (Map<String, Object>) root.getOrDefault("tint", Map.of());
            double radius = number(root, "hoop_radius", 3.5);
            List<Hoop> hoops = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.getOrDefault("hoops", List.of())) {
                Object cluckpoint = entry.get("cluckpoint");
                hoops.add(hoop(entry, radius, cluckpoint == null ? null : Cluckpoint.of(((Number) cluckpoint).intValue())));
            }
            Map<String, Object> finish = (Map<String, Object>) root.get("finish");
            return new ChickenRaceCourse(
                    new ChickenFlight.Physics(
                            number(physics, "blocks_per_second", DEFAULT_PHYSICS.blocksPerSecond()),
                            number(physics, "flap_velocity", DEFAULT_PHYSICS.flapVelocity()),
                            number(physics, "gravity", DEFAULT_PHYSICS.gravity()),
                            number(physics, "terminal_velocity", DEFAULT_PHYSICS.terminalVelocity()),
                            (int) number(physics, "flap_cooldown_ticks", DEFAULT_PHYSICS.flapCooldownTicks())),
                    (int) number(root, "launch_grace_ticks", 10),
                    number(tint, "start_seconds", 12), number(tint, "full_seconds", 7),
                    pos((Map<String, Object>) root.get("start")), pos((Map<String, Object>) root.get("return")),
                    List.copyOf(hoops), hoop(finish, number(finish, "radius", radius), Cluckpoint.FOUR));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load the Chicken Race course");
            Pos walkway = new Pos(66.5, 106, 30.5, -45, 0);
            return new ChickenRaceCourse(DEFAULT_PHYSICS, 10, 12, 7, walkway, walkway, List.of(), null);
        }
    }

    private static Hoop hoop(Map<String, Object> entry, double radius, @Nullable Cluckpoint cluckpoint) {
        Pos pos = pos(entry);
        return new Hoop(pos.asVec(), pos.direction(), radius, cluckpoint);
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
