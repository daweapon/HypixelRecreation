package net.swofty.type.skyblockgeneric.forge;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.LinkedHashMap;
import java.util.Map;

public record ForgeRecipe(String id, String name, ForgeCategory category, int hotm, String requirement, int seconds,
                          int amount, long coins, Map<String, Integer> inputs) {

    public ItemType output() {
        return ItemType.get(id);
    }

    public Map<ItemType, Integer> inputTypes() {
        Map<ItemType, Integer> types = new LinkedHashMap<>();
        inputs.forEach((key, amount) -> {
            ItemType type = ItemType.get(key);
            if (type != null) types.put(type, amount);
        });
        return types;
    }

    public boolean available() {
        if (output() == null) return false;
        return inputs.keySet().stream().allMatch(key -> ItemType.get(key) != null);
    }

    public boolean hasRequirement() {
        return requirement != null && !requirement.isEmpty();
    }
}
