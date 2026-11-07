package dev.daweapon.dungeons.catacombs.puzzle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

final class PuzzleCatalogueFixture {
    private static final Path FILE = Path.of("..", "configuration", "skyblock", "dungeons", "puzzles.json");
    private static PuzzleCatalogue cached;

    private PuzzleCatalogueFixture() {
    }

    static synchronized PuzzleCatalogue load() {
        if (cached == null) {
            try {
                cached = PuzzleCatalogue.load(FILE);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
        return cached;
    }
}
