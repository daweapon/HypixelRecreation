package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.minestom.server.coordinate.Vec;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class NucleusConfig {
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/nucleus.yml");

    private static volatile NucleusConfig loaded;

    private final Map<NucleusCrystal, Vec> statues;

    private NucleusConfig(Map<NucleusCrystal, Vec> statues) {
        this.statues = statues;
    }

    public static NucleusConfig get() {
        NucleusConfig config = loaded;
        if (config == null) loaded = config = read();
        return config;
    }

    public static void reload() {
        loaded = read();
    }

    @SuppressWarnings("unchecked")
    private static NucleusConfig read() {
        Map<NucleusCrystal, Vec> statues = new EnumMap<>(NucleusCrystal.class);
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                Map<String, Object> root = new Yaml().load(in);
                if (root != null) {
                    for (Map<String, Object> entry : (List<Map<String, Object>>) root.getOrDefault("statues", List.of())) {
                        statues.put(NucleusCrystal.valueOf((String) entry.get("crystal")), vec(entry));
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + FILE, e);
            }
        }
        return new NucleusConfig(statues);
    }

    private static Vec vec(Map<String, Object> raw) {
        return new Vec(Math.floor(num(raw.get("x"))), Math.floor(num(raw.get("y"))), Math.floor(num(raw.get("z"))));
    }

    private static double num(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }

    public static final int STATUE_HALF_WIDTH = 5;
    public static final int STATUE_HEIGHT = 12;

    public NucleusCrystal statueAt(int x, int y, int z) {
        for (Map.Entry<NucleusCrystal, Vec> entry : statues.entrySet()) {
            Vec at = entry.getValue();
            int dx = x - (int) at.x();
            int dy = y - (int) at.y();
            int dz = z - (int) at.z();
            if (Math.abs(dx) <= STATUE_HALF_WIDTH && dy >= 0 && dy <= STATUE_HEIGHT && Math.abs(dz) <= STATUE_HALF_WIDTH) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static final double PEDESTAL_HEIGHT = 4.5;

    public Vec pedestal(NucleusCrystal crystal) {
        Vec at = statues.get(crystal);
        return at == null ? null : new Vec(at.x() + 0.5, at.y() + PEDESTAL_HEIGHT, at.z() + 0.5);
    }

    public Map<NucleusCrystal, Vec> statues() {
        return statues;
    }
}
