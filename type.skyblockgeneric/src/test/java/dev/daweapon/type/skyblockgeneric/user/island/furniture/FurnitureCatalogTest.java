package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FurnitureCatalogTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path SHOP = Path.of("../configuration/skyblock/shops/carpenter.yml");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itemsIn(Path file) throws Exception {
        Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
        return items instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    private static Map<String, Map<String, Object>> allItems() throws Exception {
        Map<String, Map<String, Object>> known = new HashMap<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                itemsIn(file).forEach(item -> known.put(String.valueOf(item.get("id")), item));
            }
        }
        return known;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> component(Map<String, Object> item, String id) {
        return ((List<Map<String, Object>>) item.getOrDefault("components", List.of())).stream()
                .filter(component -> id.equals(component.get("id"))).findFirst().orElse(null);
    }

    @Test
    void everyBundleFurnitureIsDefined() {
        assertEquals(176, FurnitureCatalog.all().size());
        assertEquals(41, FurnitureCatalog.values().stream().filter(FurnitureDefinition::isCraftable).count());
    }

    @Test
    void everyFurnitureIsAPlaceableCatalogueItemWithAHead() throws Exception {
        Map<String, Map<String, Object>> items = allItems();
        List<String> problems = new ArrayList<>();
        for (FurnitureDefinition definition : FurnitureCatalog.values()) {
            Map<String, Object> item = items.get(definition.itemId());
            if (item == null) {
                problems.add(definition.itemId() + " is not an item");
                continue;
            }
            Map<String, Object> place = component(item, "PLACE_EVENT");
            Map<String, Object> head = component(item, "SKULL_HEAD");
            if (place == null || !FurniturePlacement.HANDLER_ID.equals(place.get("handler_id"))) problems.add(definition.itemId() + " has no place handler");
            if (head == null || String.valueOf(head.get("texture")).isBlank()) problems.add(definition.itemId() + " has no head texture");
            if (item.get("unavailable") != null && !String.valueOf(item.get("unavailable")).startsWith("No normal source")) {
                problems.add(definition.itemId() + " is still unavailable");
            }
        }
        assertTrue(problems.isEmpty(), problems.toString());
    }

    @Test
    void everyRecipeIngredientIsAnItemThatExists() throws Exception {
        Set<String> known = new HashSet<>(allItems().keySet());
        List<String> dangling = new ArrayList<>();
        for (FurnitureDefinition definition : FurnitureCatalog.values()) {
            if (definition.recipe() == null) continue;
            definition.recipe().forEach((ingredient, amount) -> {
                if (!known.contains(ingredient)) dangling.add(definition.itemId() + " needs " + ingredient);
                assertTrue(amount > 0);
            });
        }
        assertTrue(dangling.isEmpty(), dangling.toString());
    }

    @Test
    void carpentryLevelsMatchTheWiki() {
        assertEquals(0, FurnitureCatalog.get("STOOL").carpentryLevel());
        assertEquals(1, FurnitureCatalog.get("MINION_CHAIR").carpentryLevel());
        assertEquals(3, FurnitureCatalog.get("SKYBLOCK_BENCH").carpentryLevel());
        assertEquals(13, FurnitureCatalog.get("FURNACE_PLUS").carpentryLevel());
        assertEquals(20, FurnitureCatalog.get("WOOD_CHEST").carpentryLevel());
        assertEquals(25, FurnitureCatalog.get("GRANDFATHER_CLOCK").carpentryLevel());
        assertEquals(2, FurnitureCatalog.get("SKYBLOCK_BENCH").seats());
    }

    @Test
    void unlockedRecipesGrowWithTheCarpentryLevel() {
        assertEquals(4, CarpentryCrafting.unlocked(0).size());
        assertTrue(CarpentryCrafting.unlocked(1).size() > CarpentryCrafting.unlocked(0).size());
        assertEquals(CarpentryCrafting.craftable().size(), CarpentryCrafting.unlocked(25).size());
    }

    @Test
    void theCarpentryTableHasItsOwnRecipeInTheCatalogue() throws Exception {
        Map<String, Object> table = allItems().get("CARPENTRY_BENCH");
        assertNotNull(component(table, "DEFAULT_CRAFTABLE"));
        assertNull(FurnitureCatalog.get("CARPENTRY_BENCH").recipe());
        assertEquals(FurnitureInteraction.CARPENTRY, FurnitureCatalog.get("CARPENTRY_BENCH").interaction());
    }

    @Test
    @SuppressWarnings("unchecked")
    void theFurnitureShopSellsTheWikiPrices() throws Exception {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) YamlFileUtils.loadYaml(SHOP.toFile()).get("entries");
        Map<String, Integer> prices = new HashMap<>();
        entries.forEach(entry -> prices.put((String) entry.get("item"),
                ((Number) ((Map<String, Object>) entry.get("cost")).get("coins")).intValue()));
        assertEquals(10, prices.size());
        assertEquals(150, prices.get("FANCY_FLOWER_POT"));
        assertEquals(100, prices.get("ARMOR_SHOWCASE"));
        assertEquals(120, prices.get("SCARECROW"));
        assertEquals(100, prices.get("WEAPON_RACK"));
        assertEquals(10000, prices.get("CRAFTING_PLUS"));
        assertEquals(10000, prices.get("ENCHANTING_PLUS"));
        assertEquals(15000, prices.get("FURNACE_PLUS"));
        assertEquals(15000, prices.get("WOOD_CHEST"));
        prices.keySet().forEach(id -> assertNotNull(FurnitureCatalog.get(id), id));
    }

    @Test
    void placedFurnitureSurvivesASaveAndLoad() {
        PlacedFurniture chair = new PlacedFurniture("STOOL", 4, 100, -7, 135, null);
        PlacedFurniture chest = new PlacedFurniture("MINION_CHAIR", 1, 99, 2, 0, "COBBLESTONE:5");
        assertEquals(chair, PlacedFurniture.deserialize(chair.serialize()));
        assertEquals(chest, PlacedFurniture.deserialize(chest.serialize()));

        IslandFurniture island = IslandFurniture.deserialize(chair.serialize() + ";" + chest.serialize(), "7.5,100.0,7.5,90.0");
        assertEquals(List.of(chair, chest), island.all());
        assertEquals(chair.serialize() + ";" + chest.serialize(), island.serialize());
        assertEquals(7.5, island.spawnPoint().x());
        assertEquals("7.5,100.0,7.5,90.0", island.serializeSpawn());
    }

    @Test
    void anUnknownFurnitureIdIsDroppedOnLoad() {
        IslandFurniture island = IslandFurniture.deserialize("NOT_FURNITURE,0,0,0,0,", null);
        assertTrue(island.all().isEmpty());
        assertNull(island.spawnPoint());
    }

    @Test
    void furnitureIsCappedAtFifteenAndCenturyCakesHaveTheirOwnLimit() {
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < IslandFurniture.STANDARD_LIMIT; i++) entries.add(new PlacedFurniture("STOOL", i, 100, 0, 0, null).serialize());
        IslandFurniture island = IslandFurniture.deserialize(String.join(";", entries), null);
        assertTrue(island.isFull(FatDefinitions.STANDARD));
        assertFalse(island.isFull(FatDefinitions.CAKE));
        assertEquals(15, island.limitFor(FatDefinitions.STANDARD));
        assertEquals(20, island.limitFor(FatDefinitions.CAKE));
    }

    @Test
    void rotationSnapsToFortyFiveDegrees() {
        assertEquals(0, PlacedFurniture.snapYaw(10));
        assertEquals(45, PlacedFurniture.snapYaw(30));
        assertEquals(315, PlacedFurniture.snapYaw(-40));
        assertEquals(0, PlacedFurniture.snapYaw(360));
        assertEquals(315, new PlacedFurniture("STOOL", 0, 0, 0, 0, null).withYaw(-45).yaw());
    }

    private static final class FatDefinitions {
        static final FurnitureDefinition STANDARD = FurnitureCatalog.get("STOOL");
        static final FurnitureDefinition CAKE = new FurnitureDefinition("CENTURY_CAKE_RED", 0, FurnitureInteraction.NONE, 1, 1, 1, 0, null);
    }
}
