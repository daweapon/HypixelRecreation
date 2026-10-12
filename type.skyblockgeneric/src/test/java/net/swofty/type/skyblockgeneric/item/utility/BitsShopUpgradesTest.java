package net.swofty.type.skyblockgeneric.item.utility;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AbiphoneComponent;
import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitsShopUpgradesTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
    }

    @Test
    void pocketSackAddsTwoHundredPercentOfTheOriginalCapacityUpToThreeTimes() {
        SkyBlockItem sack = new SkyBlockItem(ItemType.DUNGEON_SACK);
        SackComponent component = sack.getComponent(SackComponent.class);
        assertEquals(20160, component.capacityOf(sack));

        AnvilCombineHandler handler = AnvilCombineRegistry.getHandler(PocketSackInASack.HANDLER_ID);
        SkyBlockItem pocket = new SkyBlockItem(ItemType.POCKET_SACK_IN_A_SACK);
        for (int use = 1; use <= SackComponent.MAX_UPGRADES; use++) {
            assertTrue(handler.validateFunction().canApply(null, sack, pocket));
            handler.combineFunction().apply(sack, pocket);
            assertEquals(20160 * (1 + 2 * use), component.capacityOf(sack));
        }
        assertFalse(handler.validateFunction().canApply(null, sack, pocket));
    }

    @Test
    void pocketSackOnlyAppliesToSacks() {
        AnvilCombineHandler handler = AnvilCombineRegistry.getHandler(PocketSackInASack.HANDLER_ID);
        assertFalse(handler.validateFunction().canApply(null, new SkyBlockItem(ItemType.DIAMOND), new SkyBlockItem(ItemType.POCKET_SACK_IN_A_SACK)));
    }

    @Test
    void contactsTrioRaisesTheContactLimitByThreeUpToSeventyFiveExtra() {
        SkyBlockItem abiphone = new SkyBlockItem(ItemType.ABIPHONE_BASIC);
        AbiphoneComponent component = abiphone.getComponent(AbiphoneComponent.class);
        int base = component.contactLimit(abiphone);

        AnvilCombineHandler handler = AnvilCombineRegistry.getHandler("ABIPHONE_CONTACTS_TRIO");
        SkyBlockItem trio = new SkyBlockItem(ItemType.ABIPHONE_CONTACTS_TRIO);
        for (int use = 1; use <= 25; use++) {
            assertTrue(handler.validateFunction().canApply(null, abiphone, trio));
            handler.combineFunction().apply(abiphone, trio);
            assertEquals(base + 3 * use, component.contactLimit(abiphone));
        }
        assertFalse(handler.validateFunction().canApply(null, abiphone, trio));
    }

    @Test
    void contactsTrioOnlyAppliesToAbiphones() {
        AnvilCombineHandler handler = AnvilCombineRegistry.getHandler("ABIPHONE_CONTACTS_TRIO");
        assertFalse(handler.validateFunction().canApply(null, new SkyBlockItem(ItemType.DIAMOND), new SkyBlockItem(ItemType.ABIPHONE_CONTACTS_TRIO)));
    }
}
