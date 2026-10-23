package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class FurnitureContents {
    static final int PAGE_SLOTS = 45;

    private FurnitureContents() {
    }

    static boolean isEmpty(String raw) {
        JSONObject root = new JSONObject(raw);
        if (root.has("furnace")) return FurnaceState.parse(raw).isEmpty();
        JSONArray pages = root.optJSONArray("pages");
        if (pages == null) return true;
        for (int page = 0; page < pages.length(); page++) {
            JSONArray slots = pages.getJSONArray(page);
            for (int slot = 0; slot < slots.length(); slot++) {
                if (!slots.getString(slot).isEmpty()) return false;
            }
        }
        return true;
    }

    static List<ItemStack> page(@Nullable String raw, int page) {
        List<ItemStack> stacks = new ArrayList<>();
        JSONArray pages = raw == null ? null : new JSONObject(raw).optJSONArray("pages");
        JSONArray slots = pages != null && page < pages.length() ? pages.getJSONArray(page) : null;
        for (int slot = 0; slot < PAGE_SLOTS; slot++) {
            String serialized = slots != null && slot < slots.length() ? slots.getString(slot) : "";
            stacks.add(serialized.isEmpty() ? ItemStack.AIR : new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(serialized)).getItemStack());
        }
        return stacks;
    }

    static String withPage(@Nullable String raw, int totalPages, int page, List<ItemStack> stacks) {
        JSONArray pages = new JSONArray();
        JSONArray existing = raw == null ? null : new JSONObject(raw).optJSONArray("pages");
        for (int index = 0; index < totalPages; index++) {
            if (index == page) {
                JSONArray slots = new JSONArray();
                stacks.forEach(stack -> slots.put(stack.isAir() ? "" : new SkyBlockItem(stack).toUnderstandable().serialize()));
                pages.put(slots);
            } else if (existing != null && index < existing.length()) {
                pages.put(existing.getJSONArray(index));
            } else {
                pages.put(new JSONArray());
            }
        }
        return new JSONObject().put("pages", pages).toString();
    }
}
