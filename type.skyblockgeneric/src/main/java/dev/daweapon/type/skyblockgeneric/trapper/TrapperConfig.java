package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.commons.YamlFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TrapperConfig {
    private static final File FILE = new File("./configuration/skyblock/trapper.yml");
    private static TrapperConfig loaded;

    private final long cooldownMillis;
    private final long timeLimitMillis;
    private final List<Zone> zones;

    public record Zone(String name, int x1, int z1, int x2, int z2, int minY, int maxY) {
        public boolean contains(double x, double z) {
            return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) && z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
        }
    }

    private TrapperConfig(long cooldownMillis, long timeLimitMillis, List<Zone> zones) {
        this.cooldownMillis = cooldownMillis;
        this.timeLimitMillis = timeLimitMillis;
        this.zones = zones;
    }

    public static synchronized TrapperConfig get() {
        if (loaded == null) loaded = load(FILE);
        return loaded;
    }

    @SuppressWarnings("unchecked")
    public static TrapperConfig load(File file) {
        try {
            Map<String, Object> raw = YamlFileUtils.loadYaml(file);
            List<Zone> zones = new ArrayList<>();
            for (Map<String, Object> zone : (List<Map<String, Object>>) raw.get("spawn_zones")) {
                zones.add(new Zone((String) zone.get("name"), number(zone, "x1"), number(zone, "z1"), number(zone, "x2"),
                        number(zone, "z2"), number(zone, "min_y"), number(zone, "max_y")));
            }
            return new TrapperConfig(number(raw, "cooldown_seconds") * 1000L, number(raw, "time_limit_minutes") * 60_000L, List.copyOf(zones));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static int number(Map<String, Object> map, String key) {
        return ((Number) map.get(key)).intValue();
    }

    public long cooldownMillis() {
        return cooldownMillis;
    }

    public long timeLimitMillis() {
        return timeLimitMillis;
    }

    public List<Zone> zones() {
        return zones;
    }
}
