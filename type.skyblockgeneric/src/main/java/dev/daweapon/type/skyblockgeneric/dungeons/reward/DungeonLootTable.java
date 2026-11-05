package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Every floor's reward chest loot, read once from {@code configuration/skyblock/dungeons/chest_loot.json}. */
public record DungeonLootTable(Map<String, Map<DungeonChestTier, List<DungeonLootEntry>>> floors) {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/chest_loot.json");
    private static volatile DungeonLootTable loaded;

    public static DungeonLootTable get() {
        DungeonLootTable current = loaded;
        if (current != null) return current;
        synchronized (DungeonLootTable.class) {
            if (loaded == null) loaded = load(FILE);
            return loaded;
        }
    }

    public static DungeonLootTable load(Path file) {
        try {
            return parse(new JSONObject(Files.readString(file)));
        } catch (IOException exception) {
            throw new UncheckedIOException("Couldn't read " + file, exception);
        }
    }

    public static DungeonLootTable parse(JSONObject root) {
        JSONObject floorsJson = root.getJSONObject("floors");
        Map<String, Map<DungeonChestTier, List<DungeonLootEntry>>> floors = new HashMap<>();
        for (String floorKey : floorsJson.keySet()) {
            JSONObject chests = floorsJson.getJSONObject(floorKey);
            Map<DungeonChestTier, List<DungeonLootEntry>> byTier = new EnumMap<>(DungeonChestTier.class);
            for (String tierName : chests.keySet()) {
                JSONArray entries = chests.getJSONObject(tierName).getJSONArray("entries");
                List<DungeonLootEntry> parsed = new ArrayList<>();
                for (int i = 0; i < entries.length(); i++) parsed.add(DungeonLootEntry.parse(entries.getJSONObject(i)));
                byTier.put(DungeonChestTier.valueOf(tierName), List.copyOf(parsed));
            }
            floors.put(floorKey, Map.copyOf(byTier));
        }
        return new DungeonLootTable(Map.copyOf(floors));
    }

    public static String floorKey(CatacombsFloor floor, CatacombsMode mode) {
        return (mode == CatacombsMode.MASTER ? "M" : "F") + floor.ordinal();
    }

    public List<DungeonLootEntry> entries(CatacombsFloor floor, CatacombsMode mode, DungeonChestTier tier) {
        Map<DungeonChestTier, List<DungeonLootEntry>> chests = floors.get(floorKey(floor, mode));
        if (chests == null) return List.of();
        return chests.getOrDefault(tier, List.of());
    }

    public boolean has(CatacombsFloor floor, CatacombsMode mode) {
        return floors.containsKey(floorKey(floor, mode));
    }
}
