package dev.daweapon.type.crystalhollows.layout;

import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class SchematicFiles {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/crystal_hollows/schematics");

    public enum Role {
        SCHEMATIC, EMPTY_PATCH
    }

    public record Reference(String structure, Role role, String file) {
    }

    public record Probe(Path path, boolean exists, Schematic schematic, String error) {
        public boolean readable() {
            return schematic != null;
        }
    }

    private SchematicFiles() {
    }

    public static Path resolve(Path directory, String name) {
        String file = name.contains(".") ? name : name + ".schem";
        Path base = directory.toAbsolutePath().normalize();
        Path resolved = base.resolve(file).normalize();
        return resolved.startsWith(base) && !resolved.equals(base) ? resolved : null;
    }

    public static Probe probe(Path directory, String name) {
        Path path = resolve(directory, name);
        if (path == null) return new Probe(null, false, null, "invalid file name");
        if (!Files.isRegularFile(path)) return new Probe(path, false, null, "file not found");
        try {
            return new Probe(path, true, SchematicReader.read(path), null);
        } catch (IOException | RuntimeException e) {
            return new Probe(path, true, null, String.valueOf(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    public static List<Reference> references(List<StructureSpec> specs) {
        List<Reference> result = new ArrayList<>();
        for (StructureSpec spec : specs) {
            if (spec.schematic() != null) result.add(new Reference(spec.id(), Role.SCHEMATIC, spec.schematic()));
            if (spec.emptyPatch() != null) result.add(new Reference(spec.id(), Role.EMPTY_PATCH, spec.emptyPatch()));
        }
        return result;
    }

    public static List<String> listFiles(Path directory) {
        if (!Files.isDirectory(directory)) return List.of();
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".schem"))
                    .map(path -> path.getFileName().toString()).sorted().toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public static String canonical(String name) {
        return name.contains(".") ? name : name + ".schem";
    }
}
