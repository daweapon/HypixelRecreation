package dev.daweapon.type.skyblockgeneric.event.actions.custom.skill;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.ItemCraftEvent;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.CraftableComponent;
import dev.daweapon.type.skyblockgeneric.item.components.SellableComponent;
import dev.daweapon.type.skyblockgeneric.item.crafting.ShapelessRecipe;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

// Grants Carpentry skill XP from crafted items based on their raw materials' NPC sell value
public class ActionSkillCarpentryGain implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(ItemCraftEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        if (!player.getMissionData().hasCompleted("give_wool_to_carpenter")) return;

        SkyBlockItem craftedItem = event.getCraftedItem();

        if (craftedItem == null || craftedItem.isAir() || craftedItem.isNA()) return;
        if (ItemType.fromMaterial(craftedItem.getMaterial()) != null &&
                ItemType.fromMaterial(craftedItem.getMaterial()) == craftedItem.getAttributeHandler().getPotentialType()) return;

        Map<String, Integer> rawItems = new HashMap<>();
        double npcSellValue = 0;

        if (!craftedItem.hasComponent(CraftableComponent.class) && event.getRecipe() instanceof ShapelessRecipe carpentry
                && carpentry.getRecipeType() == SkyBlockRecipe.RecipeType.CARPENTRY) {
            int crafts = craftedItem.getAmount() / Math.max(1, carpentry.getResult().getAmount());
            for (ItemQuantifiable ingredient : carpentry.getIngredientList()) {
                collectRawItems(ingredient.getItem(), ingredient.getAmount() * crafts, rawItems);
            }
        } else {
            collectRawItems(craftedItem, craftedItem.getAmount(), rawItems);
        }

        for (Map.Entry<String, Integer> entry : rawItems.entrySet()) {
            String itemId = entry.getKey();
            int amount = entry.getValue();

            SkyBlockItem resolvedItem = new SkyBlockItem(ItemType.get(itemId));
            if (resolvedItem.hasComponent(SellableComponent.class)) {
                double sellPrice = resolvedItem.getComponent(SellableComponent.class).getSellValue();
                npcSellValue += sellPrice * amount;
            }
        }

        double carpentryXP = 0.03 * npcSellValue;

        player.getSkills().increase(player, SkillCategories.CARPENTRY, carpentryXP);
    }

    private void collectRawItems(SkyBlockItem item, int multiplier, Map<String, Integer> rawItems) {
        if (!item.hasComponent(CraftableComponent.class)) {
            String key = item.getAttributeHandler().getTypeAsString();
            rawItems.merge(key, multiplier, Integer::sum);
            return;
        }

        SkyBlockRecipe<?> recipe = item.getComponent(CraftableComponent.class).getRecipes().getFirst();
        SkyBlockItem[] ingredients = recipe.getRecipeDisplay();

        int recipeResultAmount = recipe.getResult().getAmount();
        double scaleFactor = (double) multiplier / recipeResultAmount;

        for (SkyBlockItem ingredient : ingredients) {
            if (ingredient == null || ingredient.isAir() || ingredient.isNA()) continue;

            int totalAmount = (int) Math.ceil(ingredient.getAmount() * scaleFactor);
            collectRawItems(ingredient, totalAmount, rawItems);
        }
    }
}

