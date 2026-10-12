package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.gui.inventories.GUICreative;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCategoryTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void loadItems() throws IOException {
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

    private static ItemCategory of(String id) {
        ItemType type = ItemType.get(id);
        assertNotNull(type, id + " is not an item");
        return ItemCategory.of(type);
    }

    @Test
    void everyItemLandsInExactlyOneCategoryOrIsPlainVanilla() {
        int total = 0;
        for (ItemCategory category : ItemCategory.values()) total += ItemCategory.items(category).size();
        List<String> unlisted = new ArrayList<>();
        for (ItemType type : ItemType.values()) {
            ItemCategory category = ItemCategory.of(type);
            if (category == null) {
                unlisted.add(type.name());
                assertTrue(ItemCategory.isVanillaBacked(type, new SkyBlockItem(type)), type.name() + " is hidden but is not vanilla");
            } else {
                assertTrue(ItemCategory.items(category).contains(type), type.name() + " is missing from its category");
            }
        }
        assertEquals(ItemType.values().length, total + unlisted.size());
        assertEquals(total, ItemCategory.allListed().size());
    }

    @Test
    void plainVanillaItemsAreNotListedAnywhere() {
        for (String id : List.of("ACACIA_BOAT", "OAK_PLANKS", "ACACIA_DOOR", "GLASS", "DIAMOND_BLOCK", "COAL_ORE", "COBBLESTONE", "ANVIL", "CHEST", "DIAMOND_SWORD", "AIR")) {
            ItemType type = ItemType.get(id);
            if (type == null) continue;
            assertEquals(null, ItemCategory.of(type), id + " should not be in the item list");
        }
        for (ItemType type : ItemCategory.allListed()) {
            SkyBlockItem item = new SkyBlockItem(type);
            if (ItemCategory.isVanillaBacked(type, item)) {
                assertEquals(ItemCategory.MATERIALS, ItemCategory.of(type), type.name() + " is vanilla and not a material");
            }
        }
    }

    @Test
    void itemsAreSortedIntoTheCategoriesPlayersExpect() {
        assertEquals(ItemCategory.MATERIALS, of("ENCHANTED_SLIME_BALL"));
        assertEquals(ItemCategory.MATERIALS, of("DIAMOND"));
        assertEquals(ItemCategory.MATERIALS, of("MITHRIL"));
        assertEquals(ItemCategory.MATERIALS, of("ENCHANTED_ACACIA_LOG"));
        assertEquals(ItemCategory.MATERIALS, of("COAL"));
        assertEquals(ItemCategory.REGULAR, of("ASPECT_OF_THE_END"));
        assertEquals(ItemCategory.REGULAR, of("HYPERION"));
        assertEquals(ItemCategory.PETS, of("BEE_PET"));
        assertEquals(ItemCategory.PETS, of("SMALL_HONEY_DIPPER"));
        assertEquals(ItemCategory.PETS, of("SIMPLE_CARROT_CANDY"));
        assertEquals(ItemCategory.TALISMANS, of("SPEED_TALISMAN"));
    }

    @Test
    void buildingBlocksAreNeverMaterials() {
        List<String> blocks = new ArrayList<>();
        for (ItemType type : ItemCategory.items(ItemCategory.MATERIALS)) {
            SkyBlockItem item = new SkyBlockItem(type);
            if (ItemCategory.isBuildingBlock(item) && !item.hasComponent(net.swofty.type.skyblockgeneric.item.components.EnchantedComponent.class)) {
                blocks.add(type.name());
            }
        }
        assertEquals(List.of(), blocks);
    }

    @Test
    void theItemListNoLongerContainsRawVanillaItems() {
        for (ItemCategory category : ItemCategory.values()) {
            GUICreative.createInitialState(category, "", 0).items()
                    .forEach(item -> assertNotNull(item.getAttributeHandler().getPotentialType()));
        }
        assertEquals(ItemCategory.allListed().size(), GUICreative.createInitialState("", 0).items().size());
    }

    @Test
    void searchingAcrossAllCategoriesFindsItemsFromEveryCategory() {
        for (String query : List.of("honey_dipper", "speed_talisman", "enchanted_slime_ball", "aspect_of_the_end")) {
            assertTrue(GUICreative.createInitialState(null, query, 0).items().stream()
                    .anyMatch(item -> item.getAttributeHandler().getTypeAsString().toLowerCase().contains(query)), query);
        }
    }

    @Test
    void categoryButtonsAreEvenlySpacedInCentredRowsOfThree() {
        int[] slots = GUICentauriItemCategories.CATEGORY_SLOTS;
        assertEquals(ItemCategory.values().length, slots.length);
        for (int row = 0; row < slots.length; row += 3) {
            assertEquals(slots[row + 1] - slots[row], slots[row + 2] - slots[row + 1]);
            assertEquals(slots[row] % 9, 8 - slots[row + 2] % 9);
            assertEquals(slots[row] / 9, slots[row + 2] / 9);
        }
    }

    @Test
    void minionsAndAttributeShardsHaveTheirOwnCategories() {
        assertEquals(ItemCategory.MINIONS, of("COBBLESTONE_MINION"));
        assertEquals(ItemCategory.MINIONS, of("AUTO_SMELTER"));
        assertTrue(ItemCategory.items(ItemCategory.ATTRIBUTE_SHARDS).size() >= 300);
        assertTrue(ItemCategory.items(ItemCategory.MINIONS).size() >= 100);
        for (ItemType type : ItemCategory.items(ItemCategory.ATTRIBUTE_SHARDS)) {
            assertTrue(new SkyBlockItem(type).hasComponent(net.swofty.type.skyblockgeneric.item.components.AttributeShardComponent.class), type.name());
        }
    }

    @Test
    void everyCategoryButtonRendersThroughTheTextRenderer() {
        var context = net.swofty.type.generic.text.RenderContext.ofServer();
        for (ItemCategory category : ItemCategory.values()) {
            net.swofty.type.generic.text.HypixelTextRenderer.renderStack(GUICentauri.item("<a>" + category.getTitle(), category.getIcon(),
                    "<7>Browse " + category.getDescription() + ".", " ", "<7>Items: <b>" + ItemCategory.items(category).size(),
                    " ", "<e>Click to view!").build(), context);
        }
    }
}
