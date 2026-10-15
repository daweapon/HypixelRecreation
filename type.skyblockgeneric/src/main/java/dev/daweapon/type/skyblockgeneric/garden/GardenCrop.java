package dev.daweapon.type.skyblockgeneric.garden;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

@Getter
public enum GardenCrop {
    WHEAT("Wheat"),
    CARROT("Carrot"),
    POTATO("Potato"),
    PUMPKIN("Pumpkin"),
    SUGAR_CANE("Sugar Cane"),
    MELON("Melon"),
    CACTUS("Cactus"),
    COCOA_BEANS("Cocoa Beans"),
    MUSHROOM("Mushroom"),
    NETHER_WART("Nether Wart"),
    SUNFLOWER("Sunflower"),
    MOONFLOWER("Moonflower"),
    WILD_ROSE("Wild Rose");

    private final String displayName;

    GardenCrop(String displayName) {
        this.displayName = displayName;
    }

    public static @Nullable GardenCrop ofItem(@Nullable ItemType type) {
        if (type == null) return null;
        return switch (type.name()) {
            case "WHEAT" -> WHEAT;
            case "CARROT" -> CARROT;
            case "POTATO" -> POTATO;
            case "PUMPKIN" -> PUMPKIN;
            case "SUGAR_CANE" -> SUGAR_CANE;
            case "MELON_SLICE" -> MELON;
            case "CACTUS" -> CACTUS;
            case "COCOA_BEANS" -> COCOA_BEANS;
            case "RED_MUSHROOM", "BROWN_MUSHROOM" -> MUSHROOM;
            case "NETHER_WART" -> NETHER_WART;
            case "SUNFLOWER" -> SUNFLOWER;
            case "MOONFLOWER" -> MOONFLOWER;
            case "WILD_ROSE" -> WILD_ROSE;
            default -> null;
        };
    }
}
