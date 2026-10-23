package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.crafting.FurnaceRecipeRegistry;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;

final class FurnaceState {
    static final int MAX_OUTPUT = 64;
    private static final Map<String, Integer> FUEL_TICKS = Map.ofEntries(
            Map.entry("COAL", 1600), Map.entry("CHARCOAL", 1600), Map.entry("COAL_BLOCK", 16000),
            Map.entry("LAVA_BUCKET", 20000), Map.entry("BLAZE_ROD", 2400), Map.entry("STICK", 100),
            Map.entry("OAK_LOG", 300), Map.entry("SPRUCE_LOG", 300), Map.entry("BIRCH_LOG", 300), Map.entry("JUNGLE_LOG", 300),
            Map.entry("ACACIA_LOG", 300), Map.entry("DARK_OAK_LOG", 300), Map.entry("OAK_PLANKS", 300), Map.entry("SPRUCE_PLANKS", 300),
            Map.entry("BIRCH_PLANKS", 300), Map.entry("JUNGLE_PLANKS", 300), Map.entry("ACACIA_PLANKS", 300), Map.entry("DARK_OAK_PLANKS", 300),
            Map.entry("DRIED_KELP_BLOCK", 4000), Map.entry("BAMBOO", 50));

    ItemStack input = ItemStack.AIR;
    ItemStack fuel = ItemStack.AIR;
    ItemStack output = ItemStack.AIR;
    int burnLeft;
    int burnTotal;
    int cookProgress;
    long touchedAt = System.currentTimeMillis();

    static FurnaceState parse(@Nullable String raw) {
        FurnaceState state = new FurnaceState();
        if (raw == null || raw.isBlank()) return state;
        JSONObject furnace = new JSONObject(raw).optJSONObject("furnace");
        if (furnace == null) return state;
        state.input = read(furnace.optString("input"));
        state.fuel = read(furnace.optString("fuel"));
        state.output = read(furnace.optString("output"));
        state.burnLeft = furnace.optInt("burnLeft");
        state.burnTotal = furnace.optInt("burnTotal");
        state.cookProgress = furnace.optInt("cook");
        state.touchedAt = furnace.optLong("at", System.currentTimeMillis());
        return state;
    }

    String serialize() {
        return new JSONObject().put("furnace", new JSONObject()
                .put("input", write(input)).put("fuel", write(fuel)).put("output", write(output))
                .put("burnLeft", burnLeft).put("burnTotal", burnTotal).put("cook", cookProgress).put("at", touchedAt)).toString();
    }

    boolean isEmpty() {
        return input.isAir() && fuel.isAir() && output.isAir();
    }

    static int fuelTicks(ItemStack stack) {
        ItemType type = type(stack);
        return type == null ? 0 : FUEL_TICKS.getOrDefault(type.name(), 0);
    }

    static @Nullable ItemType type(ItemStack stack) {
        return stack.isAir() ? null : new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
    }

    void advanceTo(long now) {
        long ticks = Math.max(0, (now - touchedAt) / 50);
        touchedAt = now;
        advance((int) Math.min(ticks, 1_000_000));
    }

    void advance(int ticks) {
        int remaining = ticks;
        while (remaining > 0) {
            Optional<FurnaceRecipeRegistry.FurnaceRecipe> recipe = recipeFor();
            if (burnLeft <= 0) {
                if (recipe.isEmpty() || !fuelUp()) {
                    cookProgress = 0;
                    return;
                }
            }
            if (recipe.isEmpty()) {
                int burned = Math.min(remaining, burnLeft);
                burnLeft -= burned;
                remaining -= burned;
                cookProgress = Math.max(0, cookProgress - burned * 2);
                continue;
            }
            int step = Math.min(remaining, Math.min(burnLeft, recipe.get().cookTime() - cookProgress));
            burnLeft -= step;
            cookProgress += step;
            remaining -= step;
            if (cookProgress >= recipe.get().cookTime()) {
                cookProgress = 0;
                finishOne(recipe.get());
            }
        }
    }

    private Optional<FurnaceRecipeRegistry.FurnaceRecipe> recipeFor() {
        ItemType inputType = type(input);
        if (inputType == null) return Optional.empty();
        Optional<FurnaceRecipeRegistry.FurnaceRecipe> recipe = FurnaceRecipeRegistry.find(inputType);
        if (recipe.isEmpty()) return recipe;
        ItemType outputType = type(output);
        if (outputType != null && (outputType != recipe.get().output() || output.amount() >= MAX_OUTPUT)) return Optional.empty();
        return recipe;
    }

    private boolean fuelUp() {
        int ticks = fuelTicks(fuel);
        if (ticks <= 0) return false;
        burnLeft = ticks;
        burnTotal = ticks;
        ItemType type = type(fuel);
        fuel = type == ItemType.LAVA_BUCKET ? new SkyBlockItem(ItemType.BUCKET).getItemStack() : consume(fuel);
        return true;
    }

    private void finishOne(FurnaceRecipeRegistry.FurnaceRecipe recipe) {
        input = consume(input);
        ItemStack made = new SkyBlockItem(recipe.output()).getItemStack();
        output = output.isAir() ? made : output.withAmount(output.amount() + 1);
    }

    private static ItemStack consume(ItemStack stack) {
        return stack.amount() <= 1 ? ItemStack.AIR : stack.withAmount(stack.amount() - 1);
    }

    private static ItemStack read(String raw) {
        return raw.isEmpty() ? ItemStack.AIR : new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(raw)).getItemStack();
    }

    private static String write(ItemStack stack) {
        return stack.isAir() ? "" : new SkyBlockItem(stack).toUnderstandable().serialize();
    }
}
