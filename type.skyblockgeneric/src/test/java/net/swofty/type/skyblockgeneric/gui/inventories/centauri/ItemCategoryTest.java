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
    void everyItemLandsInExactlyOneCategory() {
        int total = 0;
        for (ItemCategory category : ItemCategory.values()) total += ItemCategory.items(category).size();
        assertEquals(ItemType.values().length, total);
        for (ItemType type : ItemType.values()) {
            assertTrue(ItemCategory.items(ItemCategory.of(type)).contains(type), type.name() + " is missing from its category");
        }
    }

    @Test
    void itemsAreSortedIntoTheCategoriesPlayersExpect() {
        assertEquals(ItemCategory.MATERIALS, of("ENCHANTED_SLIME_BALL"));
        assertEquals(ItemCategory.MATERIALS, of("DIAMOND"));
        assertEquals(ItemCategory.MATERIALS, of("MITHRIL"));
        assertEquals(ItemCategory.MATERIALS, of("ENCHANTED_ACACIA_LOG"));
        assertEquals(ItemCategory.REGULAR, of("ASPECT_OF_THE_END"));
        assertEquals(ItemCategory.REGULAR, of("HYPERION"));
        assertEquals(ItemCategory.PETS, of("BEE_PET"));
        assertEquals(ItemCategory.PETS, of("SMALL_HONEY_DIPPER"));
        assertEquals(ItemCategory.PETS, of("SIMPLE_CARROT_CANDY"));
        assertEquals(ItemCategory.TALISMANS, of("SPEED_TALISMAN"));
    }

    @Test
    void buildingBlocksAreNeverMaterials() {
        for (String id : List.of("OAK_PLANKS", "GLASS", "DIAMOND_BLOCK", "COAL_ORE", "COBBLESTONE", "ANVIL", "CHEST")) {
            if (ItemType.get(id) == null) continue;
            assertEquals(ItemCategory.REGULAR, of(id), id + " is a building block");
        }
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
        assertEquals(ItemType.values().length, GUICreative.createInitialState("", 0).items().size());
    }

    @Test
    void searchingAcrossAllCategoriesFindsItemsFromEveryCategory() {
        for (String query : List.of("honey_dipper", "speed_talisman", "enchanted_slime_ball", "aspect_of_the_end")) {
            assertTrue(GUICreative.createInitialState(null, query, 0).items().stream()
                    .anyMatch(item -> item.getAttributeHandler().getTypeAsString().toLowerCase().contains(query)), query);
        }
    }

    @Test
    void categoryButtonsAreEvenlySpacedAcrossTheMenu() {
        int[] slots = GUICentauriItemCategories.CATEGORY_SLOTS;
        assertEquals(ItemCategory.values().length, slots.length);
        for (int i = 1; i < slots.length; i++) assertEquals(slots[1] - slots[0], slots[i] - slots[i - 1]);
        assertEquals(slots[0] % 9, 8 - slots[slots.length - 1] % 9);
        assertEquals(slots[0] / 9, slots[slots.length - 1] / 9);
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
