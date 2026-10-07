package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.shen.ShenAuctionProfile;

public class DatapointShenAuctions extends SkyBlockDatapoint<ShenAuctionProfile> {
    private static final Serializer<ShenAuctionProfile> serializer = new Serializer<>() {
        @Override
        public String serialize(ShenAuctionProfile value) {
            return value.serialize();
        }

        @Override
        public ShenAuctionProfile deserialize(String json) {
            return ShenAuctionProfile.deserialize(json);
        }

        @Override
        public ShenAuctionProfile clone(ShenAuctionProfile value) {
            return value.copy();
        }
    };

    public DatapointShenAuctions(String key, ShenAuctionProfile value) {
        super(key, value, serializer);
    }

    public DatapointShenAuctions(String key) {
        this(key, new ShenAuctionProfile());
    }
}
