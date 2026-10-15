package dev.daweapon.type.skyblockgeneric.region.mining;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.components.CustomDropComponent;
import dev.daweapon.type.skyblockgeneric.item.components.CustomDropComponent.DropRule;
import dev.daweapon.type.skyblockgeneric.item.components.SkillableMineComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PondIceItemTest {
    private static ConfigurableSkyBlockItem ice;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws Exception {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        Map<String, Object> data = YamlFileUtils.loadYaml(new File("../configuration/skyblock/items/mining/vanilla.yml"));
        for (Map<String, Object> item : (List<Map<String, Object>>) data.get("items")) {
            if ("ICE".equals(item.get("id"))) ice = ItemConfigParser.parseItem(item);
        }
        assertNotNull(ice);
    }

    @Test
    void miningIceGrantsHalfAMiningXp() {
        assertEquals(0.5, ice.getComponent(SkillableMineComponent.class).getMiningValue());
    }

    @Test
    void jerrysWorkshopIceOnlyDropsWithSilkTouch() {
        List<DropRule> rules = ice.getComponent(CustomDropComponent.class).getRules();
        DropRule firstMatchWithoutSilkTouch = rules.stream()
                .filter(rule -> rule.matches(null, null, ServerType.SKYBLOCK_JERRYS_WORKSHOP, false))
                .findFirst().orElseThrow();
        assertTrue(firstMatchWithoutSilkTouch.drops().isEmpty());
        assertTrue(rules.stream().noneMatch(rule -> rule.matches(null, null, ServerType.SKYBLOCK_HUB, true)));
    }
}
