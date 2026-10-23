package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.List;

public final class WandStorage {
    public static final int ROWS = 3;
    public static final int SLOTS = ROWS * 9;

    private WandStorage() {
    }

    public static int count(SkyBlockItem wand, ItemType type) {
        int total = 0;
        for (UnderstandableSkyBlockItem stored : wand.getAttributeHandler().getBackpackData().items()) {
            SkyBlockItem item = new SkyBlockItem(stored);
            if (!item.isNA() && !item.isAir() && item.getAttributeHandler().getPotentialType() == type) total += item.getAmount();
        }
        return total;
    }

    public static int take(SkyBlockItem wand, ItemType type, int amount) {
        List<UnderstandableSkyBlockItem> updated = new ArrayList<>();
        int remaining = amount;
        for (UnderstandableSkyBlockItem stored : wand.getAttributeHandler().getBackpackData().items()) {
            SkyBlockItem item = new SkyBlockItem(stored);
            if (remaining > 0 && !item.isNA() && !item.isAir() && item.getAttributeHandler().getPotentialType() == type) {
                int used = Math.min(remaining, item.getAmount());
                remaining -= used;
                if (used == item.getAmount()) {
                    updated.add(new SkyBlockItem(ItemType.AIR).toUnderstandable());
                    continue;
                }
                item.setAmount(item.getAmount() - used);
                updated.add(item.toUnderstandable());
                continue;
            }
            updated.add(stored);
        }
        wand.getAttributeHandler().setBackpackData(new dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeBackpackData.BackpackData(updated));
        return amount - remaining;
    }
}
