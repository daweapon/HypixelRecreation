package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.stonks.StonksAuctionProfile;

public class DatapointStonksAuctions extends SkyBlockDatapoint<StonksAuctionProfile> {
    private static final Serializer<StonksAuctionProfile> serializer = new Serializer<>() {
        @Override
        public String serialize(StonksAuctionProfile value) {
            return value.serialize();
        }

        @Override
        public StonksAuctionProfile deserialize(String json) {
            return StonksAuctionProfile.deserialize(json);
        }

        @Override
        public StonksAuctionProfile clone(StonksAuctionProfile value) {
            return value.copy();
        }
    };

    public DatapointStonksAuctions(String key, StonksAuctionProfile value) {
        super(key, value, serializer);
    }

    public DatapointStonksAuctions(String key) {
        this(key, new StonksAuctionProfile());
    }
}
