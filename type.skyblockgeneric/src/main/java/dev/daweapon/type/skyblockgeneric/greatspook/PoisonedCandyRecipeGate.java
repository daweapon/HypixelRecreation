package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;

public final class PoisonedCandyRecipeGate {
    private PoisonedCandyRecipeGate() {
    }

    public static void install() {
        SkyBlockRecipe.getFromType(ItemType.POISONED_CANDY).forEach(recipe -> {
            var previous = recipe.getCanCraft();
            recipe.setCanCraft(player -> {
                if (previous != null) {
                    SkyBlockRecipe.CraftingResult result = previous.apply(player);
                    if (result != null && !result.allowed()) return result;
                }
                boolean allowed = GreatSpookEvent.isActive() && TyashoiProgress.introduced(player);
                return new SkyBlockRecipe.CraftingResult(allowed,
                        new String[]{"<7>Talk to the <5>Tyashoi Alchemist <7>during the <5>Great Spook<7>."});
            });
        });
    }
}
