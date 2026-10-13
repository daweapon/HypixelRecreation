package net.swofty.type.crystalhollows.layout;

import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class NucleusStatueFile {
    private NucleusStatueFile() {
    }

    @SuppressWarnings("unchecked")
    public static Map<NucleusCrystal, int[]> read(Path file) {
        Map<NucleusCrystal, int[]> statues = new EnumMap<>(NucleusCrystal.class);
        if (!Files.exists(file)) return statues;
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            if (root == null || root.get("statues") == null) return statues;
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.get("statues")) {
                statues.put(NucleusCrystal.valueOf((String) entry.get("crystal")),
                        new int[]{((Number) entry.get("x")).intValue(), ((Number) entry.get("y")).intValue(), ((Number) entry.get("z")).intValue()});
            }
            return statues;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }

    public static void set(Path file, NucleusCrystal crystal, int x, int y, int z) {
        Map<NucleusCrystal, int[]> statues = read(file);
        statues.put(crystal, new int[]{x, y, z});
        write(file, statues);
    }

    static String render(Map<NucleusCrystal, int[]> statues) {
        if (statues.isEmpty()) return "statues: []\n";
        StringBuilder out = new StringBuilder("statues:\n");
        statues.forEach((crystal, at) -> out.append("  - {crystal: ").append(crystal.name()).append(", x: ").append(at[0])
                .append(", y: ").append(at[1]).append(", z: ").append(at[2]).append("}\n"));
        return out.toString();
    }

    private static void write(Path file, Map<NucleusCrystal, int[]> statues) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            Path temp = Files.createTempFile(parent, "nucleus", ".tmp");
            Files.writeString(temp, render(statues), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write " + file, e);
        }
    }
}
