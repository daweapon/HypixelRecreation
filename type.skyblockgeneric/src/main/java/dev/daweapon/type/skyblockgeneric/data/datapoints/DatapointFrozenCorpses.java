package dev.daweapon.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatapointFrozenCorpses extends SkyBlockDatapoint<DatapointFrozenCorpses.FrozenCorpseData> {
    private static final Serializer<FrozenCorpseData> serializer = new Serializer<>() {
        @Override
        public String serialize(FrozenCorpseData value) {
            JSONObject json = new JSONObject();
            json.put("looted", new JSONObject(value.looted));
            json.put("rngXp", value.rngXp);
            json.put("rngSelected", value.rngSelected == null ? "" : value.rngSelected);
            json.put("milestonesClaimed", value.milestonesClaimed);
            json.put("metWolf", value.metWolf);
            return json.toString();
        }

        @Override
        public FrozenCorpseData deserialize(String json) {
            FrozenCorpseData data = new FrozenCorpseData();
            if (json == null || json.isEmpty()) return data;
            try {
                JSONObject obj = new JSONObject(json);
                JSONObject looted = obj.optJSONObject("looted");
                if (looted != null) looted.keySet().forEach(key -> data.looted.put(key, looted.getInt(key)));
                data.rngXp = obj.optLong("rngXp", 0L);
                data.rngSelected = obj.optString("rngSelected", "").isEmpty() ? null : obj.getString("rngSelected");
                data.milestonesClaimed = obj.optInt("milestonesClaimed", 0);
                data.metWolf = obj.optBoolean("metWolf", false);
            } catch (Exception ignored) {
            }
            return data;
        }

        @Override
        public FrozenCorpseData clone(FrozenCorpseData value) {
            FrozenCorpseData cloned = new FrozenCorpseData();
            cloned.looted.putAll(value.looted);
            cloned.rngXp = value.rngXp;
            cloned.rngSelected = value.rngSelected;
            cloned.milestonesClaimed = value.milestonesClaimed;
            cloned.metWolf = value.metWolf;
            return cloned;
        }
    };

    public DatapointFrozenCorpses(String key, FrozenCorpseData value) {
        super(key, value, serializer);
    }

    public DatapointFrozenCorpses(String key) {
        super(key, new FrozenCorpseData(), serializer);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FrozenCorpseData {
        private final Map<String, Integer> looted = new LinkedHashMap<>();
        private long rngXp;
        private String rngSelected;
        private int milestonesClaimed;
        private boolean metWolf;

        public int lootedCount(String type) {
            return looted.getOrDefault(type, 0);
        }

        public void addLooted(String type) {
            looted.merge(type, 1, Integer::sum);
        }
    }
}
