package dev.daweapon.dungeons.catacombs.layout;

import java.nio.file.Files;
import java.nio.file.Path;

final class PlaceholderLibrary {
    private static final String RELATIVE_DIRECTORY = "configuration/skyblock/dungeons/rooms";
    private static RoomTemplateLibrary cached;

    private PlaceholderLibrary() {
    }

    static synchronized RoomTemplateLibrary load() {
        if (cached == null) {
            Path directory = Path.of("").toAbsolutePath();
            while (directory != null && !Files.isDirectory(directory.resolve(RELATIVE_DIRECTORY))) {
                directory = directory.getParent();
            }
            if (directory == null) {
                throw new IllegalStateException("Could not find " + RELATIVE_DIRECTORY + " above the working directory");
            }
            cached = RoomTemplateLibrary.fromDirectory(directory.resolve(RELATIVE_DIRECTORY));
        }
        return cached;
    }
}
