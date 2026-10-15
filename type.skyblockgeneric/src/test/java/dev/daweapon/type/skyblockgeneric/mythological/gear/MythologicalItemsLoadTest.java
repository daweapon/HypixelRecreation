package dev.daweapon.type.skyblockgeneric.mythological.gear;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeLoader;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MythologicalItemsLoadTest {
    private static final List<String> FILES = List.of("mythological.yml", "pets/petitems.yml", "accessories/anguish.yml", "weapons.yml", "staff.yml");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyMythologicalItemParses() throws Exception {
        File items = new File("../configuration/skyblock/items");
        for (String name : FILES) {
            Map<String, Object> data = YamlFileUtils.loadYaml(new File(items, name));
            for (Map<String, Object> item : (List<Map<String, Object>>) data.get("items")) {
                ConfigurableSkyBlockItem parsed = ItemConfigParser.parseItem(item);
                assertNotNull(parsed, name + ": " + item.get("id"));
            }
        }
    }
}
