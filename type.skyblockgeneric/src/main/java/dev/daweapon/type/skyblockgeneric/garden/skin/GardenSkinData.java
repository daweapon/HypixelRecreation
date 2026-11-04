package dev.daweapon.type.skyblockgeneric.garden.skin;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class GardenSkinData {
    private final Set<String> owned = new TreeSet<>();
    private final Map<GardenSkinType, String> selected = new EnumMap<>(GardenSkinType.class);

    public boolean owns(String skinId) {
        return owned.contains(skinId);
    }

    public boolean unlock(String skinId) {
        return owned.add(skinId);
    }

    public Set<String> owned() {
        return owned;
    }

    public String selected(GardenSkinType type) {
        return selected.getOrDefault(type, type.getDefaultId());
    }

    public void select(GardenSkinType type, String skinId) {
        selected.put(type, skinId);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("owned", new JSONArray(owned));
        JSONObject chosen = new JSONObject();
        selected.forEach((type, id) -> chosen.put(type.name(), id));
        json.put("selected", chosen);
        return json;
    }

    public static GardenSkinData fromJson(@Nullable JSONObject json) {
        GardenSkinData data = new GardenSkinData();
        if (json == null) return data;
        JSONArray owned = json.optJSONArray("owned");
        if (owned != null) {
            for (int i = 0; i < owned.length(); i++) data.owned.add(owned.getString(i));
        }
        JSONObject chosen = json.optJSONObject("selected");
        if (chosen != null) {
            for (String key : chosen.keySet()) {
                try {
                    data.selected.put(GardenSkinType.valueOf(key), chosen.getString(key));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return data;
    }
}
