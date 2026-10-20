package dev.daweapon.type.skyblockgeneric.romero;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class RomeroDialogue {
    public static final Path FILE = Path.of("./configuration/skyblock/romero_juliette/dialogue.yml");
    private static volatile RomeroDialogue loaded;

    private final Map<String, Map<String, List<String>>> speakers;

    private RomeroDialogue(Map<String, Map<String, List<String>>> speakers) {
        this.speakers = speakers;
    }

    public static RomeroDialogue get() {
        RomeroDialogue current = loaded;
        if (current != null) return current;
        synchronized (RomeroDialogue.class) {
            if (loaded == null) loaded = load(FILE);
            return loaded;
        }
    }

    public static RomeroDialogue load(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<String, Map<String, List<String>>> root = new Yaml().load(stream);
            return new RomeroDialogue(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + file, exception);
        }
    }

    public Map<String, List<String>> romero() {
        return speakers.get("romero");
    }

    public Map<String, List<String>> juliette() {
        return speakers.get("juliette");
    }
}
