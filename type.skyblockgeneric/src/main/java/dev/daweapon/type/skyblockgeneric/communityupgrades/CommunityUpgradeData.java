package dev.daweapon.type.skyblockgeneric.communityupgrades;

import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;

public final class CommunityUpgradeData {
    public record Active(CommunityUpgradeId id, int targetTier, long startedAt, long completesAt) {
    }

    private final Map<CommunityUpgradeId, Integer> tiers = new EnumMap<>(CommunityUpgradeId.class);
    private Active active;
    private final Map<String, Long> allowanceDays = new java.util.HashMap<>();

    public int tier(CommunityUpgradeId id) {
        return tiers.getOrDefault(id, 0);
    }

    public void setTier(CommunityUpgradeId id, int tier) {
        tiers.put(id, tier);
    }

    public long allowanceDay(String profileId) {
        return allowanceDays.getOrDefault(profileId, -1L);
    }

    public void setAllowanceDay(String profileId, long day) {
        allowanceDays.put(profileId, day);
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
        json.put("allowance", new JSONObject(allowanceDays));
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
        JSONObject allowanceJson = json.optJSONObject("allowance");
        if (allowanceJson != null) {
            for (String key : allowanceJson.keySet()) data.allowanceDays.put(key, allowanceJson.getLong(key));
        }
        JSONObject activeJson = json.optJSONObject("active");
        if (activeJson != null) {
            data.active = new Active(CommunityUpgradeId.valueOf(activeJson.getString("id")), activeJson.getInt("tier"),
                    activeJson.getLong("startedAt"), activeJson.getLong("completesAt"));
        }
        return data;
    }
}
