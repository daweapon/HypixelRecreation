package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Recipes mirror the sc3000 sections of the wiki's Module:Minion/Data; unlisted items fall back to ENCHANTED_<X> at 160.
public final class MinionCompaction {
    public static final int ENCHANTED_COST = 160;
    private static final String ENCHANTED_PREFIX = "ENCHANTED_";

    public record Recipe(String input, String output, int cost) {
    }

    private static final String TABLE = """
            WHEAT ENCHANTED_WHEAT 160
            ENCHANTED_WHEAT ENCHANTED_HAY_BALE 160
            SEEDS ENCHANTED_SEEDS 160
            ENCHANTED_SEEDS BOX_OF_SEEDS 160
            SUNFLOWER ENCHANTED_SUNFLOWER 160
            MOONFLOWER ENCHANTED_MOONFLOWER 160
            ENCHANTED_SUNFLOWER COMPACTED_SUNFLOWER 160
            ENCHANTED_MOONFLOWER COMPACTED_MOONFLOWER 160
            CARROT ENCHANTED_CARROT 160
            ENCHANTED_CARROT ENCHANTED_GOLDEN_CARROT 160
            POTATO ENCHANTED_POTATO 160
            ENCHANTED_POTATO ENCHANTED_BAKED_POTATO 160
            PUMPKIN ENCHANTED_PUMPKIN 160
            ENCHANTED_PUMPKIN POLISHED_PUMPKIN 160
            MELON_SLICE ENCHANTED_MELON 160
            ENCHANTED_MELON ENCHANTED_MELON_BLOCK 160
            RED_MUSHROOM ENCHANTED_RED_MUSHROOM 160
            BROWN_MUSHROOM ENCHANTED_BROWN_MUSHROOM 160
            ENCHANTED_RED_MUSHROOM ENCHANTED_RED_MUSHROOM_BLOCK 160
            ENCHANTED_BROWN_MUSHROOM ENCHANTED_BROWN_MUSHROOM_BLOCK 160
            COCOA_BEANS ENCHANTED_COCOA_BEANS 160
            ENCHANTED_COCOA_BEANS ENCHANTED_COOKIE 160
            CACTUS_GREEN ENCHANTED_CACTUS_GREEN 160
            ENCHANTED_CACTUS_GREEN ENCHANTED_CACTUS 160
            SUGAR_CANE ENCHANTED_SUGAR 160
            ENCHANTED_SUGAR ENCHANTED_SUGAR_CANE 160
            RAW_CHICKEN ENCHANTED_RAW_CHICKEN 160
            FEATHER ENCHANTED_FEATHER 160
            RAW_BEEF ENCHANTED_RAW_BEEF 160
            LEATHER ENCHANTED_LEATHER 160
            RAW_PORKCHOP ENCHANTED_PORK 160
            ENCHANTED_PORK ENCHANTED_GRILLED_PORK 160
            MUTTON ENCHANTED_MUTTON 160
            ENCHANTED_MUTTON ENCHANTED_COOKED_MUTTON 160
            WHITE_WOOL ENCHANTED_WOOL 160
            RAW_RABBIT ENCHANTED_RAW_RABBIT 160
            ENCHANTED_RAW_RABBIT ENCHANTED_COOKED_RABBIT 160
            RABBIT_HIDE ENCHANTED_RABBIT_HIDE 160
            NETHER_WART ENCHANTED_NETHER_WART 160
            ENCHANTED_NETHER_WART MUTANT_NETHER_WART 160
            COBBLESTONE ENCHANTED_COBBLESTONE 160
            COAL ENCHANTED_COAL 160
            ENCHANTED_COAL ENCHANTED_COAL_BLOCK 160
            IRON_INGOT ENCHANTED_IRON_INGOT 160
            ENCHANTED_IRON_INGOT ENCHANTED_IRON_BLOCK 160
            GOLD_INGOT ENCHANTED_GOLD_INGOT 160
            ENCHANTED_GOLD_INGOT ENCHANTED_GOLD_BLOCK 160
            DIAMOND ENCHANTED_DIAMOND 160
            ENCHANTED_DIAMOND ENCHANTED_DIAMOND_BLOCK 160
            LAPIS_LAZULI ENCHANTED_LAPIS_LAZULI 160
            ENCHANTED_LAPIS_LAZULI ENCHANTED_LAPIS_LAZULI_BLOCK 160
            EMERALD ENCHANTED_EMERALD 160
            ENCHANTED_EMERALD ENCHANTED_EMERALD_BLOCK 160
            REDSTONE ENCHANTED_REDSTONE 160
            ENCHANTED_REDSTONE ENCHANTED_REDSTONE_BLOCK 160
            QUARTZ ENCHANTED_QUARTZ 160
            ENCHANTED_QUARTZ ENCHANTED_QUARTZ_BLOCK 160
            OBSIDIAN ENCHANTED_OBSIDIAN 160
            GLOWSTONE_DUST ENCHANTED_GLOWSTONE_DUST 160
            ENCHANTED_GLOWSTONE_DUST ENCHANTED_GLOWSTONE 160
            ICE ENCHANTED_ICE 160
            ENCHANTED_ICE ENCHANTED_PACKED_ICE 160
            SAND ENCHANTED_SAND 160
            END_STONE ENCHANTED_END_STONE 160
            MITHRIL ENCHANTED_MITHRIL 160
            HARD_STONE ENCHANTED_HARD_STONE 576
            ENCHANTED_HARD_STONE CONCENTRATED_STONE 576
            MYCELIUM ENCHANTED_MYCELIUM 160
            ENCHANTED_MYCELIUM ENCHANTED_MYCELIUM_CUBE 160
            RED_SAND ENCHANTED_RED_SAND 160
            ENCHANTED_RED_SAND ENCHANTED_RED_SAND_CUBE 160
            SNOWBALL ENCHANTED_SNOW_BLOCK 640
            ROTTEN_FLESH ENCHANTED_ROTTEN_FLESH 160
            POISONOUS_POTATO ENCHANTED_POISONOUS_POTATO 160
            BONE ENCHANTED_BONE 160
            STRING ENCHANTED_STRING 160
            SPIDER_EYE ENCHANTED_SPIDER_EYE 160
            GUNPOWDER ENCHANTED_GUNPOWDER 160
            ENDER_PEARL ENCHANTED_ENDER_PEARL 20
            ENCHANTED_ENDER_PEARL ABSOLUTE_ENDER_PEARL 80
            GHAST_TEAR ENCHANTED_GHAST_TEAR 5
            ENCHANTED_GHAST_TEAR SILVER_FANG 25
            SLIME_BALL ENCHANTED_SLIME_BALL 160
            ENCHANTED_SLIME_BALL ENCHANTED_SLIME_BLOCK 160
            BLAZE_ROD ENCHANTED_BLAZE_POWDER 160
            ENCHANTED_BLAZE_POWDER ENCHANTED_BLAZE_ROD 160
            MAGMA_CREAM ENCHANTED_MAGMA_CREAM 160
            ENCHANTED_MAGMA_CREAM WHIPPED_MAGMA_CREAM 160
            HEMOVIBE HEMOGLASS 160
            OAK_LOG ENCHANTED_OAK_LOG 160
            SPRUCE_LOG ENCHANTED_SPRUCE_LOG 160
            BIRCH_LOG ENCHANTED_BIRCH_LOG 160
            DARK_OAK_LOG ENCHANTED_DARK_OAK_LOG 160
            ACACIA_LOG ENCHANTED_ACACIA_LOG 160
            JUNGLE_LOG ENCHANTED_JUNGLE_LOG 160
            DANDELION ENCHANTED_DANDELION 160
            POPPY ENCHANTED_POPPY 576
            RAW_FISH ENCHANTED_RAW_FISH 160
            ENCHANTED_RAW_FISH ENCHANTED_COOKED_FISH 160
            RAW_SALMON ENCHANTED_RAW_SALMON 160
            ENCHANTED_RAW_SALMON ENCHANTED_COOKED_SALMON 160
            PUFFERFISH ENCHANTED_PUFFERFISH 160
            TROPICAL_FISH ENCHANTED_TROPICAL_FISH 160
            PRISMARINE_CRYSTALS ENCHANTED_PRISMARINE_CRYSTALS 160
            PRISMARINE_SHARD ENCHANTED_PRISMARINE_SHARD 160
            SPONGE ENCHANTED_SPONGE 40
            ENCHANTED_SPONGE ENCHANTED_WET_SPONGE 40
            CLAY_BALL ENCHANTED_CLAY_BALL 160
            ENCHANTED_CLAY_BALL ENCHANTED_CLAY_BLOCK 160
            LILY_PAD ENCHANTED_LILY_PAD 160
            ENCHANTED_LILY_PAD CONDENSED_LILY_PAD 160
            """;

