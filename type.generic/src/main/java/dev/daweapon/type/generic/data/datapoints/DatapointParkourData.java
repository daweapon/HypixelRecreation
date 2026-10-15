package dev.daweapon.type.generic.data.datapoints;

import lombok.Getter;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

import java.util.HashMap;
import java.util.Map;

public class DatapointParkourData extends Datapoint<Map<DatapointParkourData.ParkourType, Long>> {
    private static final JacksonSerializer<Map<ParkourType, Long>> serializer = new JacksonSerializer<>((Class) Map.class);

    public DatapointParkourData(String key, Map<ParkourType, Long> value) {
        super(key, value, serializer);
    }

    // Required for deepClone() reflection which passes HashMap.class
    public DatapointParkourData(String key, HashMap<ParkourType, Long> value) {
        super(key, value, serializer);
    }

    public DatapointParkourData(String key) {
        super(key, new HashMap<>(), serializer);
    }

    @Getter
    public enum ParkourType {
        PROTOTYPE_LOBBY,
        MURDER_MYSTERY_LOBBY,
        SKYWARS_LOBBY,
        BED_WARS_LOBBY,
        MAIN_LOBBY,
    }
}
