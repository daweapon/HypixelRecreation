package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MinionBlockCompactionTest {
    @Test
    void everyRecipeUsesRealItemTypes() {
        MinionBlockCompaction.recipes().values().forEach(recipe -> {
            assertNotNull(ItemType.get(recipe.input()), recipe.input());
            assertNotNull(ItemType.get(recipe.block()), recipe.block());
        });
    }
}
