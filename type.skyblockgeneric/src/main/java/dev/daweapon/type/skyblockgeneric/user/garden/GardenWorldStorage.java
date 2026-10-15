package dev.daweapon.type.skyblockgeneric.user.garden;

import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.type.skyblockgeneric.data.monogdb.GardenDatabase;
import org.bson.types.Binary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public final class GardenWorldStorage {
    public static final int CURRENT_VERSION = 1;

    private static byte[] templateBytes;
    private static PolarWorld pristineTemplate;

    private GardenWorldStorage() {
    }

    public static LoadedGardenWorld load(GardenDatabase database) {
        if (!database.exists() || !database.has("data")) {
            return new LoadedGardenWorld(templateWorld(), CURRENT_VERSION, 0, true);
        }
        int version = database.has("version") ? (int) database.get("version", Integer.class) : CURRENT_VERSION;
        long lastSaved = database.has("lastSaved") ? (long) database.get("lastSaved", Long.class) : 0;
        PolarWorld stored = PolarReader.read(((Binary) database.get("data", Binary.class)).getData());
        return new LoadedGardenWorld(GardenWorldDelta.overlay(templateWorld(), stored), version, lastSaved, false);
    }

    public static void save(GardenDatabase database, PolarWorld world, int version) {
        PolarWorld delta = GardenWorldDelta.changedChunks(pristineTemplate(), world);
        database.insertOrUpdate("data", new Binary(PolarWriter.write(delta)));
        database.insertOrUpdate("lastSaved", System.currentTimeMillis());
        database.insertOrUpdate("version", version);
    }

    private static synchronized PolarWorld templateWorld() {
        return PolarReader.read(readTemplate());
    }

    private static synchronized PolarWorld pristineTemplate() {
        if (pristineTemplate == null) {
            pristineTemplate = PolarReader.read(readTemplate());
        }
        return pristineTemplate;
    }

    private static byte[] readTemplate() {
        if (templateBytes == null) {
            try {
                templateBytes = Files.readAllBytes(CustomWorlds.SKYBLOCK_GARDEN_TEMPLATE.getPath());
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read the Garden template", e);
            }
        }
        return templateBytes;
    }

    public record LoadedGardenWorld(PolarWorld world, int version, long lastSaved, boolean firstCreated) {
    }
}
