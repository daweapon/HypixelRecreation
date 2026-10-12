package net.swofty.type.skyblockgeneric.minion;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.ItemQuantifiable;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MinionBlockCompaction {
    public record BlockRecipe(String input, String block, int cost) {
    }

    private static final Map<String, BlockRecipe> RECIPES = new LinkedHashMap<>();

    static {
        register("COAL", "COAL_BLOCK", 9);
        register("IRON_INGOT", "IRON_BLOCK", 9);
        register("GOLD_INGOT", "GOLD_BLOCK", 9);
        register("DIAMOND", "DIAMOND_BLOCK", 9);
        register("EMERALD", "EMERALD_BLOCK", 9);
        register("REDSTONE", "REDSTONE_BLOCK", 9);
        register("LAPIS_LAZULI", "LAPIS_LAZULI_BLOCK", 9);
        register("WHEAT", "HAY_BALE", 9);
        register("QUARTZ", "QUARTZ_BLOCK", 4);
        register("SNOWBALL", "SNOW_BLOCK", 4);
        register("GLOWSTONE_DUST", "GLOWSTONE", 4);
        register("SAND", "SANDSTONE", 4);
    }

    private MinionBlockCompaction() {
    }

    private static void register(String input, String block, int cost) {
        RECIPES.put(input, new BlockRecipe(input, block, cost));
    }

    public static Map<String, BlockRecipe> recipes() {
        return RECIPES;
    }

    public static void compact(IslandMinionData.IslandMinion minion) {
        List<ItemQuantifiable> stored = minion.getItemsInMinion();
        List<ItemQuantifiable> produced = new ArrayList<>();
        for (ItemQuantifiable entry : stored) {
            ItemType type = entry.getItem().getAttributeHandler().getPotentialType();
            BlockRecipe recipe = type == null ? null : RECIPES.get(type.name());
            if (recipe == null) continue;
            ItemType block = ItemType.get(recipe.block());
            int crafted = entry.getAmount() / recipe.cost();
            if (block == null || crafted == 0) continue;
            entry.setAmount(entry.getAmount() - crafted * recipe.cost());
            produced.add(new ItemQuantifiable(new SkyBlockItem(block), crafted));
        }
        stored.removeIf(entry -> entry.getAmount() <= 0);
        for (ItemQuantifiable made : produced) {
            ItemQuantifiable existing = stored.stream().filter(e -> e.matchesType(made.getItem())).findFirst().orElse(null);
            if (existing != null) existing.setAmount(existing.getAmount() + made.getAmount());
            else stored.add(made);
        }
    }
}
