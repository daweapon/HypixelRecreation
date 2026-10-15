package net.swofty.type.skyblockgeneric.island;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.user.island.SkyBlockIsland;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IslandUtilityBlocks {
    public static final String STORAGE_KEY = "utility_blocks";
    public static final double MITHRIL_CRYSTAL_MINION_SPEED_PERCENT = 10;

    private static final Map<UUID, Map<String, String>> PLACED = new ConcurrentHashMap<>();

    private IslandUtilityBlocks() {
    }

    public static boolean isUtility(ItemType type) {
        return type == ItemType.MITHRIL_CRYSTAL || type == ItemType.REPELLING_CANDLE || type == ItemType.SILENCE_BLOCK
                || type == ItemType.EXPERIMENTATION_TABLE;
    }

    public static void place(Instance instance, Point position, ItemType type) {
        SkyBlockIsland island = islandOf(instance);
        if (island == null || !isUtility(type)) return;
        Map<String, String> placed = placed(island);
        placed.put(key(position), type.name());
        save(island, placed);
    }

    public static void remove(Instance instance, Point position) {
        SkyBlockIsland island = islandOf(instance);
        if (island == null) return;
        Map<String, String> placed = placed(island);
        if (placed.remove(key(position)) != null) save(island, placed);
    }

    public static boolean has(Instance instance, ItemType type) {
        SkyBlockIsland island = islandOf(instance);
        return island != null && placed(island).containsValue(type.name());
    }

    public static List<Point> positions(Instance instance, ItemType type) {
        SkyBlockIsland island = islandOf(instance);
        List<Point> positions = new ArrayList<>();
        if (island == null) return positions;
        placed(island).forEach((key, value) -> {
            if (!value.equals(type.name())) return;
            String[] parts = key.split(",");
            positions.add(new net.minestom.server.coordinate.Vec(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        });
        return positions;
    }

    public static double minionSpeedPercent(Instance instance, boolean miningMinion) {
        return miningMinion && has(instance, ItemType.MITHRIL_CRYSTAL) ? MITHRIL_CRYSTAL_MINION_SPEED_PERCENT : 0;
    }

    public static String key(Point position) {
        return position.blockX() + "," + position.blockY() + "," + position.blockZ();
    }

    public static String serialize(Map<String, String> placed) {
        List<String> entries = new ArrayList<>();
        placed.forEach((position, type) -> entries.add(position + "," + type));
        return String.join(";", entries);
    }

    public static Map<String, String> parse(String raw) {
        Map<String, String> placed = new HashMap<>();
        if (raw == null || raw.isBlank()) return placed;
        for (String entry : raw.split(";")) {
            String[] parts = entry.split(",");
            if (parts.length != 4) continue;
            placed.put(parts[0] + "," + parts[1] + "," + parts[2], parts[3]);
        }
        return placed;
    }

    private static SkyBlockIsland islandOf(Instance instance) {
        if (instance == null) return null;
        return SkyBlockIsland.loaded().stream()
                .filter(island -> island.getIslandInstance() == instance)
                .findFirst()
                .orElse(null);
    }

    private static Map<String, String> placed(SkyBlockIsland island) {
        return PLACED.computeIfAbsent(island.getIslandID(),
                id -> new ConcurrentHashMap<>(parse(String.valueOf(island.getDatabase().get(STORAGE_KEY, "")))));
    }

    private static void save(SkyBlockIsland island, Map<String, String> placed) {
        island.getDatabase().insertOrUpdate(STORAGE_KEY, serialize(placed));
    }
}
