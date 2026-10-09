package net.swofty.type.generic.data.datapoints;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.generic.data.Datapoint;

import java.util.HashMap;
import java.util.Map;

public class DatapointMapStringLong extends Datapoint<Map<String, Long>> {
    private static final JacksonSerializer<Map<String, Object>> json =
            new JacksonSerializer<>((Class) Map.class);

    public static final Serializer<Map<String, Long>> serializer = new Serializer<>() {
        @Override
        public String serialize(Map<String, Long> value) {
            return json.serialize((Map) value);
        }

        @Override
        public Map<String, Long> deserialize(String raw) {
            Map<String, Object> parsed = json.deserialize(raw);
            if (parsed == null) return null;
            Map<String, Long> values = new HashMap<>();
            parsed.forEach((key, value) -> {
                if (value instanceof Number number) values.put(key, number.longValue());
                else if (value != null) values.put(key, Long.parseLong(value.toString()));
            });
            return values;
        }

        @Override
        public Map<String, Long> clone(Map<String, Long> value) {
            return deserialize(serialize(value));
        }
    };

    public DatapointMapStringLong(String key, Map<String, Long> value) {
        super(key, value, serializer);
    }

    public DatapointMapStringLong(String key, HashMap<String, Long> value) {
        super(key, value, serializer);
    }

    public DatapointMapStringLong(String key) {
        super(key, new HashMap<>(), serializer);
    }
}
