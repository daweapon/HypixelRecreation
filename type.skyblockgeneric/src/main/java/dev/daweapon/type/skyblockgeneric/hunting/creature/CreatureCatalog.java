package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.YamlFileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CreatureCatalog {
    private static final File DEFAULT_DIRECTORY = new File("configuration/skyblock/hunting");
    private static final Map<String, CreatureSpec> CREATURES = new LinkedHashMap<>();
    private static boolean loaded;

    private CreatureCatalog() {
    }

    public static synchronized void load(File directory) {
        CREATURES.clear();
        File[] files = directory.listFiles((dir, name) -> name.endsWith("_creatures.yml"));
        if (files == null) {
            loaded = true;
            return;
        }
        Arrays.sort(files);
        for (File file : files) {
            try {
                for (Object row : creatureRows(file)) {
                    @SuppressWarnings("unchecked")
                    CreatureSpec spec = CreatureSpec.parse((Map<String, Object>) row);
                    if (CREATURES.put(spec.id(), spec) != null) throw new IllegalStateException("Duplicate creature " + spec.id());
                }
            } catch (Exception exception) {
                Logger.error(exception, "Failed to load hunting creatures from {}", file);
                throw new IllegalStateException("Invalid creature file " + file, exception);
            }
        }
        inheritFamilyBestiary();
        loaded = true;
    }

    private static void inheritFamilyBestiary() {
        Map<String, CreatureSpec> mains = new LinkedHashMap<>();
        CREATURES.values().stream().filter(spec -> spec.bestiary().family() != null && spec.hasOwnBestiary())
                .forEach(spec -> mains.put(spec.bestiary().family(), spec));
        for (CreatureSpec spec : List.copyOf(CREATURES.values())) {
            CreatureSpec main = spec.bestiary().family() == null ? null : mains.get(spec.bestiary().family());
            if (main == null || spec.hasOwnBestiary()) continue;
            CREATURES.put(spec.id(), spec.withBestiary(new CreatureSpec.Bestiary(main.bestiary().section(), main.bestiary().bracket(),
                    main.bestiary().maxTier(), main.bestiary().lore(), main.bestiary().family())));
        }
    }

    private static List<Object> creatureRows(File file) throws Exception {
        Map<String, Object> root = YamlFileUtils.loadYaml(file);
        Object rows = root.get("creatures");
        return rows instanceof List<?> list ? new ArrayList<>(list) : List.of();
    }

    private static synchronized void ensureLoaded() {
        if (!loaded) load(DEFAULT_DIRECTORY.isDirectory() ? DEFAULT_DIRECTORY : new File("../configuration/skyblock/hunting"));
    }

    public static List<CreatureSpec> all() {
        ensureLoaded();
        return List.copyOf(CREATURES.values());
    }

    public static Optional<CreatureSpec> byId(String id) {
        ensureLoaded();
        return Optional.ofNullable(CREATURES.get(id));
    }

    public static List<CreatureSpec> forIsland(String island) {
        return all().stream().filter(spec -> spec.island().equals(island)).toList();
    }
}
