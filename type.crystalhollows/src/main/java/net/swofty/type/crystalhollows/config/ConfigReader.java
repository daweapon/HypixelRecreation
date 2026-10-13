package net.swofty.type.crystalhollows.config;

import net.minestom.server.coordinate.Pos;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ConfigReader {
    private ConfigReader() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> read(Path file) {
        if (!Files.exists(file)) return Map.of();
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            return root == null ? Map.of() : root;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> list(Map<String, Object> root, String key) {
        Object value = root.get(key);
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    public static Pos pos(Map<String, Object> raw) {
        return new Pos(num(raw.get("x")), num(raw.get("y")), num(raw.get("z")), (float) num(raw.get("yaw")), (float) num(raw.get("pitch")));
    }

    public static Box box(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;
        return Box.of(pos(map(raw, "min")), pos(map(raw, "max")));
    }

    private static double num(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }
}
