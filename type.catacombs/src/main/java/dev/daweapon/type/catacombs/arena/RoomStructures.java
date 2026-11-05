package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.RoomStructure;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicReader;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The block files of the real rooms, read from the rooms folder once and shared by every run. */
public final class RoomStructures {
    private static final RoomStructures SHARED = new RoomStructures(CatacombsTemplates.DIRECTORY);

    private final Path directory;
    private final Map<String, Schematic> loaded = new ConcurrentHashMap<>();

    public RoomStructures(Path directory) {
        this.directory = directory;
    }

    public static RoomStructures shared() {
        return SHARED;
    }

    public int preload(RoomTemplateLibrary library) {
        int count = 0;
        for (RoomTemplate template : library.all()) {
            if (template.blocks().isEmpty()) continue;
            try {
                load(template.blocks().get());
                count++;
            } catch (RuntimeException e) {
                Logger.warn(e, "Room {} has no usable blocks and will be a placeholder", template.id());
            }
        }
        return count;
    }

    public Schematic load(RoomStructure structure) {
        return loaded.computeIfAbsent(structure.file(), file -> {
            try {
                return SchematicReader.read(directory.resolve(file));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read room structure " + file, e);
            }
        });
    }
}
