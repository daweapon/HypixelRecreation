package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBackpacks;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointStorage;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.List;
import java.util.Map;

public final class StoredItems {
    private StoredItems() {
    }

    public static int count(SkyBlockPlayer player, ItemType type) {
        int total = 0;
        for (DatapointStorage.PlayerStorage.StorageSlot page : storage(player).slots) {
            for (SkyBlockItem item : page.items) {
                if (matches(item, type)) total += item.getAmount();
            }
        }
        for (UnderstandableSkyBlockItem backpack : backpacks(player).getBackpacks().values()) {
            for (UnderstandableSkyBlockItem content : contents(backpack)) {
                if (matches(content, type)) total += content.amount();
            }
        }
        return total;
    }

    public static int take(SkyBlockPlayer player, ItemType type, int amount) {
        int left = amount;
        for (DatapointStorage.PlayerStorage.StorageSlot page : storage(player).slots) {
            for (int i = 0; i < page.items.length && left > 0; i++) {
                SkyBlockItem item = page.items[i];
                if (!matches(item, type)) continue;
                int taken = Math.min(left, item.getAmount());
                left -= taken;
                if (taken >= item.getAmount()) {
                    page.items[i] = null;
                } else {
                    item.setAmount(item.getAmount() - taken);
                }
            }
        }
        for (Map.Entry<Integer, UnderstandableSkyBlockItem> entry : backpacks(player).getBackpacks().entrySet()) {
            if (left <= 0) break;
            SkyBlockItem backpack = new SkyBlockItem(entry.getValue());
            List<UnderstandableSkyBlockItem> items = backpack.getAttributeHandler().getBackpackData().items();
            boolean changed = false;
            for (int i = 0; i < items.size() && left > 0; i++) {
                UnderstandableSkyBlockItem content = items.get(i);
                if (!matches(content, type)) continue;
                int taken = Math.min(left, content.amount());
                left -= taken;
                changed = true;
                if (taken >= content.amount()) {
                    items.set(i, new UnderstandableSkyBlockItem());
                } else {
                    SkyBlockItem reduced = new SkyBlockItem(content);
                    reduced.setAmount(content.amount() - taken);
                    items.set(i, reduced.toUnderstandable());
                }
            }
            if (changed) entry.setValue(backpack.toUnderstandable());
        }
        return amount - left;
    }

    private static boolean matches(SkyBlockItem item, ItemType type) {
        return item != null && !item.isNA() && item.getAttributeHandler().getPotentialType() == type;
    }

    private static boolean matches(UnderstandableSkyBlockItem item, ItemType type) {
        return item != null && item.itemKey() == type && item.amount() > 0;
    }

    private static List<UnderstandableSkyBlockItem> contents(UnderstandableSkyBlockItem backpack) {
        return new SkyBlockItem(backpack).getAttributeHandler().getBackpackData().items();
    }

    private static DatapointStorage.PlayerStorage storage(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class).getValue();
    }

    private static DatapointBackpacks.PlayerBackpacks backpacks(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class).getValue();
    }
}
