package net.swofty.type.skyblockgeneric.essence;

import net.swofty.commons.skyblock.item.ItemType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Essence star upgrade costs for every item, imported from NEU's essencecosts.json, and the
 * salvage table. Both files live in configuration/skyblock.
 */
public final class EssenceUpgrades {
    private static final Path UPGRADES_FILE = Path.of("./configuration/skyblock/essence_upgrades.json");
    private static final Path SALVAGE_FILE = Path.of("./configuration/skyblock/essence_salvage.json");
    private static Map<String, Upgrade> upgrades;
    private static Map<String, Salvage> salvage;

    public record Star(long essence, long coins, Map<String, Integer> items) {
        public boolean hasUnknownItems() {
            return items.keySet().stream().anyMatch(id -> ItemType.get(id) == null);
        }
    }

    public record Upgrade(EssenceType essence, List<Star> stars) {
        public int maxStars() {
            return stars.size();
        }

        public Star star(int level) {
            return stars.get(level - 1);
        }
    }

    public record Salvage(EssenceType essence, long amount, Map<String, Integer> items) {
    }

    private EssenceUpgrades() {
    }

    public static Optional<Upgrade> upgradeFor(ItemType type) {
        return type == null ? Optional.empty() : Optional.ofNullable(upgrades().get(type.name()));
    }

    public static Optional<Salvage> salvageFor(ItemType type) {
        return type == null ? Optional.empty() : Optional.ofNullable(salvage().get(type.name()));
    }

    public static Map<String, Upgrade> upgrades() {
        if (upgrades == null) upgrades = loadUpgrades(UPGRADES_FILE);
        return upgrades;
    }

    public static Map<String, Salvage> salvage() {
        if (salvage == null) salvage = loadSalvage(SALVAGE_FILE);
        return salvage;
    }

    static Map<String, Upgrade> loadUpgrades(Path path) {
        JSONObject root = read(path);
        if (root == null) return Collections.emptyMap();
        Map<String, Upgrade> result = new HashMap<>();
        JSONObject all = root.getJSONObject("upgrades");
        for (String id : all.keySet()) {
            JSONObject entry = all.getJSONObject(id);
            EssenceType type = EssenceType.valueOf(entry.getString("type"));
            JSONArray stars = entry.getJSONArray("stars");
            List<Star> parsed = new java.util.ArrayList<>();
            for (int i = 0; i < stars.length(); i++) {
                JSONObject star = stars.getJSONObject(i);
                parsed.add(new Star(star.getLong("essence"), star.optLong("coins", 0), items(star.optJSONObject("items"))));
            }
            result.put(id, new Upgrade(type, List.copyOf(parsed)));
        }
        return result;
    }

    static Map<String, Salvage> loadSalvage(Path path) {
        JSONObject root = read(path);
        if (root == null) return Collections.emptyMap();
        Map<String, Salvage> result = new HashMap<>();
        JSONObject all = root.getJSONObject("salvage");
        for (String id : all.keySet()) {
            JSONObject entry = all.getJSONObject(id);
            result.put(id, new Salvage(EssenceType.valueOf(entry.getString("type")), entry.getLong("essence"),
                    items(entry.optJSONObject("items"))));
        }
        return result;
    }

    private static Map<String, Integer> items(JSONObject object) {
        if (object == null) return Map.of();
        Map<String, Integer> items = new LinkedHashMap<>();
        for (String id : object.keySet()) items.put(id, object.getInt(id));
        return Collections.unmodifiableMap(items);
    }

    private static JSONObject read(Path path) {
        try {
            return new JSONObject(Files.readString(path));
        } catch (Exception e) {
            Logger.error(e, "Failed to load essence data from {}", path);
            return null;
        }
    }
}
