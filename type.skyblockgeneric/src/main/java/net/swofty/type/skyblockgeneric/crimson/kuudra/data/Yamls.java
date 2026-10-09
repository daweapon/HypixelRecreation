package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

final class Yamls {
    private Yamls() {
    }

    static Map<String, Object> load(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(stream);
            return root == null ? Map.of() : root;
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read " + file, e);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object value) {
        return value == null ? Map.of() : (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    static List<Object> list(Object value) {
        return value == null ? List.of() : (List<Object>) value;
    }

    static List<Map<String, Object>> maps(Object value) {
        return list(value).stream().map(Yamls::map).toList();
    }

    static List<String> strings(Object value) {
        return list(value).stream().map(Objects::toString).toList();
    }

    static List<Integer> integers(Object value) {
        return list(value).stream().map(each -> ((Number) each).intValue()).toList();
    }

    static int integer(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        return value == null ? fallback : ((Number) value).intValue();
    }

    static long longValue(Map<String, Object> map, String key, long fallback) {
        Object value = map.get(key);
        return value == null ? fallback : ((Number) value).longValue();
    }

    static double decimal(Map<String, Object> map, String key, double fallback) {
        Object value = map.get(key);
        return value == null ? fallback : ((Number) value).doubleValue();
    }

    static boolean bool(Map<String, Object> map, String key) {
        return Boolean.TRUE.equals(map.get(key));
    }

    static String string(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    static <T> Map<KuudraTier, T> byTier(Object value, Function<Object, T> parser) {
        Map<KuudraTier, T> result = new EnumMap<>(KuudraTier.class);
        map(value).forEach((tier, entry) -> result.put(KuudraTier.valueOf(tier), parser.apply(entry)));
        return result;
    }
}
