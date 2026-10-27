package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.skyblock.item.ItemType;


public enum HuntrapTier {
    RETIA_BASICA(ItemType.RETIA_BASICA, "Small Huntrap", 0),
    RETIA_MELIORA(ItemType.RETIA_MELIORA, "Medium Huntrap", 0.10),
    RETIA_ROBUSTA(ItemType.RETIA_ROBUSTA, "Large Huntrap", 0.20),
    RETIA_FORTA(ItemType.RETIA_FORTA, "Greater Huntrap", 0.35),
    RETIA_SUPREMA(ItemType.RETIA_SUPREMA, "Astral Huntrap", 0.50);

    private final ItemType itemType;
    private final String displayName;
    private final double reduction;

    HuntrapTier(ItemType itemType, String displayName, double reduction) {
        this.itemType = itemType;
        this.displayName = displayName;
        this.reduction = reduction;
    }

    public ItemType itemType() {
        return itemType;
    }

    public String displayName() {
        return displayName;
    }

    public double reduction() {
        return reduction;
    }
}
