package dev.daweapon.type.skyblockgeneric.gui.inventories;

import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.RefreshingGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorage;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorageChest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUIMinionStorage extends HypixelInventoryGUI implements RefreshingGUI {
    private static final int CONTENT_SLOTS = 27;
    private static final int BACK_SLOT = 30;
    private static final int COLLECT_SLOT = 31;

    private final IslandMinionData.IslandMinion minion;

    public GUIMinionStorage(IslandMinionData.IslandMinion minion) {
        super("Minion Storage", InventoryType.CHEST_4_ROW);
        this.minion = minion;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");

        set(new GUIClickableItem(BACK_SLOT) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer p) {
                new GUIMinion(minion).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.named(Material.ARROW, "<a>Go Back");
            }
        });

        set(new GUIClickableItem(COLLECT_SLOT) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                MinionStorage.drain(minion).forEach(item -> player.addAndUpdateItem(item.toSkyBlockItem()));
                refreshItems(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.named(Material.CHEST, "<a>Collect All");
            }
        });

        refreshItems(e.player());
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        MinionStorageChest chest = MinionStorage.get(minion);
        if (chest == null) {
            p.closeInventory();
            return;
        }

        for (int slot = 0; slot < CONTENT_SLOTS; slot++) {
            int index = slot;
            boolean unlocked = slot < chest.getTier().slots();
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent event, HypixelPlayer clicker) {
                    event.setCancelled(true);
                    if (!unlocked || chest.getItems().size() <= index) return;
                    if (!clicker.getInventory().getCursorItem().isAir()) return;
                    ItemQuantifiable entry = chest.getItems().remove(index);
                    MinionStorage.persist(minion);
                    ((SkyBlockPlayer) clicker).addAndUpdateItem(entry.toSkyBlockItem());
                    refreshItems(clicker);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer viewer) {
                    if (!unlocked) return ItemStacks.named(Material.WHITE_STAINED_GLASS_PANE, "");
                    if (chest.getItems().size() <= index) return ItemStack.builder(Material.AIR);
                    SkyBlockItem item = chest.getItems().get(index).getItem();
                    return new NonPlayerItemUpdater(item).getUpdatedItem();
                }
            });
        }
    }

    @Override
    public int refreshRate() {
        return 5;
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
