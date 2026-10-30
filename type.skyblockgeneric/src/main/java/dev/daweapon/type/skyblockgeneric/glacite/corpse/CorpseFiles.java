package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

final class CorpseFiles {
    private static final String DIRECTORY = "configuration/skyblock/frozen_corpses/";

    private CorpseFiles() {
    }

    static Map<String, Object> read(String name) {
        Path path = Files.exists(Path.of("./" + DIRECTORY + name)) ? Path.of("./" + DIRECTORY + name) : Path.of("../" + DIRECTORY + name);
        try (InputStream stream = Files.newInputStream(path)) {
            return new Yaml().load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path.toAbsolutePath(), exception);
        }
    }
}
