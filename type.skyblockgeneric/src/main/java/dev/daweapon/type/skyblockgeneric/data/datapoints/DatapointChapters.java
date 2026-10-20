package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DatapointChapters extends SkyBlockDatapoint<DatapointChapters.ChaptersData> {
    private static final Serializer<ChaptersData> SERIALIZER = new Serializer<>() {
        @Override
        public String serialize(ChaptersData value) {
            JSONObject triggers = new JSONObject();
            value.triggers.forEach((name, amount) -> triggers.put(name, amount.longValue()));
            return new JSONObject()
                    .put("triggers", triggers)
                    .put("tasks", new JSONArray(value.tasks))
                    .put("chapters", new JSONArray(value.chapters))
                    .toString();
        }

        @Override
        public ChaptersData deserialize(String json) {
            ChaptersData data = new ChaptersData();
            if (json == null || json.isBlank()) return data;
            try {
                JSONObject object = new JSONObject(json);
                JSONObject triggers = object.optJSONObject("triggers");
                if (triggers != null) triggers.keySet().forEach(key -> data.triggers.put(key, triggers.optLong(key)));
                JSONArray tasks = object.optJSONArray("tasks");
                if (tasks != null) tasks.forEach(task -> data.tasks.add(String.valueOf(task)));
                JSONArray chapters = object.optJSONArray("chapters");
                if (chapters != null) chapters.forEach(chapter -> data.chapters.add(((Number) chapter).intValue()));
            } catch (Exception exception) {
                return new ChaptersData();
            }
            return data;
        }

        @Override
        public ChaptersData clone(ChaptersData value) {
            ChaptersData copy = new ChaptersData();
            copy.triggers.putAll(value.triggers);
            copy.tasks.addAll(value.tasks);
            copy.chapters.addAll(value.chapters);
            return copy;
        }
    };

    public DatapointChapters(String key, ChaptersData value) {
        super(key, value, SERIALIZER);
    }

    public DatapointChapters(String key) {
        super(key, new ChaptersData(), SERIALIZER);
    }

    @Getter
    @NoArgsConstructor
    public static class ChaptersData {
        private final Map<String, Long> triggers = new LinkedHashMap<>();
        private final Set<String> tasks = new HashSet<>();
        private final Set<Integer> chapters = new HashSet<>();

        public long trigger(String name) {
            return triggers.getOrDefault(name, 0L);
        }
    }
}
