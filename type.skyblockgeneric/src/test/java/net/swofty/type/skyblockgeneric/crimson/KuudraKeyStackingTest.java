package net.swofty.type.skyblockgeneric.crimson;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.components.TrackedUniqueComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraKeyStackingTest {
    private static final Map<String, Map<String, Object>> RAW = new HashMap<>();
    private static final Map<String, ConfigurableSkyBlockItem> PARSED = new HashMap<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void load() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        List<Map<String, Object>> items = (List<Map<String, Object>>) YamlFileUtils
                .loadYaml(new File("../configuration/skyblock/items/crimson/kuudra_keys.yml")).get("items");
        for (Map<String, Object> item : items) {
            RAW.put((String) item.get("id"), item);
            PARSED.put((String) item.get("id"), ItemConfigParser.parseItem(item));
        }
    }

    @Test
    void everyKuudraKeyIsUnstackable() {
        for (KuudraTier tier : KuudraTier.values()) {
            ConfigurableSkyBlockItem key = PARSED.get(tier.getKeyItemId());
            assertNotNull(key, tier.getKeyItemId());
            assertTrue(key.hasComponent(TrackedUniqueComponent.class), tier.getKeyItemId() + " still stacks");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void eachUpgradeRecipeTakesFourSingleKeysOfTheTierBelow() {
        KuudraTier[] tiers = KuudraTier.values();
        for (int i = 1; i < tiers.length; i++) {
            List<Map<String, Object>> components = (List<Map<String, Object>>) RAW.get(tiers[i].getKeyItemId()).get("components");
            Map<String, Object> craftable = components.stream()
                    .filter(component -> "DEFAULT_CRAFTABLE".equals(component.get("id"))).findFirst().orElseThrow();
            Map<String, Object> recipe = ((List<Map<String, Object>>) craftable.get("recipes")).getFirst();
            assertEquals(List.of("AA", "AA"), recipe.get("pattern"));
            Map<String, Object> ingredient = (Map<String, Object>) ((Map<String, Object>) recipe.get("ingredients")).get("A");
            assertEquals(tiers[i - 1].getKeyItemId(), ingredient.get("type"));
            assertEquals(1, ((Number) ingredient.get("amount")).intValue());
        }
    }
}
