package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.region.RegionType;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StructureCatalogue {
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/structures.yml");

    private StructureCatalogue() {
    }

    public static List<StructureSpec> load() {
        return load(FILE);
    }

    @SuppressWarnings("unchecked")
    public static List<StructureSpec> load(Path file) {
        if (!Files.exists(file)) return List.of();
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            List<StructureSpec> specs = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.getOrDefault("structures", List.of())) {
                specs.add(parse(entry));
            }
            return specs;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static StructureSpec parse(Map<String, Object> entry) {
        List<Pos> candidates = new ArrayList<>();
        for (Object candidate : (List<Object>) entry.getOrDefault("candidates", List.of())) {
            candidates.add(pos((Map<String, Object>) candidate));
        }
        Map<String, Pos> anchors = new LinkedHashMap<>();
        Map<String, Object> rawAnchors = (Map<String, Object>) entry.getOrDefault("anchors", Map.of());
        rawAnchors.forEach((name, value) -> anchors.put(name, pos((Map<String, Object>) value)));
        return new StructureSpec(
                (String) entry.get("id"),
                (String) entry.get("name"),
                RegionType.valueOf((String) entry.get("region")),
                Boolean.TRUE.equals(entry.get("optional")),
                ((Number) entry.getOrDefault("clearance", 40)).intValue(),
                List.copyOf(candidates),
                Map.copyOf(anchors));
    }

    private static Pos pos(Map<String, Object> raw) {
        return new Pos(num(raw.get("x")), num(raw.get("y")), num(raw.get("z")), (float) num(raw.get("yaw")), (float) num(raw.get("pitch")));
    }

    private static double num(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }
}
