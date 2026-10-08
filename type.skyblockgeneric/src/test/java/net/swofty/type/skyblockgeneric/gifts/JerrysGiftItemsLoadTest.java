package net.swofty.type.skyblockgeneric.gifts;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JerrysGiftItemsLoadTest {
    private static final List<String> FILES = List.of("jerrys_gifts.yml", "accessories/gift.yml");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyGiftItemParses() throws Exception {
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
