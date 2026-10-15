package dev.daweapon.type.hub.gui.elizabeth.subguis;

import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockItemAchievements;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.hub.gui.elizabeth.GUIBitsShop;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class GUIBitsConfirmBuy extends HypixelInventoryGUI {

    SkyBlockItem item;
    Integer price;
    public GUIBitsConfirmBuy(SkyBlockItem item, Integer price) {
        super("Confirm", InventoryType.CHEST_3_ROW);
        this.item = item;
        this.price = price;
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(new GUIClickableItem(11) {

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                player.addAndUpdateItem(item);
                SkyBlockItemAchievements.onObtained(player, item.getAttributeHandler().getPotentialType());
                player.removeBits(price);
                new GUIBitsShop().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.LIME_TERRACOTTA, """
                        <a>Confirm
                        <7>Buying: {}
                        <7>Cost: <b>{:,}""", item.getDisplayName(), price);
            }
        });
        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel");
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
