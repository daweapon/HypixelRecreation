package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StructureFile {
    public static final class Entry {
        private final String id;
        private final String name;
        private final String region;
        private final boolean optional;
        private final int clearance;
        private final List<Pos> candidates = new ArrayList<>();
        private final Map<String, Pos> anchors = new LinkedHashMap<>();

        private Entry(String id, String name, String region, boolean optional, int clearance) {
            this.id = id;
            this.name = name;
            this.region = region;
            this.optional = optional;
            this.clearance = clearance;
        }

        public String id() {
            return id;
        }

        public List<Pos> candidates() {
            return List.copyOf(candidates);
        }

        public Map<String, Pos> anchors() {
            return java.util.Collections.unmodifiableMap(anchors);
        }

        public Pos nearestCandidate(Pos position) {
            Pos best = null;
            for (Pos candidate : candidates) {
                if (best == null || candidate.distanceSquared(position) < best.distanceSquared(position)) best = candidate;
            }
            return best;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private StructureFile() {
    }

    public static StructureFile read(Path file) {
        StructureFile document = new StructureFile();
        if (!Files.exists(file)) return document;
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            if (root == null) return document;
            for (Map<String, Object> raw : listOf(root.get("structures"))) {
                document.entries.add(parse(raw));
            }
            return document;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listOf(Object value) {
        return value == null ? List.of() : (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static Entry parse(Map<String, Object> raw) {
        Entry entry = new Entry((String) raw.get("id"), (String) raw.get("name"), (String) raw.get("region"),
                Boolean.TRUE.equals(raw.get("optional")), ((Number) raw.getOrDefault("clearance", 40)).intValue());
        Object candidates = raw.get("candidates");
        if (candidates != null) {
            for (Object candidate : (List<Object>) candidates) entry.candidates.add(pos((Map<String, Object>) candidate));
        }
        Object anchors = raw.get("anchors");
        if (anchors != null) {
            ((Map<String, Object>) anchors).forEach((name, value) -> {
                Map<String, Object> map = (Map<String, Object>) value;
                entry.anchors.put(name, map == null || map.isEmpty() ? null : pos(map));
            });
        }
        return entry;
    }

    private static Pos pos(Map<String, Object> raw) {
        return new Pos(num(raw.get("x")), num(raw.get("y")), num(raw.get("z")), (float) num(raw.get("yaw")), (float) num(raw.get("pitch")));
    }

    private static double num(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Entry find(String id) {
        for (Entry entry : entries) if (entry.id.equals(id)) return entry;
        return null;
    }

    private Entry require(String id) {
        Entry entry = find(id);
        if (entry == null) throw new IllegalArgumentException("Unknown structure " + id);
        return entry;
    }

    public void addCandidate(String id, Pos origin) {
        Entry entry = require(id);
        if (!entry.candidates.contains(origin)) entry.candidates.add(origin);
    }

    public void clearCandidates(String id) {
        require(id).candidates.clear();
    }

    public void setAnchor(String id, String name, Pos offset) {
        require(id).anchors.put(name, offset);
    }

    public String render() {
        StringBuilder out = new StringBuilder("structures:\n");
        for (Entry entry : entries) {
            out.append("  - id: ").append(entry.id).append('\n');
            out.append("    name: ").append(quote(entry.name)).append('\n');
            out.append("    region: ").append(entry.region).append('\n');
            out.append("    optional: ").append(entry.optional).append('\n');
            out.append("    clearance: ").append(entry.clearance).append('\n');
            if (entry.candidates.isEmpty()) {
                out.append("    candidates: []\n");
            } else {
                out.append("    candidates:\n");
                entry.candidates.forEach(candidate -> out.append("      - ").append(flow(candidate)).append('\n'));
            }
            if (entry.anchors.isEmpty()) {
                out.append("    anchors: {}\n");
            } else {
                out.append("    anchors:\n");
                entry.anchors.forEach((name, offset) ->
                        out.append("      ").append(name).append(": ").append(offset == null ? "{}" : flow(offset)).append('\n'));
            }
        }
        return out.toString();
    }

    public void write(Path file) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            Path temp = Files.createTempFile(parent, "structures", ".tmp");
            Files.writeString(temp, render(), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write " + file, e);
        }
    }

    private static String quote(String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    private static String flow(Pos pos) {
        return "{x: " + number(pos.x()) + ", y: " + number(pos.y()) + ", z: " + number(pos.z())
                + ", yaw: " + angle(pos.yaw()) + ", pitch: " + angle(pos.pitch()) + "}";
    }

    static String angle(float value) {
        return number(Double.parseDouble(Float.toString(value)));
    }

    static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
