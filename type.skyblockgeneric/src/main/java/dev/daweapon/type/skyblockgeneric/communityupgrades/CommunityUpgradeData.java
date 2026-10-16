package dev.daweapon.type.skyblockgeneric.communityupgrades;

import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;

public final class CommunityUpgradeData {
    public record Active(CommunityUpgradeId id, int targetTier, long startedAt, long completesAt) {
    }

    private final Map<CommunityUpgradeId, Integer> tiers = new EnumMap<>(CommunityUpgradeId.class);
    private Active active;

    public int tier(CommunityUpgradeId id) {
        return tiers.getOrDefault(id, 0);
    }

    public void setTier(CommunityUpgradeId id, int tier) {
        tiers.put(id, tier);
    }

    public Active active() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        JSONObject tierJson = new JSONObject();
        tiers.forEach((id, tier) -> tierJson.put(id.name(), tier));
        json.put("tiers", tierJson);
        if (active != null) {
            json.put("active", new JSONObject()
                    .put("id", active.id().name())
                    .put("tier", active.targetTier())
                    .put("startedAt", active.startedAt())
                    .put("completesAt", active.completesAt()));
        }
        return json.toString();
    }

    public static CommunityUpgradeData fromJson(String text) {
        CommunityUpgradeData data = new CommunityUpgradeData();
        if (text == null || text.isBlank()) return data;
        JSONObject json = new JSONObject(text);
        JSONObject tierJson = json.optJSONObject("tiers");
        if (tierJson != null) {
            for (CommunityUpgradeId id : CommunityUpgradeId.values()) {
                if (tierJson.has(id.name())) data.tiers.put(id, tierJson.getInt(id.name()));
            }
        }
        JSONObject activeJson = json.optJSONObject("active");
        if (activeJson != null) {
            data.active = new Active(CommunityUpgradeId.valueOf(activeJson.getString("id")), activeJson.getInt("tier"),
                    activeJson.getLong("startedAt"), activeJson.getLong("completesAt"));
        }
        return data;
    }
}
