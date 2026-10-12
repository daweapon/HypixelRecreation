package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.commons.YamlFileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class ReforgeAbilityCatalogue {
    public static final Path DEFAULT_PATH = Path.of("./configuration/skyblock/reforge_abilities.yml");

    public record Entry(String reforgeId, String name, String text, @Nullable String unavailable) {
    }

    private ReforgeAbilityCatalogue() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Entry> load(Path path) {
        Map<String, Entry> entries = new LinkedHashMap<>();
        if (!path.toFile().exists()) return entries;
        try {
            Map<String, Object> raw = YamlFileUtils.loadYaml(path.toFile());
            if (raw == null) return entries;
            raw.forEach((id, value) -> {
                Map<String, Object> fields = (Map<String, Object>) value;
                entries.put(id, new Entry(id, (String) fields.get("name"), (String) fields.get("text"),
                        (String) fields.get("unavailable")));
            });
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        return entries;
    }
}
