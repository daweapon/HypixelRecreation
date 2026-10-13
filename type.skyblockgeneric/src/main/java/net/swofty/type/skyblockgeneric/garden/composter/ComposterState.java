package net.swofty.type.skyblockgeneric.garden.composter;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Setter
public final class ComposterState {
    private double organicMatter;
    private double fuel;
    private double remainingSeconds;
    private long lastUpdate;
    private int storedCompost;
    private boolean firstCycleDone;
    private boolean initialised;
    private final int[] tiers = new int[ComposterUpgrade.values().length];

    public int tier(ComposterUpgrade upgrade) {
        return tiers[upgrade.ordinal()];
    }

    public void setTier(ComposterUpgrade upgrade, int tier) {
        tiers[upgrade.ordinal()] = Math.max(0, Math.min(ComposterUpgrade.MAX_TIER, tier));
    }

    public boolean running() {
        return remainingSeconds > 0;
    }

    public JSONObject toJson() {
        return new JSONObject().put("matter", organicMatter).put("fuel", fuel).put("remaining", remainingSeconds)
                .put("lastUpdate", lastUpdate).put("stored", storedCompost).put("firstCycleDone", firstCycleDone)
                .put("initialised", initialised).put("tiers", new JSONArray(tiers));
    }

    public static ComposterState fromJson(JSONObject json) {
        ComposterState state = new ComposterState();
        if (json == null) return state;
        state.organicMatter = json.optDouble("matter", 0);
        state.fuel = json.optDouble("fuel", 0);
        state.remainingSeconds = json.optDouble("remaining", 0);
        state.lastUpdate = json.optLong("lastUpdate", 0);
        state.storedCompost = json.optInt("stored", 0);
        state.firstCycleDone = json.optBoolean("firstCycleDone", false);
        state.initialised = json.optBoolean("initialised", false);
        JSONArray tiers = json.optJSONArray("tiers");
        if (tiers != null) {
            for (int i = 0; i < Math.min(tiers.length(), state.tiers.length); i++) {
                state.tiers[i] = tiers.getInt(i);
            }
        }
        return state;
    }
}
