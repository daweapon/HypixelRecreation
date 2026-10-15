package dev.daweapon.dungeons.catacombs.layout;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class RoomTemplateLibrary {
    private final Map<String, RoomTemplate> templatesById;

    private RoomTemplateLibrary(Map<String, RoomTemplate> templatesById) {
        this.templatesById = templatesById;
    }

    public static RoomTemplateLibrary of(Collection<RoomTemplate> templates) {
        Map<String, RoomTemplate> byId = new LinkedHashMap<>();
        templates.stream().sorted(Comparator.comparing(RoomTemplate::id)).forEach(template -> {
            if (byId.putIfAbsent(template.id(), template) != null) {
                throw new RoomTemplateException("Room template '" + template.id() + "' is defined more than once");
            }
        });
        return new RoomTemplateLibrary(byId);
    }

    public static RoomTemplateLibrary fromJson(String source, String json) {
        return of(RoomTemplateParser.parse(source, json));
    }

    public static RoomTemplateLibrary fromDirectory(Path directory) {
        if (!Files.isDirectory(directory)) {
            throw new RoomTemplateException("Room template directory " + directory + " does not exist");
        }
        List<RoomTemplate> templates = new ArrayList<>();
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                templates.addAll(RoomTemplateParser.parse(file.toString(), Files.readString(file, StandardCharsets.UTF_8)));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read room templates from " + directory, exception);
        }
        return of(templates);
    }

    public static RoomTemplateLibrary fromResource(String resourcePath) {
        try (InputStream stream = RoomTemplateLibrary.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new RoomTemplateException("Room template resource " + resourcePath + " was not found on the classpath");
            }
            return fromJson(resourcePath, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read room template resource " + resourcePath, exception);
        }
    }

    public List<RoomTemplate> all() {
        return List.copyOf(templatesById.values());
    }

    public Optional<RoomTemplate> find(String id) {
        return Optional.ofNullable(templatesById.get(id));
    }

    public List<RoomTemplate> templates(RoomKind kind) {
        return templatesById.values().stream().filter(template -> template.kind() == kind).toList();
    }

    public List<RoomTemplate> templates(RoomKind kind, RoomShape shape) {
        return templatesById.values().stream()
                .filter(template -> template.kind() == kind && template.shape() == shape)
                .toList();
    }

    public boolean has(RoomKind kind, RoomShape shape) {
        return !templates(kind, shape).isEmpty();
    }

    public int size() {
        return templatesById.size();
    }
}
