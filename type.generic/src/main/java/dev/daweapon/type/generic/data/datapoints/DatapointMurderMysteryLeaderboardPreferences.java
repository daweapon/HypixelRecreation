package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.murdermystery.MurderMysteryLeaderboardPreferences;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

public class DatapointMurderMysteryLeaderboardPreferences extends Datapoint<MurderMysteryLeaderboardPreferences> {
    private static final JacksonSerializer<MurderMysteryLeaderboardPreferences> serializer =
            new JacksonSerializer<>(MurderMysteryLeaderboardPreferences.class);

    public DatapointMurderMysteryLeaderboardPreferences(String key, MurderMysteryLeaderboardPreferences value) {
        super(key, value, serializer);
    }

    public DatapointMurderMysteryLeaderboardPreferences(String key) {
        this(key, MurderMysteryLeaderboardPreferences.defaults());
    }
}
