package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.event.custom.ItemCraftEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.crafting.ShapelessRecipe;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CarpentryCrafting {
    public static final int QUICK_CRAFTING_LEVEL = 3;
    public static final Rank QUICK_CRAFTING_RANK = Rank.VIP;

    private CarpentryCrafting() {
    }

    public static int level(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.CARPENTRY);
    }

    public static List<FurnitureDefinition> craftable() {
        return FurnitureCatalog.values().stream()
                .filter(FurnitureDefinition::isCraftable)
                .sorted(Comparator.comparingInt(FurnitureDefinition::carpentryLevel).thenComparing(FurnitureDefinition::itemId))
                .toList();
    }

    public static List<FurnitureDefinition> unlocked(int carpentryLevel) {
        return craftable().stream().filter(definition -> definition.carpentryLevel() <= carpentryLevel).toList();
    }

    public static boolean quickCraftingAllowed(SkyBlockPlayer player) {
        return level(player) >= QUICK_CRAFTING_LEVEL && player.getRank().isEqualOrHigherThan(QUICK_CRAFTING_RANK);
    }

    public static int affordable(SkyBlockPlayer player, FurnitureDefinition definition) {
        int crafts = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> ingredient : definition.recipe().entrySet()) {
            ItemType type = ItemType.get(ingredient.getKey());
            if (type == null) return 0;
            crafts = Math.min(crafts, player.getAmountInInventory(type) / ingredient.getValue());
        }
        return crafts == Integer.MAX_VALUE ? 0 : crafts;
    }

    public static boolean craft(SkyBlockPlayer player, FurnitureDefinition definition, int times) {
        if (times < 1 || definition.carpentryLevel() > level(player) || affordable(player, definition) < times) return false;

        ShapelessRecipe recipe = new ShapelessRecipe(SkyBlockRecipe.RecipeType.CARPENTRY, new SkyBlockItem(definition.itemType()));
        for (Map.Entry<String, Integer> ingredient : definition.recipe().entrySet()) {
            ItemType type = ItemType.get(ingredient.getKey());
            player.takeItem(type, ingredient.getValue() * times);
            recipe.add(type, ingredient.getValue());
        }
        SkyBlockItem crafted = new SkyBlockItem(definition.itemType(), times);
        player.addAndUpdateItem(new SkyBlockItem(definition.itemType(), times));
        HypixelEventHandler.callCustomEvent(new ItemCraftEvent(player, crafted, recipe));
        return true;
    }
}
