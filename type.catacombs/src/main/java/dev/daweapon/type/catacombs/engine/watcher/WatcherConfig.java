package dev.daweapon.type.catacombs.engine.watcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.type.catacombs.engine.look.Skin;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** The captured Floor I Watcher, read once from {@code configuration/skyblock/dungeons/watcher.json}. */
public record WatcherConfig(Skin head, String name, String bossBar, String introLine, List<String> spawnLines,
                            List<String> killLines, String enoughLine, String provenLine, int introTicks, int bossBarTicks,
                            int firstWave, List<Integer> firstWaveLineTicks, int lineToSpawnTicks, int laterGapMinTicks,
                            int laterGapMaxTicks, int enoughAfterLastSpawnTicks, int provenAfterClearTicks,
                            int portalAfterProvenTicks, List<Blessing> blessings, Skin blessingHead) {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/watcher.json");
    private static volatile WatcherConfig shared;

    public record Blessing(BlessingType type, int level) {
    }

    public static WatcherConfig shared() {
        WatcherConfig loaded = shared;
        if (loaded != null) return loaded;
        synchronized (WatcherConfig.class) {
            if (shared == null) shared = load(FILE);
            return shared;
        }
    }

    public static WatcherConfig load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException exception) {
            Logger.error(exception, "Could not read the Watcher from {}", file);
            throw new IllegalStateException("The Watcher's configuration is missing", exception);
        }
    }

    public static WatcherConfig parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject gap = root.getAsJsonObject("laterGapTicks");
        List<Blessing> blessings = new ArrayList<>();
        root.getAsJsonArray("blessings").forEach(entry -> blessings.add(new Blessing(
                BlessingType.valueOf(entry.getAsJsonObject().get("type").getAsString()), entry.getAsJsonObject().get("level").getAsInt())));
        List<Integer> waveTicks = new ArrayList<>();
        root.getAsJsonArray("firstWaveLineTicks").forEach(tick -> waveTicks.add(tick.getAsInt()));
        return new WatcherConfig(Skin.parse(root.getAsJsonObject("head")), root.get("name").getAsString(),
                root.get("bossBar").getAsString(), root.get("introLine").getAsString(), strings(root.getAsJsonArray("spawnLines")),
                strings(root.getAsJsonArray("killLines")), root.get("enoughLine").getAsString(), root.get("provenLine").getAsString(),
                root.get("introTicks").getAsInt(), root.get("bossBarTicks").getAsInt(), root.get("firstWave").getAsInt(),
                List.copyOf(waveTicks), root.get("lineToSpawnTicks").getAsInt(), gap.get("min").getAsInt(), gap.get("max").getAsInt(),
                root.get("enoughAfterLastSpawnTicks").getAsInt(), root.get("provenAfterClearTicks").getAsInt(),
                root.get("portalAfterProvenTicks").getAsInt(), List.copyOf(blessings), Skin.parse(root.getAsJsonObject("blessingHead")));
    }

    private static List<String> strings(JsonArray array) {
        List<String> out = new ArrayList<>();
        array.forEach(entry -> out.add(entry.getAsString()));
        return List.copyOf(out);
    }
}
