package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.region.IslandType;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class EggLocationCatalogue {
    private static final File FILE = new File("./configuration/skyblock/hoppity/egg_locations.yml");
    private static volatile EggLocationCatalogue instance;

    private final Map<IslandType, List<EggLocation>> byIsland = new EnumMap<>(IslandType.class);

    private EggLocationCatalogue() {
    }

    public static EggLocationCatalogue get() {
        EggLocationCatalogue current = instance;
        if (current == null) {
            synchronized (EggLocationCatalogue.class) {
                if (instance == null) instance = load(FILE);
                current = instance;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public static EggLocationCatalogue load(File file) {
        EggLocationCatalogue catalogue = new EggLocationCatalogue();
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(file);
            for (Map.Entry<String, Object> entry : root.entrySet()) {
                IslandType island = IslandType.valueOf(entry.getKey());
                List<EggLocation> locations = new ArrayList<>();
                for (Object raw : (List<Object>) entry.getValue()) {
                    Map<String, Object> row = (Map<String, Object>) raw;
                    locations.add(new EggLocation(island, locations.size(), String.valueOf(row.get("zone")),
                            ((Number) row.get("x")).doubleValue(), ((Number) row.get("y")).doubleValue(),
                            ((Number) row.get("z")).doubleValue(), String.valueOf(row.get("message"))));
                }
                catalogue.byIsland.put(island, List.copyOf(locations));
            }
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load egg_locations.yml");
        }
        return catalogue;
    }

    public List<EggLocation> locations(IslandType island) {
        return byIsland.getOrDefault(island, List.of());
    }

    public EggLocation location(IslandType island, int index) {
        List<EggLocation> locations = locations(island);
        return index >= 0 && index < locations.size() ? locations.get(index) : null;
    }

    public boolean hasEggs(IslandType island) {
        return !locations(island).isEmpty();
    }

    public List<IslandType> islands() {
        return List.copyOf(byIsland.keySet());
    }
}
