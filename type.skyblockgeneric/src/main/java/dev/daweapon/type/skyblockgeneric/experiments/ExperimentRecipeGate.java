package dev.daweapon.type.skyblockgeneric.experiments;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;

public final class ExperimentRecipeGate {
    private ExperimentRecipeGate() {
    }

    public static void install() {
        SkyBlockRecipe.getFromType(ItemType.EXPERIMENTATION_TABLE).forEach(recipe -> {
            var previous = recipe.getCanCraft();
            recipe.setCanCraft(player -> {
                if (previous != null) {
                    SkyBlockRecipe.CraftingResult result = previous.apply(player);
                    if (result != null && !result.allowed()) return result;
                }
                boolean allowed = ExperimentTable.enchantingLevel(player) >= ExperimentTable.TABLE_RECIPE_LEVEL;
                return new SkyBlockRecipe.CraftingResult(allowed,
                        new String[]{"<c>Requires Enchanting " + ExperimentTable.TABLE_RECIPE_LEVEL + "!"});
            });
        });
    }
}
