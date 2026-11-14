package dev.daweapon.type.bedwarslobby.gui;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;

// GUI providing quick teleports to named locations within the Slumber Hotel lobby area.
public class GUISlumberLocations extends HypixelInventoryGUI {

    public GUISlumberLocations() {
        super("Slumber Locations", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.teleport(new Pos(17, 69, 0, -90, 0));
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_DOOR, "<a>Teleport to Doorman Dave");
            }
        });
        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.teleport(new Pos(43, 69, 0, -90, 0));
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.RED_BED, "<e>Teleport to Reception");
            }
        });
        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.teleport(new Pos(34.5, 69.5, 15.5, 90, 0));
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.NAME_TAG, "<6>Teleport to the Ticket Machine");
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
