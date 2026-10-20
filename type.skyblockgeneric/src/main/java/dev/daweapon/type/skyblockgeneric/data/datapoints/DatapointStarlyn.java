package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile;

public class DatapointStarlyn extends SkyBlockDatapoint<StarlynProfile> {
    private static final Serializer<StarlynProfile> serializer = new Serializer<>() {
        @Override
        public String serialize(StarlynProfile value) {
            return value.serialize();
        }

        @Override
        public StarlynProfile deserialize(String json) {
            return StarlynProfile.deserialize(json);
        }

        @Override
        public StarlynProfile clone(StarlynProfile value) {
            return value.copy();
        }
    };

    public DatapointStarlyn(String key, StarlynProfile value) {
        super(key, value, serializer);
    }

    public DatapointStarlyn(String key) {
        this(key, new StarlynProfile());
    }
}
