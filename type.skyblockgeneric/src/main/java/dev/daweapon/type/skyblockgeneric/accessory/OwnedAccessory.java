package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AccessoryComponent;
import org.jetbrains.annotations.Nullable;

public record OwnedAccessory(SkyBlockItem item, ItemType type, AccessoryComponent definition, boolean inBag) {
    public static @Nullable OwnedAccessory of(@Nullable SkyBlockItem item, boolean inBag) {
        if (item == null || item.isNA() || item.isAir() || !item.hasComponent(AccessoryComponent.class)) return null;
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type == null) return null;
        return new OwnedAccessory(item, type, item.getComponent(AccessoryComponent.class), inBag);
    }

    public int rarityPower() {
        return item.getAttributeHandler().getEffectiveRarity().getAccessoryPower();
    }
}
