package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;
import dev.daweapon.type.generic.leaderboard.LeaderboardTracked;

// Datapoint storing a player's network-wide Hypixel experience, tracked on the network experience leaderboard.
public class DatapointHypixelExperience extends Datapoint<Long> implements LeaderboardTracked {
    private static final JacksonSerializer<Long> serializer = new JacksonSerializer<>(Long.class);
    private static final String LEADERBOARD_KEY = "hypixel:network_experience";

    public DatapointHypixelExperience(String key, Long value) {
        super(key, value != null ? value : 0L, serializer);
    }

    public DatapointHypixelExperience(String key) {
        super(key, 0L, serializer);
    }

    @Override
    public String getLeaderboardKey() {
        return LEADERBOARD_KEY;
    }

    @Override
    public double getLeaderboardScore() {
        return value != null ? value.doubleValue() : 0.0;
    }
}
