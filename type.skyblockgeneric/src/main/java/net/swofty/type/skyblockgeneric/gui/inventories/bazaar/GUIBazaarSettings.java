package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarSettings;

import java.util.ArrayList;
import java.util.List;

public class GUIBazaarSettings extends HypixelInventoryGUI {
    private static final int IGNORE_LIST_SLOT = 11;
    private static final int VIEW_MODE_SLOT = 15;

    public GUIBazaarSettings() {
        super(Text.key("gui_bazaar.settings.title"), InventoryType.CHEST_4_ROW);

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(BazaarMenuButtons.back(30, "Bazaar", () -> new GUIBazaar(BazaarCategories.FARMING)));
        set(GUIClickableItem.getCloseItem(31));

        set(new GUIClickableItem(IGNORE_LIST_SLOT) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                new GUIBazaarIgnoreList(0).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>(Text.keyLines("gui_bazaar.settings.ignore_list.lore"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.settings.ignore_list.count", BazaarSettings.ignored(player.getUuid()).size()));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.settings.ignore_list.click"));
                return ItemStacks.item(Material.DROPPER, 1, Text.key("gui_bazaar.settings.ignore_list"), lore);
            }
        });

        set(new GUIClickableItem(VIEW_MODE_SLOT) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                BazaarSettings.toggleDirectMode(player.getUuid());
                updateItemStacks(getInventory(), player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return viewModeIcon(player);
            }
        });
    }

    static ItemStack.Builder viewModeIcon(HypixelPlayer player) {
        boolean direct = BazaarSettings.directMode(player.getUuid());
        String mode = direct ? "gui_bazaar.settings.view_mode.direct" : "gui_bazaar.settings.view_mode.classic";
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key("gui_bazaar.settings.view_mode.subtitle"));
        lore.add(Text.literal(" "));
        lore.addAll(Text.keyLines(mode + ".lore"));
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_bazaar.settings.view_mode.click"));
        return ItemStacks.item(Material.ANDESITE, 1, Text.key(mode), lore);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent event) {
        event.setCancelled(true);
    }
}
