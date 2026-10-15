package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;
import dev.daweapon.type.generic.user.categories.Rank;

public class DatapointRank extends Datapoint<Rank> {
    private static final JacksonSerializer<Rank> serializer = new JacksonSerializer<>(Rank.class);

    public DatapointRank(String key, Rank value) {
        super(key, value, serializer);
    }

    public DatapointRank(String key) {
        super(key, null, serializer);
    }
}