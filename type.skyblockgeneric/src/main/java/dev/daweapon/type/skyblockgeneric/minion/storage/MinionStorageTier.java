package dev.daweapon.type.skyblockgeneric.minion.storage;

import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.Optional;

public enum MinionStorageTier {
    SMALL(ItemType.SMALL_ENCHANTED_CHEST, 3),
    MEDIUM(ItemType.MEDIUM_ENCHANTED_CHEST, 9),
    LARGE(ItemType.LARGE_ENCHANTED_CHEST, 15),
    XLARGE(ItemType.XLARGE_ENCHANTED_CHEST, 21),
    XXLARGE(ItemType.XXLARGE_ENCHANTED_CHEST, 27);

    public static final int ITEMS_PER_SLOT = 64;

    private final ItemType itemType;
    private final int slots;

    MinionStorageTier(ItemType itemType, int slots) {
        this.itemType = itemType;
        this.slots = slots;
    }

    public ItemType itemType() {
        return itemType;
    }

    public int slots() {
        return slots;
    }

    public int capacityItems() {
        return slots * ITEMS_PER_SLOT;
    }

    public static Optional<MinionStorageTier> fromItem(ItemType type) {
        for (MinionStorageTier tier : values()) {
            if (tier.itemType == type) return Optional.of(tier);
        }
        return Optional.empty();
    }
}
