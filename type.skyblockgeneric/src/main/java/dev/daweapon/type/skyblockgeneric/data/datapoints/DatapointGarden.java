package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;

public class DatapointGarden extends SkyBlockDatapoint<GardenProfile> {
    private static final Serializer<GardenProfile> serializer = new Serializer<>() {
        @Override
        public String serialize(GardenProfile value) {
            return value.serialize();
        }

        @Override
        public GardenProfile deserialize(String json) {
            return GardenProfile.deserialize(json);
        }

        @Override
        public GardenProfile clone(GardenProfile value) {
            return value.copy();
        }
    };

    public DatapointGarden(String key, GardenProfile value) {
        super(key, value, serializer);
    }

    public DatapointGarden(String key) {
        this(key, new GardenProfile());
    }
}
