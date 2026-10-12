package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DatapointTreeGifts extends SkyBlockDatapoint<Map<String, Integer>> {
    private static final Serializer<Map<String, Integer>> SERIALIZER = new Serializer<>() {
        @Override
        public String serialize(Map<String, Integer> value) {
            return new JSONObject(value).toString();
        }

        @Override
        public Map<String, Integer> deserialize(String json) {
            Map<String, Integer> counts = new HashMap<>();
            if (json == null || json.isBlank()) return counts;
            JSONObject object = new JSONObject(json);
            object.keySet().forEach(key -> counts.put(key, object.optInt(key)));
            return counts;
        }

        @Override
        public Map<String, Integer> clone(Map<String, Integer> value) {
            return new HashMap<>(value);
        }
    };

    public DatapointTreeGifts(String key, Map<String, Integer> value) {
        super(key, value, SERIALIZER);
    }

    public DatapointTreeGifts(String key) {
        this(key, new HashMap<>());
    }

    public int count(String treeType) {
        return getValue().getOrDefault(treeType, 0);
    }

    public int increment(String treeType) {
        Map<String, Integer> counts = getValue();
        int updated = counts.merge(treeType, 1, Integer::sum);
        setValue(counts);
        return updated;
    }
}
