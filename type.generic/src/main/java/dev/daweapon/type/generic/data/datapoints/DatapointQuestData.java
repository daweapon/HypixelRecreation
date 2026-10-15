package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;
import dev.daweapon.type.generic.quest.QuestData;

public class DatapointQuestData extends Datapoint<QuestData> {
    private static final JacksonSerializer<QuestData> serializer = new JacksonSerializer<>(QuestData.class);

    public DatapointQuestData(String key, QuestData value) {
        super(key, value != null ? value : new QuestData(), serializer);
    }

    public DatapointQuestData(String key) {
        super(key, new QuestData(), serializer);
    }
}
