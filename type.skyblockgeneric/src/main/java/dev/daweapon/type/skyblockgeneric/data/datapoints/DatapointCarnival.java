package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalProfile;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;

public class DatapointCarnival extends SkyBlockDatapoint<CarnivalProfile> {
    private static final Serializer<CarnivalProfile> serializer = new Serializer<>() {
        @Override
        public String serialize(CarnivalProfile value) {
            return value.serialize();
        }

        @Override
        public CarnivalProfile deserialize(String json) {
            return CarnivalProfile.deserialize(json);
        }

        @Override
        public CarnivalProfile clone(CarnivalProfile value) {
            return value.copy();
        }
    };

    public DatapointCarnival(String key, CarnivalProfile value) {
        super(key, value, serializer);
    }

    public DatapointCarnival(String key) {
        this(key, new CarnivalProfile());
    }
}
