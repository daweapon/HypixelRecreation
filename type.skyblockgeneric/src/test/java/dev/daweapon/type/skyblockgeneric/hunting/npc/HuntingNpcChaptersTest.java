package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HuntingNpcChaptersTest {
    @SuppressWarnings("unchecked")
    private static Map<String, Object> task(String file, String trigger) throws IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File("../configuration/skyblock/chapters/" + file));
        for (Map<String, Object> chapter : (List<Map<String, Object>>) root.get("chapters")) {
            for (Map<String, Object> task : (List<Map<String, Object>>) chapter.get("tasks")) {
                if (trigger.equals(task.get("trigger"))) return task;
            }
        }
        return null;
    }

    @Test
    void theNpcTasksAreNoLongerMarkedUnavailable() throws IOException {
        for (String trigger : List.of("freddy.hint", "sanger.buy", "maxim.booster", "ant.stone")) {
            Map<String, Object> task = task("torrhus_canyon.yml", trigger);
            assertNotNull(task, trigger);
            assertNull(task.get("available"), trigger);
            assertNull(task.get("unavailable"), trigger);
        }
        Map<String, Object> kiara = task("moonglade_marsh.yml", "kiara.buy");
        assertNotNull(kiara);
        assertNull(kiara.get("available"));
        assertFalse(kiara.containsKey("unavailable"));
    }
}
