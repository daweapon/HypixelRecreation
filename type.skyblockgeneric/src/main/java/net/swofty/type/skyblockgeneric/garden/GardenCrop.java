package net.swofty.type.skyblockgeneric.garden;

import lombok.Getter;

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
}
