package dev.daweapon.type.skyblockgeneric.garden.chip;

import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;

public final class ChipData {
    private final Map<GardenChip, Integer> consumed = new EnumMap<>(GardenChip.class);
    private final Map<GardenChip, Integer> levels = new EnumMap<>(GardenChip.class);
    private int jeffStage;

    public int jeffStage() {
        return jeffStage;
    }

    public void setJeffStage(int stage) {
        jeffStage = stage;
    }

    public int consumed(GardenChip chip) {
        return consumed.getOrDefault(chip, 0);
    }

    public int level(GardenChip chip) {
        return levels.getOrDefault(chip, 0);
    }

    public void setConsumed(GardenChip chip, int amount) {
        consumed.put(chip, amount);
    }

    public void setLevel(GardenChip chip, int level) {
        levels.put(chip, level);
    }

    public int rarity(GardenChip chip) {
        return GardenChip.rarityIndex(consumed(chip));
    }

    public boolean unlocked(GardenChip chip) {
        return rarity(chip) >= 0;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (GardenChip chip : GardenChip.values()) {
            if (consumed(chip) > 0 || level(chip) > 0) {
                json.put(chip.name(), new JSONObject().put("consumed", consumed(chip)).put("level", level(chip)));
            }
        }
        if (jeffStage > 0) json.put("jeffStage", jeffStage);
        return json;
    }

    public static ChipData fromJson(JSONObject json) {
        ChipData data = new ChipData();
        if (json == null) return data;
        data.jeffStage = json.optInt("jeffStage", 0);
        for (String key : json.keySet()) {
            if (key.equals("jeffStage")) continue;
            try {
                GardenChip chip = GardenChip.valueOf(key);
                JSONObject entry = json.getJSONObject(key);
                data.consumed.put(chip, entry.optInt("consumed", 0));
                data.levels.put(chip, entry.optInt("level", 0));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }
}
