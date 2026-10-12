package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarCategoriesTest {
    private static final int CATEGORY_TILE_LIMIT = 24;
    private static final int NESTED_SET_LIMIT = 14;

    @Test
    void everyItemBelongsToExactlyOneGroup() {
        Map<ItemType, String> owner = new HashMap<>();
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                assertFalse(set.items.isEmpty(), set.displayName);
                for (ItemType type : set.items) {
                    assertNotNull(type, set.displayName);
                    String where = category + "/" + set.displayName;
                    String previous = owner.put(type, where);
                    assertEquals(null, previous, type.name() + " is in both " + previous + " and " + where);
                }
            }
        }
        assertFalse(owner.isEmpty());
    }

    @Test
    void everyItemTypeExists() {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                for (ItemType type : set.items) {
                    assertSame(type, ItemType.valueOf(type.name()), type.name());
                }
            }
        }
    }

    @Test
    void displayItemIsPartOfItsSet() {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                if (set.items.contains(set.displayMaterial)) continue;
                assertNotNull(ItemType.valueOf(set.displayMaterial.name()));
            }
        }
    }

    @Test
    void categoriesFitTheirGrids() {
        for (BazaarCategories category : BazaarCategories.values()) {
            Set<String> tiles = new HashSet<>();
            Map<String, Integer> nestedSizes = new HashMap<>();
            for (BazaarItemSet set : category.getItems()) {
                if (set.nestedGroup == null) {
                    tiles.add("set:" + set.displayName);
                } else {
                    tiles.add("group:" + set.nestedGroup);
                    nestedSizes.merge(set.nestedGroup, 1, Integer::sum);
                }
            }
            assertTrue(tiles.size() <= CATEGORY_TILE_LIMIT, category + " has " + tiles.size() + " tiles");
            nestedSizes.forEach((group, size) ->
                    assertTrue(size <= NESTED_SET_LIMIT, category + "/" + group + " has " + size + " sets"));
        }
    }

    @Test
    void lookupFindsEveryItem() {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet set : category.getItems()) {
                for (ItemType type : set.items) {
                    Map.Entry<BazaarCategories, BazaarItemSet> found = BazaarCategories.getFromItem(type);
                    assertNotNull(found, type.name());
                    assertSame(category, found.getKey());
                    assertSame(set, found.getValue());
                }
            }
        }
    }
}
