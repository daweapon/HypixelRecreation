package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile;

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
