package dev.daweapon.type.skyblockgeneric.hunting.floordrop;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloorDropTableTest {
    @SuppressWarnings("unchecked")
    private static FloorDropTable load(String zone) throws Exception {
        File file = new File("../configuration/skyblock/hunting/floor_drops.yml");
        return FloorDropTable.parse((Map<String, Object>) YamlFileUtils.loadYaml(file).get(zone));
    }

    @Test
    void moongladeMatchesTheWikiWeights() throws Exception {
        FloorDropTable table = load("moonglade_marsh");
        assertEquals(106, table.totalWeight(), 0.0001);
        assertEquals(33.02, table.chance(table.rewards().get(0)), 0.01);
        assertEquals(0.94, table.chance(table.rewards().get(5)), 0.01);
    }

    @Test
    void torrhusMatchesTheWikiWeights() throws Exception {
        FloorDropTable table = load("torrhus_canyon");
        assertEquals(500, table.totalWeight(), 0.0001);
        assertEquals(40, table.chance(table.rewards().get(0)), 0.0001);
    }
}
