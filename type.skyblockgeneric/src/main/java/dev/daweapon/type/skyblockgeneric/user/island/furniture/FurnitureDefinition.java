package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record FurnitureDefinition(
        String itemId,
        int carpentryLevel,
        FurnitureInteraction interaction,
        double scale,
        float width,
        float height,
        int seats,
        @Nullable Map<String, Integer> recipe
) {
    public static final String CENTURY_CAKE_PREFIX = "CENTURY_CAKE";

    public boolean isCenturyCake() {
        return itemId.startsWith(CENTURY_CAKE_PREFIX);
    }

    public boolean isCraftable() {
        return recipe != null && !recipe.isEmpty();
    }

    public @Nullable ItemType itemType() {
        return ItemType.get(itemId);
    }
}