    private static final Map<String, Recipe> RECIPES = new HashMap<>();

    static {
        for (String line : TABLE.strip().split("\\R")) {
            String[] parts = line.trim().split(" ");
            RECIPES.put(parts[0], new Recipe(parts[0], parts[1], Integer.parseInt(parts[2])));
        }
    }

    private MinionCompaction() {
    }

    public static Map<String, Recipe> recipes() {
        return RECIPES;
    }

    public static int enchantedCount(int amount) {
        return Math.max(0, amount) / ENCHANTED_COST;
    }

    public static Recipe recipeFor(String inputName) {
        Recipe listed = RECIPES.get(inputName);
        if (listed != null) return listed;
        if (inputName.startsWith(ENCHANTED_PREFIX)) return null;
        return new Recipe(inputName, ENCHANTED_PREFIX + inputName, ENCHANTED_COST);
    }

    public static void compact(IslandMinionData.IslandMinion minion) {
        List<ItemQuantifiable> stored = minion.getItemsInMinion();
        List<ItemQuantifiable> produced = new ArrayList<>();
        for (ItemQuantifiable entry : stored) {
            ItemType type = entry.getItem().getAttributeHandler().getPotentialType();
            Recipe recipe = type == null ? null : recipeFor(type.name());
            if (recipe == null) continue;
            ItemType output = ItemType.get(recipe.output());
            int crafted = Math.max(0, entry.getAmount()) / recipe.cost();
            if (output == null || crafted == 0) continue;
            entry.setAmount(entry.getAmount() - crafted * recipe.cost());
            produced.add(new ItemQuantifiable(new SkyBlockItem(output), crafted));
        }
        stored.removeIf(entry -> entry.getAmount() <= 0);
        for (ItemQuantifiable made : produced) {
            ItemQuantifiable existing = stored.stream().filter(e -> e.matchesType(made.getItem())).findFirst().orElse(null);
            if (existing != null) existing.setAmount(existing.getAmount() + made.getAmount());
            else stored.add(made);
        }
    }
}
