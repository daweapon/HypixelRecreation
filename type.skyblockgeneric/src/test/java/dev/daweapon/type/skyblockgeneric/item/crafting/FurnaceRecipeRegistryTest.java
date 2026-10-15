package dev.daweapon.type.skyblockgeneric.item.crafting;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FurnaceRecipeRegistryTest {
    private static final Map<String, String> SMELTS = Map.of(
            "RAW_BEEF", "COOKED_BEEF",
            "RAW_CHICKEN", "COOKED_CHICKEN",
            "RAW_FISH", "COOKED_FISH",
            "MUTTON", "COOKED_MUTTON",
            "RAW_RABBIT", "COOKED_RABBIT",
            "CLAY_BALL", "CLAY_BRICK");

    @Test
    void skyBlockRawFoodAndClayBallSmeltIntoTheirSkyBlockItems() {
        SMELTS.forEach((input, output) -> {
            var recipe = FurnaceRecipeRegistry.find(ItemType.valueOf(input));
            assertTrue(recipe.isPresent(), input + " has no furnace recipe");
            assertEquals(ItemType.valueOf(output), recipe.get().output());
        });
    }
}
