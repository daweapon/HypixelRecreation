package net.swofty.type.skyblockgeneric.data;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.data.datapoints.*;
import net.swofty.type.skyblockgeneric.data.datapoints.*;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.trade.TradeManager;
import net.swofty.type.skyblockgeneric.trade.TradeOffer;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

final class SkyBlockInventorySync {

    private SkyBlockInventorySync() {
    }

    static void applyLoaded(SkyBlockPlayer player, Datapoint<?> datapoint) {
        SkyBlockInventory inv = (SkyBlockInventory) datapoint.getValue();

        player.setHelmet(new SkyBlockItem(inv.getHelmet()).getItemStack());
        player.setChestplate(new SkyBlockItem(inv.getChestplate()).getItemStack());
        player.setLeggings(new SkyBlockItem(inv.getLeggings()).getItemStack());
        player.setBoots(new SkyBlockItem(inv.getBoots()).getItemStack());

        List<SkyBlockItem> displaced = new ArrayList<>();
        inv.getItems().forEach((slot, item) -> {
            SkyBlockItem loaded = new SkyBlockItem(item);
            if (loaded.getItemType() == ItemType.SKYBLOCK_MENU) return;
            if (slot == 8) {
                displaced.add(loaded);
                return;
            }
            PlayerItemOrigin origin = PlayerItemOrigin.INVENTORY_SLOT;
            origin.setData(slot);

            ItemStack updated = PlayerItemUpdater.playerUpdate(
                    player,
                    loaded.getItemStack(),
                    true
            ).build();

            origin.setStack(player, updated);
        });

        player.getInventory().setItemStack(8,
                new NonPlayerItemUpdater(new SkyBlockItem(ItemType.SKYBLOCK_MENU).getItemStack())
                        .getUpdatedItem().build());
        displaced.forEach(player::addAndUpdateItem);
    }

    static Datapoint<?> capture(SkyBlockPlayer player) {
            SkyBlockInventory inv = new SkyBlockInventory();
            ItemStack helmet = player.getHelmet();
            if (SkyBlockItem.isSkyBlockItem(helmet)) inv.setHelmet(new SkyBlockItem(helmet).toUnderstandable());
            ItemStack chestplate = player.getChestplate();
            if (SkyBlockItem.isSkyBlockItem(chestplate))
                inv.setChestplate(new SkyBlockItem(chestplate).toUnderstandable());
            ItemStack leggings = player.getLeggings();
            if (SkyBlockItem.isSkyBlockItem(leggings))
                inv.setLeggings(new SkyBlockItem(leggings).toUnderstandable());
            ItemStack boots = player.getBoots();
            if (SkyBlockItem.isSkyBlockItem(boots)) inv.setBoots(new SkyBlockItem(boots).toUnderstandable());

            for (int i = 0; i < 36; i++) {
                ItemStack stack = player.getInventory().getItemStack(i);
                if (SkyBlockItem.isSkyBlockItem(stack)) {
                    inv.getItems().put(i, new SkyBlockItem(stack).toUnderstandable());
                }
            }
            for (TradeOffer.HeldItem held : TradeManager.heldItems(player)) {
                int slot = inv.getItems().containsKey(held.homeSlot()) ? freeInventorySlot(inv) : held.homeSlot();
                if (slot >= 0) inv.getItems().put(slot, new SkyBlockItem(held.stack()).toUnderstandable());
            }
            return new DatapointInventory("inventory", inv);
    }

    private static int freeInventorySlot(SkyBlockInventory inventory) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot != 8 && !inventory.getItems().containsKey(slot)) return slot;
        }
        return -1;
    }
}
