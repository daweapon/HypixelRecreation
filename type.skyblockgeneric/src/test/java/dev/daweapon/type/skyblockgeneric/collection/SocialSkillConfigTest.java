package dev.daweapon.type.skyblockgeneric.collection;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocialSkillConfigTest {
    @Test
    @SuppressWarnings("unchecked")
    void socialSkillHasTwentyFiveLevelsTotallingTheWikiXp() throws Exception {
        Map<String, Object> data = YamlFileUtils.loadYaml(new File("../configuration/skyblock/skills/social.yml"));
        List<Map<String, Object>> rewards = (List<Map<String, Object>>) data.get("rewards");
        assertEquals(25, rewards.size());
        assertEquals(272_800, rewards.getLast().get("requirement"));
    }
}
