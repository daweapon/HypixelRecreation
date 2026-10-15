package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContestProfile;

public class DatapointFarmingContests extends SkyBlockDatapoint<FarmingContestProfile> {
    private static final Serializer<FarmingContestProfile> serializer = new Serializer<>() {
        @Override
        public String serialize(FarmingContestProfile value) {
            return value.serialize();
        }

        @Override
        public FarmingContestProfile deserialize(String json) {
            return FarmingContestProfile.deserialize(json);
        }

        @Override
        public FarmingContestProfile clone(FarmingContestProfile value) {
            return value.copy();
        }
    };

    public DatapointFarmingContests(String key, FarmingContestProfile value) {
        super(key, value, serializer);
    }

    public DatapointFarmingContests(String key) {
        this(key, new FarmingContestProfile());
    }
}
