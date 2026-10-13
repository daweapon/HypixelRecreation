package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public record HollowsWorld(Pos spawn, Bounds bounds) {
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/world.yml");
    public static final Pos PLACEHOLDER_SPAWN = new Pos(512.5, 120, 512.5, 0, 0);

    public record Bounds(int x1, int z1, int x2, int z2) {
    }

    private static volatile HollowsWorld current;

    public static HollowsWorld get() {
        HollowsWorld world = current;
        if (world == null) current = world = read(FILE);
        return world;
    }

    public static void reload() {
        current = read(FILE);
    }

    public Pos spawnOrPlaceholder() {
        return spawn == null ? PLACEHOLDER_SPAWN : spawn;
    }

    public HollowsWorld withSpawn(Pos newSpawn) {
        return new HollowsWorld(newSpawn, bounds);
    }

    public HollowsWorld withBounds(Bounds newBounds) {
        return new HollowsWorld(spawn, newBounds);
    }

    @SuppressWarnings("unchecked")
    public static HollowsWorld read(Path file) {
        if (!Files.exists(file)) return new HollowsWorld(null, null);
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            if (root == null) return new HollowsWorld(null, null);
            return new HollowsWorld(spawn((Map<String, Object>) root.get("spawn")), bounds((Map<String, Object>) root.get("bounds")));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }

    private static Pos spawn(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;
        return new Pos(num(raw.get("x")), num(raw.get("y")), num(raw.get("z")), (float) num(raw.get("yaw")), (float) num(raw.get("pitch")));
    }

    private static Bounds bounds(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;
        return new Bounds((int) num(raw.get("x1")), (int) num(raw.get("z1")), (int) num(raw.get("x2")), (int) num(raw.get("z2")));
    }

    private static double num(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }

    public String render() {
        StringBuilder out = new StringBuilder();
        if (spawn == null) {
            out.append("spawn: {}\n");
        } else {
            out.append("spawn: {x: ").append(StructureFile.number(spawn.x())).append(", y: ").append(StructureFile.number(spawn.y()))
                    .append(", z: ").append(StructureFile.number(spawn.z())).append(", yaw: ").append(StructureFile.angle(spawn.yaw()))
                    .append(", pitch: ").append(StructureFile.angle(spawn.pitch())).append("}\n");
        }
        if (bounds == null) {
            out.append("bounds: {}\n");
        } else {
            out.append("bounds: {x1: ").append(bounds.x1()).append(", z1: ").append(bounds.z1())
                    .append(", x2: ").append(bounds.x2()).append(", z2: ").append(bounds.z2()).append("}\n");
        }
        return out.toString();
    }

    public void write(Path file) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            Path temp = Files.createTempFile(parent, "world", ".tmp");
            Files.writeString(temp, render(), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write " + file, e);
        }
    }
}
