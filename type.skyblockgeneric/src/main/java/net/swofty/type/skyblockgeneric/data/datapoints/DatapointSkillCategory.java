package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;

// Persists a single SkillCategories value (e.g. the last skill category a player edited).
public class DatapointSkillCategory extends SkyBlockDatapoint<SkillCategories> {
    private static final JacksonSerializer<SkillCategories> serializer = new JacksonSerializer<>(SkillCategories.class);

    public DatapointSkillCategory(String key, SkillCategories value) {
        super(key, value, serializer);
    }

    public DatapointSkillCategory(String key) {
        super(key, null, serializer);
    }
}