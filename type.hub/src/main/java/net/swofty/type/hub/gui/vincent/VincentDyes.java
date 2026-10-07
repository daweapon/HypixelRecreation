package net.swofty.type.hub.gui.vincent;

import net.swofty.commons.YamlFileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class VincentDyes {
    public static final int[] MASTERPIECE_MULTIPLIERS = {2, 3, 2};

    private static final File DYES_FILE = new File("./configuration/skyblock/vincent_dyes.yml");
    private static List<VincentDye> dyes;

    private VincentDyes() {
    }

    public static synchronized List<VincentDye> all() {
        if (dyes == null) {
            dyes = load();
        }
        return dyes;
    }

    /**
     * Vincent picks three droppable dyes per SkyBlock year. Seeding by year keeps the pick identical on
     * every server without storing it; the middle pick is the tripled one, as in his menu.
     */
    public static List<VincentDye> masterpiece(int skyBlockYear) {
        List<VincentDye> candidates = new ArrayList<>(all().stream().filter(VincentDye::droppable).toList());
        Collections.shuffle(candidates, new Random(skyBlockYear * 31L + 7));
        return candidates.subList(0, Math.min(MASTERPIECE_MULTIPLIERS.length, candidates.size()));
    }

    @SuppressWarnings("unchecked")
    private static List<VincentDye> load() {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(DYES_FILE);
            List<Map<String, Object>> entries = (List<Map<String, Object>>) root.getOrDefault("dyes", List.of());
            return entries.stream().map(entry -> new VincentDye(
                    String.valueOf(entry.get("id")),
                    String.valueOf(entry.get("name")),
                    String.valueOf(entry.getOrDefault("colour", "f")).charAt(0),
                    entry.get("hex") == null ? null : String.valueOf(entry.get("hex")),
                    String.valueOf(entry.getOrDefault("kind", "HEX")),
                    Boolean.TRUE.equals(entry.get("droppable")),
                    (List<String>) entry.getOrDefault("source", List.of())
            )).toList();
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load Vincent's dyes");
            return List.of();
        }
    }
}
