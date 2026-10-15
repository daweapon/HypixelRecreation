package dev.daweapon.type.skyblockgeneric.slayer;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;

import java.util.function.Function;

/**
 * Locks every recipe a slayer level unlocks (the {@code unlocks} lists in slayers.yml) until the
 * player reaches that level.
 */
public final class SlayerRecipeGates {
    private SlayerRecipeGates() {
    }

    public static void install() {
        for (SlayerDefinition definition : SlayerRegistry.all()) {
            for (SlayerLevelReward reward : definition.levels()) {
                for (ItemType item : reward.unlocks()) {
                    SkyBlockRecipe.getFromType(item).forEach(recipe -> gate(recipe, definition.type(), reward.level()));
                }
            }
        }
    }

    private static void gate(SkyBlockRecipe<?> recipe, SlayerType type, int level) {
        Function<dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer, SkyBlockRecipe.CraftingResult> previous = recipe.getCanCraft();
        recipe.setCanCraft(player -> {
            if (previous != null) {
                SkyBlockRecipe.CraftingResult result = previous.apply(player);
                if (result != null && !result.allowed()) return result;
            }
            boolean allowed = SlayerRequirement.level(player, type) >= level;
            return new SkyBlockRecipe.CraftingResult(allowed, new String[]{SlayerRequirement.lockedLine(type, level)});
        });
    }
}
