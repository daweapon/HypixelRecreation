package dev.daweapon.type.skyblockgeneric.garden.tool;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ToolkitData {
    @Getter
    @Setter
    private boolean unlocked;
    private final Map<String, String> stored = new LinkedHashMap<>();

    public @Nullable String stored(String family) {
        return stored.get(family);
    }

    public void store(String family, String serializedTool) {
        stored.put(family, serializedTool);
    }

    public @Nullable String take(String family) {
        return stored.remove(family);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("unlocked", unlocked);
        JSONObject tools = new JSONObject();
        stored.forEach(tools::put);
        json.put("stored", tools);
        return json;
    }

    public static ToolkitData fromJson(@Nullable JSONObject json) {
        ToolkitData data = new ToolkitData();
        if (json == null) return data;
        data.unlocked = json.optBoolean("unlocked", false);
        JSONObject tools = json.optJSONObject("stored");
        if (tools != null) {
            for (String key : tools.keySet()) data.stored.put(key, tools.getString(key));
        }
        return data;
    }
}
