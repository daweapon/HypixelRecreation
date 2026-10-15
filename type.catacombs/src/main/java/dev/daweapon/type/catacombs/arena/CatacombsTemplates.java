package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;

import java.nio.file.Path;

/** The room templates under {@code configuration/skyblock/dungeons/rooms}, read once on first use. */
public final class CatacombsTemplates {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/dungeons/rooms");

    private static volatile RoomTemplateLibrary library;

    private CatacombsTemplates() {
    }

    public static RoomTemplateLibrary get() {
        RoomTemplateLibrary loaded = library;
        if (loaded != null) return loaded;
        synchronized (CatacombsTemplates.class) {
            if (library == null) library = RoomTemplateLibrary.fromDirectory(DIRECTORY);
            return library;
        }
    }
}
