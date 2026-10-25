package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpookNpcCatalogueTest {
    @Test
    void everyIngredientItemExists() {
        for (String id : List.of("THE_SOUP_PAINTING", "SIRIUS_BOOK", "BETRAYAL_HEAD", "COW_HEAD", "FREE_SPIDER",
                "DWARVEN_IRON_HAMMER", "DWARVEN_GOLD_HAMMER", "DWARVEN_EMERALD_HAMMER", "EPHEMERAL_GRATITUDE",
                "POISONED_CANDY", "SECRET_I_POTION", "DECORATION_ONION", "DECORATION_LETTUCE", "FRENCH_FRIES")) {
            assertNotNull(ItemType.get(id), id);
        }
    }
}
