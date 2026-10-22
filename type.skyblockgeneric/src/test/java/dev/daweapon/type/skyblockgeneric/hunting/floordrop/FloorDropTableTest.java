package dev.daweapon.type.skyblockgeneric.hunting.floordrop;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloorDropTableTest {
    @SuppressWarnings("unchecked")
    private static FloorDropTable load() throws Exception {
        File file = new File("../configuration/skyblock/hunting/floor_drops.yml");
        if (!file.exists()) file = new File("configuration/skyblock/hunting/floor_drops.yml");
        return FloorDropTable.parse((Map<String, Object>) YamlFileUtils.loadYaml(file).get("torrhus_canyon"));
    }

    @Test
    void torrhusWeightsMatchTheWikiChances() throws Exception {
        FloorDropTable table = load();
        assertEquals(500, table.totalWeight());
        FloorDropTable.Reward shard = table.rewards().stream().filter(r -> r.kind() == FloorDropTable.Kind.SHARD).findFirst().orElseThrow();
        assertEquals(40, table.chance(shard), 1e-9);
        FloorDropTable.Reward big = table.rewards().stream().filter(r -> r.amount() == 64).findFirst().orElseThrow();
        assertEquals(10, table.chance(big), 1e-9);
        assertEquals(5, table.rewards().size());
    }
}
