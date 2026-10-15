package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.skywars.SkywarsLeaderboardPreferences;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

public class DatapointSkywarsLeaderboardPreferences extends Datapoint<SkywarsLeaderboardPreferences> {
    private static final JacksonSerializer<SkywarsLeaderboardPreferences> serializer =
            new JacksonSerializer<>(SkywarsLeaderboardPreferences.class);

    public DatapointSkywarsLeaderboardPreferences(String key, SkywarsLeaderboardPreferences value) {
        super(key, value, serializer);
    }

    public DatapointSkywarsLeaderboardPreferences(String key) {
        this(key, SkywarsLeaderboardPreferences.defaults());
    }
}
