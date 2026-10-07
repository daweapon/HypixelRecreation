package net.swofty.type.skyblockgeneric.spray;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Every spray in {@code configuration/skyblock/sprays.yml}, sorted alphabetically as in the Spray Can. */
public final class SprayCatalogue {
    static final Path DIRECTORY = Path.of("./configuration/skyblock");
    private static final Path FILE = DIRECTORY.resolve("sprays.yml");
    private static volatile Map<String, Spray> sprays;

    private SprayCatalogue() {}

    public static List<Spray> all() {
        return List.copyOf(loaded().values());
    }

    public static Optional<Spray> byId(String id) {
        return Optional.ofNullable(id == null ? null : loaded().get(id));
    }

    private static Map<String, Spray> loaded() {
        Map<String, Spray> current = sprays;
        if (current != null) return current;
        synchronized (SprayCatalogue.class) {
            if (sprays == null) sprays = parse(FILE);
            return sprays;
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Spray> parse(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(stream);
            List<Spray> parsed = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.getOrDefault("sprays", List.of())) {
                Map<SprayInk, Integer> ink = new EnumMap<>(SprayInk.class);
                ((Map<String, Object>) entry.getOrDefault("ink", Map.of()))
                        .forEach((dye, percent) -> ink.put(SprayInk.valueOf(dye), ((Number) percent).intValue()));
                parsed.add(new Spray(
                        (String) entry.get("id"),
                        (String) entry.get("name"),
                        SprayCategory.valueOf((String) entry.get("category")),
                        SpraySource.valueOf((String) entry.get("source")),
                        ((Number) entry.getOrDefault("directions", 1)).intValue(),
                        (String) entry.get("image"),
                        (String) entry.get("rank"),
                        ((List<Object>) entry.getOrDefault("lore", List.of())).stream().map(Objects::toString).toList(),
                        Collections.unmodifiableMap(ink)));
            }
            parsed.sort(Comparator.comparing(spray -> spray.name().toLowerCase()));
            Map<String, Spray> result = new LinkedHashMap<>();
            parsed.forEach(spray -> result.put(spray.id(), spray));
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read " + file, e);
        }
    }
}
