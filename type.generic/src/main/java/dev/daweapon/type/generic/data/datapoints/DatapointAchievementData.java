package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.achievement.AchievementData;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint storing a player's AchievementData (progress and tier tracking), serialized via Jackson.
public class DatapointAchievementData extends Datapoint<AchievementData> {
    private static final JacksonSerializer<AchievementData> serializer = new JacksonSerializer<>(AchievementData.class);

    public DatapointAchievementData(String key, AchievementData value) {
        super(key, value != null ? value : new AchievementData(), serializer);
    }

    public DatapointAchievementData(String key) {
        super(key, new AchievementData(), serializer);
    }
}
