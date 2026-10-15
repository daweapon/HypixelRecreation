package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarNames;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarSettings;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.Comparator;
import java.util.List;

public class GUIBazaarIgnoreList extends HypixelInventoryGUI {
    private static final int[] SLOTS = BazaarSetLayout.pagedSlots();

    private final int page;

    public GUIBazaarIgnoreList(int page) {
        super(Text.key("gui_bazaar.ignore_list.title"), InventoryType.CHEST_6_ROW);
        this.page = page;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        List<ItemType> all = BazaarSettings.ignored(e.player().getUuid()).stream()
                .sorted(Comparator.comparing(BazaarNames::of))
                .toList();
        int pages = Math.max(1, (all.size() + SLOTS.length - 1) / SLOTS.length);
        int current = Math.max(0, Math.min(page, pages - 1));

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(BazaarMenuButtons.back(48, "Bazaar Settings", GUIBazaarSettings::new));
        set(GUIClickableItem.getCloseItem(49));
        for (int slot : SLOTS) {
            set(BazaarMenuButtons.empty(slot));
        }
        if (current > 0) {
            set(pageButton(45, current - 1, "gui_bazaar.buttons.previous_page", current));
        }
        if (current < pages - 1) {
            set(pageButton(53, current + 1, "gui_bazaar.buttons.next_page", current + 2));
        }

        int first = current * SLOTS.length;
        for (int i = 0; i < SLOTS.length && first + i < all.size(); i++) {
            ItemType type = all.get(first + i);
            set(new GUIClickableItem(SLOTS[i]) {
                @Override
                public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                    BazaarSettings.toggleIgnored(player.getUuid(), type);
                    new GUIBazaarIgnoreList(current).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return BazaarIcons.of(type, BazaarTooltips.productName(type),
                            List.of(Text.key("gui_bazaar.ignore_list.remove")), true);
                }
            });
        }
        updateItemStacks(getInventory(), getPlayer());
    }

    private GUIClickableItem pageButton(int slot, int target, String key, int shown) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                new GUIBazaarIgnoreList(target).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, 1, Text.key(key), List.of(Text.key("gui_bazaar.buttons.page_label", shown)));
            }
        };
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent event) {
        event.setCancelled(true);
        SkyBlockItem clicked = new SkyBlockItem(event.getClickedItem());
        ItemType type = clicked.getAttributeHandler().getPotentialType();
        if (type == null || BazaarCategories.getFromItem(type) == null) {
            return;
        }
        BazaarSettings.toggleIgnored(event.getPlayer().getUuid(), type);
        new GUIBazaarIgnoreList(page).open((HypixelPlayer) event.getPlayer());
    }
}
